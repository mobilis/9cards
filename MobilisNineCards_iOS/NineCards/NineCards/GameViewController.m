//
//  GameViewController.m
//  NineCards
//
//  Created by Markus Wutzler on 10.10.13.
//  Copyright (c) 2013 Mobilis. All rights reserved.
//



#import "GameViewController.h"

@interface GameViewController ()
@property (strong, nonatomic) IBOutletCollection(UIButton) NSArray *cardButtons;
- (IBAction)quitGame:(UIBarButtonItem *)sender;

@end

@implementation GameViewController

- (void)viewDidLoad
{
    [super viewDidLoad];

	for (UIButton *cardButton in self.cardButtons) {
		[[cardButton layer] setCornerRadius:4.0f];
		[[cardButton layer] setBorderWidth:0.25f];
		cardButton.titleLabel.font = [UIFont fontWithName:@"AppleColorEmoji" size:36.f];
	}
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction)quitGame:(UIBarButtonItem *)sender
{
	[self.navigationController popToRootViewControllerAnimated:YES];
}
@end
