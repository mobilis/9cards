//
//  ViewController.m
//  NineCards
//
//  Created by Markus Wutzler on 10.10.13.
//  Copyright (c) 2013 Mobilis. All rights reserved.
//

<<<<<<< Updated upstream:MobilisNineCards_iOS/NineCards/NineCards/GameListViewController.m
#import <MobilisMXi/MXi/MXiConnectionHandler.h>
#import "GameListViewController.h"

@interface GameListViewController ()<UITableViewDataSource, UITableViewDelegate>

@property (weak, nonatomic) IBOutlet UITableView *tableView;
@property (strong, nonatomic) NSArray *availableGames;
=======
#import "GameListTableViewController.h"

@interface GameListTableViewController ()
>>>>>>> Stashed changes:MobilisNineCards_iOS/NineCards/NineCards/GameListTableViewController.m

@end

@implementation GameListTableViewController

static void *KVOContext = &KVOContext;

- (void)viewDidLoad
{
    [super viewDidLoad];
<<<<<<< Updated upstream:MobilisNineCards_iOS/NineCards/NineCards/GameListViewController.m
	self.view.backgroundColor = [UIColor colorWithPatternImage:[UIImage imageNamed:@"LaunchImage"]];
	UITableViewController *tvCtr = [[UITableViewController alloc] init];
	tvCtr.tableView = self.tableView;
	tvCtr.tableView.delegate = self;
	tvCtr.tableView.dataSource = self;
	[self addChildViewController:tvCtr];

    self.availableGames = [NSArray new];
    [[MXiConnectionHandler sharedInstance] addObserver:self
                                            forKeyPath:@"discoveredServiceInstances"
                                               options:NSKeyValueObservingOptionNew
                                               context:KVOContext];
=======
>>>>>>> Stashed changes:MobilisNineCards_iOS/NineCards/NineCards/GameListTableViewController.m
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

<<<<<<< Updated upstream:MobilisNineCards_iOS/NineCards/NineCards/GameListViewController.m
#pragma mark - KVO Compliance

- (void)observeValueForKeyPath:(NSString *)keyPath ofObject:(id)object change:(NSDictionary *)change context:(void *)context
{
    if ([keyPath isEqualToString:@"discoveredServiceInstances"] && context == KVOContext) {
        self.availableGames = [[MXiConnectionHandler sharedInstance] discoveredServiceInstances];
        [self.tableView reloadData];
    }
}

=======
>>>>>>> Stashed changes:MobilisNineCards_iOS/NineCards/NineCards/GameListTableViewController.m
@end
