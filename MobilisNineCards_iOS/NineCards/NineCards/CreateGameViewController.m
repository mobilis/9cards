//
//  CreateGameViewController.m
//  NineCards
//
//  Created by Markus Wutzler on 11.10.13.
//  Copyright (c) 2013 Mobilis. All rights reserved.
//

#import <MobilisMXi/MXi/MXiConnectionHandler.h>
#import "CreateGameViewController.h"

@interface CreateGameViewController ()
@property (weak, nonatomic) IBOutlet UITextField *gameNameTextField;
@property (weak, nonatomic) IBOutlet UIStepper *gamePlayerStepper;
@property (weak, nonatomic) IBOutlet UIStepper *gameRoundsStepper;
@property (weak, nonatomic) IBOutlet UILabel *gamePlayerLabel;
@property (weak, nonatomic) IBOutlet UILabel *gameRoundsLabel;
@property (weak, nonatomic) IBOutlet UIButton *startGameButton;

- (IBAction)createGame:(id)sender;

@end

@implementation CreateGameViewController

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
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction)createGame:(id)sender {
    [[MXiConnectionHandler sharedInstance] createServiceWithCompletionBlock:^(NSString *serviceJID)
    {
        NSLog(@"Service with JID %@ created", serviceJID);
    }];
}
@end
