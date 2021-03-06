//
//  CreateGameViewController.m
//  NineCards
//
//  Created by Markus Wutzler on 11.10.13.
//  Copyright (c) 2013 Mobilis. All rights reserved.
//

#import <MobilisMXi/MXi/MXiConnectionHandler.h>
#import "CreateGameViewController.h"
#import "GameViewController.h"

#import "ConfigureGameRequest.h"
#import "ConfigureGameResponse.h"
#import "MXiServiceManager.h"

@interface CreateGameViewController () <MXiServiceManagerDelegate>
{
@private __strong Game *_game;
@private __strong MXiServiceManager *_serviceManager;
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

- (IBAction)stepperValueChanged:(id)sender;

@end

@implementation CreateGameViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
    _serviceManager = [MXiConnectionHandler sharedInstance].serviceManager;
    [_serviceManager addDelegate:self];
    self.gameRoundsStepper.value = [self.gameRoundsLabel.text doubleValue];
}

- (void)viewWillDisappear:(BOOL)animated
{
    [_serviceManager removeDelegate:self];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction)createGame:(id)sender {
    [_serviceManager createServiceWithName:_gameNameTextField.text andPassword:nil];
}

- (IBAction)cancelGameCreation:(UIBarButtonItem *)sender {
	if (self.navigationController) {
		[self.navigationController popViewControllerAnimated:YES];
	} else {
		[self dismissViewControllerAnimated:YES completion:nil];
	}
}

- (IBAction)stepperValueChanged:(id)sender {
    if (self.gamePlayerStepper == sender) {
        self.gamePlayerLabel.text = [NSString stringWithFormat:@"%.0f", self.gamePlayerStepper.value];
    }
    if (self.gameRoundsStepper == sender) {
        self.gameRoundsLabel.text = [NSString stringWithFormat:@"%.0f", self.gameRoundsStepper.value];
    }
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
	[self performSegueWithIdentifier:@"JoinGame" sender:game];
}

- (BOOL)shouldPerformSegueWithIdentifier:(NSString *)identifier sender:(id)sender
{
	if ([sender class] != [Game class])
	{
		((UIButton*)sender).enabled = NO;
		return NO;
	} else {
		return YES;
	}
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(Game *)game
{
	if ([segue.identifier isEqualToString:@"JoinGame"])
	{
		((GameViewController*)segue.destinationViewController).game = game;
	}
}

- (void)viewDidDisappear:(BOOL)animated
{
    [[MXiConnectionHandler sharedInstance].connection removeBeanDelegate:self forBeanClass:[ConfigureGameResponse class]];
	[super viewDidDisappear:animated];
}

- (void)viewWillAppear:(BOOL)animated
{
    [[MXiConnectionHandler sharedInstance].connection addBeanDelegate:self
                                                         withSelector:@selector(didReceiveConfigureGameResponse)
                                                         forBeanClass:[ConfigureGameResponse class]];
	[super viewWillAppear:animated];
}

#pragma mark - MXiServiceManagerDelegate

- (void)serviceDiscoveryFinishedWithError:(NSError *)error
{
    NSLog(@"%@", error);
}

- (void)createdServiceInstanceSuccessfully:(MXiService *)service
{
    NSLog(@"Service with JID %@ created", service.jid);
    _game = [[Game alloc] initWithName:_gameNameTextField.text
                       numberOfPlayers:[NSNumber numberWithDouble:_gamePlayerStepper.value]
                        numberOfRounds:[NSNumber numberWithDouble:_gameRoundsStepper.value]
                            andGameJid:service.jid];

    ConfigureGameRequest *req = [ConfigureGameRequest new];
    req.to = _game.gameJid;
    req.players = _game.players;
    req.rounds = _game.rounds;
    [[MXiConnectionHandler sharedInstance].connection sendBean:req];
}

@end
