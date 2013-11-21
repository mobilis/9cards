//
//  ViewController.m
//  NineCards
//
//  Created by Markus Wutzler on 10.10.13.
//  Copyright (c) 2013 Mobilis. All rights reserved.
//

#import <MobilisMXi/MXi/MXiConnectionHandler.h>
#import "GameListTableViewController.h"
#import "Game.h"

@interface GameListTableViewController ()<UITableViewDataSource, UITableViewDelegate>

@property (weak, nonatomic) UITableView *tableView;
@property (strong, nonatomic) NSArray *availableGames;
@property (strong, nonatomic) UIRefreshControl *myRefreshControl;

- (void) handleRefresh:(UIRefreshControl *)sender;

@end

@implementation GameListTableViewController

static void *KVOContext = &KVOContext;

- (void)viewDidLoad
{
    [super viewDidLoad];
	self.refreshControl = [[UIRefreshControl alloc] init];
	[self.refreshControl addTarget:self action:@selector(handleRefresh:) forControlEvents:UIControlEventValueChanged];
    self.availableGames = [[MXiConnectionHandler sharedInstance] discoveredServiceInstances];
    [[MXiConnectionHandler sharedInstance] addObserver:self
											forKeyPath:@"discoveredServiceInstances"
                                               options:0
                                               context:KVOContext];
}

- (void)viewWillAppear:(BOOL)animated
{
	[[MXiConnectionHandler sharedInstance] rediscoverServices:nil];
	[super viewWillAppear:animated];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - UITableViewDataSource
-(NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
	return 1;
}

-(NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
	return self.availableGames ? self.availableGames.count : 0;
}

-(UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
	UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"GameCell"];
	if (!cell) {
		cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:@"GameCell"];
	}

    MXiService *service = [self.availableGames objectAtIndex:indexPath.row];
    cell.textLabel.text = service.name;
	
	return cell;
}

#pragma mark - KVO Compliance
- (void)observeValueForKeyPath:(NSString *)keyPath ofObject:(id)object change:(NSDictionary *)change context:(void *)context
{
    if ([keyPath isEqualToString:@"discoveredServiceInstances"] && context == KVOContext) {
        self.availableGames = [[MXiConnectionHandler sharedInstance] discoveredServiceInstances];
        [self.tableView reloadData];
		[self.refreshControl endRefreshing];
    }
}


- (Game *)gameForIndexPath:(NSIndexPath *)path
{
	MXiService *service = [self.availableGames objectAtIndex:path.row];
	Game *game = [[Game alloc] initWithName:service.name numberOfPlayers:nil numberOfRounds:nil andGameJid:service.jid];
	return game;
}

- (void)handleRefresh:(UIRefreshControl *)sender
{
	[[MXiConnectionHandler sharedInstance] rediscoverServices:nil];
	dispatch_after(dispatch_time(DISPATCH_TIME_NOW, 10000000000), dispatch_get_main_queue(), ^{
		[self.refreshControl endRefreshing];
	});
}

@end
