//
//  StartViewController_iPad.m
//  NineCards
//
//  Created by Markus Wutzler on 14.10.13.
//  Copyright (c) 2013 Mobilis. All rights reserved.
//

#import "StartViewController_iPad.h"

@interface StartViewController_iPad ()
@property (weak, nonatomic) IBOutlet UIImageView *backgroundImageView;
@property (weak, nonatomic) IBOutlet UIView *layerView;

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
	// Do any additional setup after loading the view.
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)willAnimateRotationToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
	[UIView transitionWithView:self.backgroundImageView duration:duration options:UIViewAnimationOptionLayoutSubviews animations:^{
		self.backgroundImageView.image = [UIImage imageNamed:@"LaunchImage"];
		#warning wrong image set
	} completion:nil];
}

@end
