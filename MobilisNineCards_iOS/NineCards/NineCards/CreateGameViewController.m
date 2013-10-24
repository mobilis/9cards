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

@interface CreateGameViewController () {
@private __strong Game *_game;
}

@property (weak, nonatomic) IBOutlet UITextField *gameNameTextField;
@property (weak, nonatomic) IBOutlet UIStepper *gamePlayerStepper;
@property (weak, nonatomic) IBOutlet UIStepper *gameRoundsStepper;
@property (weak, nonatomic) IBOutlet UILabel *gamePlayerLabel;
@property (weak, nonatomic) IBOutlet UILabel *gameRoundsLabel;
@property (weak, nonatomic) IBOutlet UIButton *startGameButton;


- (void) didReceiveConfigureGameResponse;
- (IBAction)createGame:(id)sender;
- (IBAction)cancelGameCreation:(UIBarButtonItem *)sender;

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
	[[MXiConnectionHandler sharedInstance] addDelegate:self withSelector:@selector(didReceiveConfigureGameResponse) forBeanClass:[ConfigureGameResponse class]];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction)createGame:(id)sender {
    [[MXiConnectionHandler sharedInstance] createServiceWithName:_gameNameTextField.text completionBlock:^(NSString * serviceJID)
    {
		NSLog(@"Service with JID %@ created", serviceJID);
		_game = [[Game alloc] initWithName:_gameNameTextField.text
						   numberOfPlayers:[NSNumber numberWithDouble:_gamePlayerStepper.value]
							numberOfRounds:[NSNumber numberWithDouble:_gameRoundsStepper.value]
								andGameJid:[XMPPJID jidWithString:serviceJID]];
		
		ConfigureGameRequest *req = [ConfigureGameRequest new];
		req.to = _game.gameJid;
		req.gamename = _game.name;
		req.players = _game.players;
		req.rounds = _game.rounds;
		[[MXiConnectionHandler sharedInstance] sendBean:req];
    }];
}

- (IBAction)cancelGameCreation:(UIBarButtonItem *)sender {
	[self dismissViewControllerAnimated:YES completion:nil];
}

- (void)didReceiveConfigureGameResponse
{
	if(_delegate)
	{
		[self dismissViewControllerAnimated:YES completion:^{
			[_delegate gameCreated:_game];
		}];
	}
	else
	{
		[self gameCreated:_game];
	}
}

#pragma mark - CreateGameDelegate
-(void)gameCreated:(Game *)game
{
	[self performSegueWithIdentifier:@"JoinGame" sender:nil];
}
@end
