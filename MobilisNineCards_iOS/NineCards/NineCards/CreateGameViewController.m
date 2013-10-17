//
//  CreateGameViewController.m
//  NineCards
//
//  Created by Markus Wutzler on 11.10.13.
//  Copyright (c) 2013 Mobilis. All rights reserved.
//

#import <MobilisMXi/MXi/MXiConnectionHandler.h>
#import "CreateGameViewController.h"

#import "ConfigureGameRequest.h"
#import "ConfigureGameResponse.h"

@interface CreateGameViewController ()
@property (weak, nonatomic) IBOutlet UITextField *gameNameTextField;
@property (weak, nonatomic) IBOutlet UIStepper *gamePlayerStepper;
@property (weak, nonatomic) IBOutlet UIStepper *gameRoundsStepper;
@property (weak, nonatomic) IBOutlet UILabel *gamePlayerLabel;
@property (weak, nonatomic) IBOutlet UILabel *gameRoundsLabel;
@property (weak, nonatomic) IBOutlet UIButton *startGameButton;



- (IBAction)createGame:(id)sender;
- (IBAction)cancelGameCreation:(UIBarButtonItem *)sender;

@end

@implementation CreateGameViewController

Game *_game;

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
		ConfigureGameRequest *req = [ConfigureGameRequest new];
		req.to = [XMPPJID jidWithString:serviceJID];
		req.players = [NSNumber numberWithDouble:_gamePlayerStepper.value];
		req.rounds = [NSNumber numberWithDouble:_gameRoundsStepper.value];
		if(_delegate)
		{
			[_delegate gameCreated:nil];
		}
		else
		{
			[self gameCreated:nil];
		}
    }];
}

- (IBAction)cancelGameCreation:(UIBarButtonItem *)sender {
	[self dismissViewControllerAnimated:YES completion:nil];
}
	   
#pragma mark - CreateGameDelegate
-(void)gameCreated:(Game *)game
{
	[self performSegueWithIdentifier:@"JoinGame" sender:nil];
}
@end
