//
//  StartViewController_iPad.m
//  NineCards
//
//  Created by Markus Wutzler on 14.10.13.
//  Copyright (c) 2013 Mobilis. All rights reserved.
//

#import "StartViewController_iPad.h"
#import "GameListTableViewController.h"

#import "CreateGameViewController.h"
#import "GameViewController.h"

#import <DDLog.h>

@interface StartViewController_iPad () <CreateGameDelegate>
@property (weak, nonatomic) IBOutlet UIImageView *backgroundImageView;
@property (weak, nonatomic) IBOutlet UIView *layerView;
@property (weak, nonatomic) IBOutlet UITableView *gameListTableView;

@end

@implementation StartViewController_iPad

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
	gameList.tableView = self.gameListTableView;
	gameList.tableView.delegate = gameList;
	gameList.tableView.dataSource = gameList;
	[gameList viewDidLoad];
	
	NSString *imageName;
	if (UIInterfaceOrientationIsLandscape(self.interfaceOrientation)) {
		imageName = @"9Cards-BGL-iPad";
	} else {
		imageName = @"9Cards-BGP-iPad";
	}
	self.backgroundImageView.image = [UIImage imageNamed:imageName];
	[self addChildViewController:gameList];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

- (void)willAnimateRotationToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
	NSString *imageName;
	if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
		imageName = @"9Cards-BGL-iPad";
	} else {
		imageName = @"9Cards-BGP-iPad";
	}
	[UIView animateWithDuration:duration
					 animations:^{
						 [self.view layoutIfNeeded];
						 self.backgroundImageView.image = [UIImage imageNamed:imageName];
	}];
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
	if ([segue.identifier isEqualToString:@"CreateGame"]) {
		((CreateGameViewController *)segue.destinationViewController).delegate = self;
	} else if ([segue.identifier isEqualToString:@"JoinGame"]) {
		if([sender class] == [Game class]) {
			((GameViewController*)segue.destinationViewController).game = sender;
		} else {
			NSLog(@"%@", [[sender class] description]);
		}
	}
}

#pragma mark - UINavigationBarDelegate
-(UIBarPosition)positionForBar:(id<UIBarPositioning>)bar
{
	return UIBarPositionTopAttached;
}

#pragma mark - Create Game Delegate
- (void)gameCreated:(Game *)game
{
	[self performSegueWithIdentifier:@"JoinGame" sender:game];
}

@end
