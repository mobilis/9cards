//
//  ViewController.m
//  NineCards
//
//  Created by Markus Wutzler on 10.10.13.
//  Copyright (c) 2013 Mobilis. All rights reserved.
//

#import <MobilisMXi/MXi/MXiConnectionHandler.h>
#import <MobilisMXi/MXi/MXiServiceManager.h>
#import "GameListTableViewController.h"
#import "Game.h"

@interface GameListTableViewController ()<MXiServiceManagerDelegate>

@property (weak, nonatomic) UITableView *tableView;
@property (strong, nonatomic) NSArray *availableGames;
@property (strong, nonatomic) UIRefreshControl *myRefreshControl;

- (void) handleRefresh:(UIRefreshControl *)sender;

@end

@implementation GameListTableViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
	self.refreshControl = [[UIRefreshControl alloc] init];
	[self.refreshControl addTarget:self action:@selector(handleRefresh:) forControlEvents:UIControlEventValueChanged];
    self.availableGames = [MXiConnectionHandler sharedInstance].serviceManager.services;

}

- (void)viewWillAppear:(BOOL)animated
{
	[[MXiConnectionHandler sharedInstance].serviceManager rediscoverServices];
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

- (Game *)gameForIndexPath:(NSIndexPath *)path
{
	MXiService *service = [self.availableGames objectAtIndex:path.row];
	Game *game = [[Game alloc] initWithName:service.name numberOfPlayers:nil numberOfRounds:nil andGameJid:service.jid];
	return game;
}

- (void)handleRefresh:(UIRefreshControl *)sender
{
	[[MXiConnectionHandler sharedInstance].serviceManager rediscoverServices];
	dispatch_after(dispatch_time(DISPATCH_TIME_NOW, 10000000000), dispatch_get_main_queue(), ^{
		[self.refreshControl endRefreshing];
	});
}

#pragma mark - MXiServiceManagerDelegate

- (void)serviceDiscoveryFinishedWithError:(NSError *)error
{
    if (!error) {
        self.availableGames = [MXiConnectionHandler sharedInstance].serviceManager.services;
        [self.tableView reloadData];
        [self.refreshControl endRefreshing];
    }
}

@end
