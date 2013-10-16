//
//  StartViewController_iPhone.m
//  NineCards
//
//  Created by Markus Wutzler on 15.10.13.
//  Copyright (c) 2013 Mobilis. All rights reserved.
//

#import "StartViewController_iPhone.h"
#import "GameListTableViewController.h"

@interface StartViewController_iPhone ()
@property (weak, nonatomic) IBOutlet UITableView *tableView;

@end

@implementation StartViewController_iPhone

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	GameListTableViewController *gameList = [GameListTableViewController new];
	gameList.tableView = self.tableView;
	gameList.tableView.delegate = gameList;
	gameList.tableView.dataSource = gameList;
	
	NSString *imageName = @"9Cards-BG-iPhone";
	if ([UIScreen mainScreen].bounds.size.height == 568.0) {
		imageName = [NSString stringWithFormat:@"%@-568h", imageName];
	}
	self.view.backgroundColor = [UIColor colorWithPatternImage:[UIImage imageNamed:imageName]];
	[self addChildViewController:gameList];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

@end
