//
//  StartViewController_iPhone.m
//  NineCards
//
//  Created by Markus Wutzler on 15.10.13.
//  Copyright (c) 2013 Mobilis. All rights reserved.
//

#import "StartViewController_iPhone.h"
#import "GameListTableViewController.h"
#import "GameViewController.h"

@interface StartViewController_iPhone ()
@property (weak, nonatomic) IBOutlet UITableView *tableView;
@property (strong) GameListTableViewController *gameList;

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
	self.gameList = [GameListTableViewController new];
	_gameList.tableView = self.tableView;
	_gameList.tableView.delegate = _gameList;
	_gameList.tableView.dataSource = _gameList;
	
	NSString *imageName = @"9Cards-BG-iPhone";
	if ([UIScreen mainScreen].bounds.size.height == 568.0) {
		imageName = [NSString stringWithFormat:@"%@-568h", imageName];
	}
	self.view.backgroundColor = [UIColor colorWithPatternImage:[UIImage imageNamed:imageName]];
	[self addChildViewController:_gameList];
	[_gameList viewDidLoad];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
	if ([segue.identifier isEqualToString:@"JoinGame"]) {
		if([sender class] == [Game class]) {
			((GameViewController*)segue.destinationViewController).game = sender;
		} else {
			((GameViewController*)segue.destinationViewController).game = [_gameList gameForIndexPath:[_gameList.tableView indexPathForSelectedRow]];
		}
	}
}

- (BOOL)shouldAutomaticallyForwardAppearanceMethods
{
	return YES;
}

- (BOOL)shouldAutomaticallyForwardRotationMethods
{
	return YES;
}
@end
