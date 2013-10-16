//
//  StartViewController_iPad.m
//  NineCards
//
//  Created by Markus Wutzler on 14.10.13.
//  Copyright (c) 2013 Mobilis. All rights reserved.
//

#import "StartViewController_iPad.h"
#import "GameListTableViewController.h"

@interface StartViewController_iPad ()
@property (weak, nonatomic) IBOutlet UIImageView *backgroundImageView;
@property (weak, nonatomic) IBOutlet UIView *layerView;
@property (weak, nonatomic) IBOutlet UITableView *gameListTableView;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *gameListTableViewHeightConstraint;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *gameListTableViewWidthConstraint;

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
    // Dispose of any resources that can be recreated.
}

- (void)willAnimateRotationToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
	CGFloat height, width;
	NSString *imageName;
	CGSize screenSize = [UIScreen mainScreen].bounds.size;
	if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
		height = screenSize.width / 3;
		width = screenSize.height / 3;
		imageName = @"9Cards-BGL-iPad";
	} else {
		height = screenSize.height / 3;
		width = screenSize.width / 3;
		imageName = @"9Cards-BGP-iPad";
	}
	[UIView animateWithDuration:duration
					 animations:^{
						 [self.view layoutIfNeeded];
						 self.backgroundImageView.image = [UIImage imageNamed:imageName];
	}];
}

#pragma mark - UINavigationBarDelegate
-(UIBarPosition)positionForBar:(id<UIBarPositioning>)bar
{
	return UIBarPositionTopAttached;
}

@end
