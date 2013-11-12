//
//  GameViewController.m
//  NineCards
//
//  Created by Markus Wutzler on 10.10.13.
//  Copyright (c) 2013 Mobilis. All rights reserved.
//



#import "GameViewController.h"
#import "CardButton.h"
#import "GamePointsViewController.h"

#import <MXi/MXi.h>
#import "StartGameMessage.h"
#import "GetGameConfigurationRequest.h"
#import "GetGameConfigurationResponse.h"

#import "CardPlayedMessage.h"
#import "GameOverMessage.h"
#import "RoundCompleteMessage.h"
#import "GameStartsMessage.h"
#import "PlayCardMessage.h"

#import "NSString+StringUtils.h"

@interface GameViewController () <MXiMultiUserChatDelegate>

@property (strong, nonatomic) IBOutletCollection(UIButton) NSArray *cardButtons;
@property (weak, nonatomic) IBOutlet UIButton *startButton;
- (IBAction)quitGame:(UIBarButtonItem *)sender;
- (IBAction)cardPlayed:(CardButton *)card;
- (IBAction)startGame:(UIButton *)startButton;
- (IBAction)showGamePoints:(id)sender;

- (void) startGameMessageReceived:(GameStartsMessage *)bean;
- (void) cardPlayedMessageReceived:(CardPlayedMessage *)bean;
- (void) roundCompleteMessageReceived:(RoundCompleteMessage *)bean;
- (void) gameOverMessageReceived:(GameOverMessage *)bean;

- (void)showWaitingView;
- (void)hideWaitingView;

@end

@implementation GameViewController {
	BOOL _gameStarted;
	NSNumber *_rounds;
	NSNumber *_currentRound;
    
    __strong UIView *_waitingView;
    __strong UIPopoverController *_pointsPopOverView;
    __strong GamePointsViewController *_gamePointsViewController;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	for (UIButton *cardButton in self.cardButtons) {
		cardButton.enabled = NO;
		if([UIDevice currentDevice].userInterfaceIdiom == UIUserInterfaceIdiomPhone)
		{
			[[cardButton layer] setCornerRadius:4.0f];
			[[cardButton layer] setBorderWidth:0.5f];
			cardButton.titleLabel.font = [UIFont fontWithName:@"AppleColorEmoji" size:36.f];
		}
	}
	_gameStarted = NO;
    if ([self.game hasGameConfiguration]) {
        [[MXiConnectionHandler sharedInstance] connectToMultiUserChatRoom:[_game roomJid].bare withDelegate:self];
    } else {
        [[MXiConnectionHandler sharedInstance] addDelegate:self withSelector:@selector(gameConfigurationReceived:) forBeanClass:[GetGameConfigurationResponse class]];
        GetGameConfigurationRequest *gameConfigurationRequest = [GetGameConfigurationRequest new];
        gameConfigurationRequest.to = self.game.gameJid;
        [[MXiConnectionHandler sharedInstance] sendBean:gameConfigurationRequest];
        self.startButton.hidden = YES;
    }
    
    GamePointsViewController *gamePointsViewController = [[GamePointsViewController alloc] initWithNibName:@"GamePointsView"
                                                                                                    bundle:nil];
    [self addChildViewController:gamePointsViewController];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (NSString *)title
{
    return self.game.name;
}

- (void)dealloc
{
    [[MXiConnectionHandler sharedInstance] removeDelegate:self withSelector:@selector(gameConfigurationReceived:) forBeanClass:[GetGameConfigurationResponse class]];
}

- (IBAction)quitGame:(UIBarButtonItem *)sender
{
	[self.navigationController popToRootViewControllerAnimated:YES];
}

- (IBAction)cardPlayed:(CardButton *)card {
    card.enabled = NO;
    PlayCardMessage *play = [PlayCardMessage new];
    play.card = card.cardNumber;
    play.round = _currentRound;
        
    [[MXiConnectionHandler sharedInstance] sendMessageString:[[play toXML] XMLString] toJID:[[_game gameJid] full]];
    [self showWaitingView];
    NSLog(@"%@", [[play toXML] XMLString]);
}

- (IBAction)startGame:(UIButton *)startButton
{
	for (CardButton *card in _cardButtons) {
		[card setEnabled:YES];
	}
	startButton.enabled = NO;
	startButton.hidden = YES;
	StartGameMessage *startGame = [StartGameMessage new];

    [[MXiConnectionHandler sharedInstance] sendMessageString:[[startGame toXML] XMLString] toJID:[_game.gameJid full]];
	NSLog(@"%@", [[startGame toXML] XMLString]);
}

- (IBAction)showGamePoints:(id)sender {
    if (!_pointsPopOverView) {
        _gamePointsViewController = [[GamePointsViewController alloc] initWithNibName:@"GamePointsView"
                                                                               bundle:nil];
        _pointsPopOverView = [[UIPopoverController alloc] initWithContentViewController:_gamePointsViewController];
    }
    [_pointsPopOverView presentPopoverFromBarButtonItem:sender
                               permittedArrowDirections:UIPopoverArrowDirectionAny
                                               animated:YES];
}

#pragma mark - MXiMUCDelegate
- (void)connectionToRoomEstablished:(NSString *)roomJID
{
    self.startButton.hidden = NO;
	NSLog(@"ConnectionToRoomEstablished: %@", roomJID);
}

-(void)didReceiveMultiUserChatMessage:(NSString *)message fromUser:(NSString *)user publishedInRoom:(NSString *)roomJID
{
	NSError *error;
	NSXMLElement *messageBean = [[NSXMLElement alloc] initWithXMLString:message error:&error];
	if(!error) {
		if ([messageBean.name isEqualToString:[GameStartsMessage elementName] ignoreCase:YES]) {
			GameStartsMessage *start = [GameStartsMessage new];
			[start fromXML:messageBean];
			[self startGameMessageReceived:start];
		} else if ([messageBean.name isEqualToString:[CardPlayedMessage elementName] ignoreCase:YES]) {
			CardPlayedMessage *card = [CardPlayedMessage new];
			[card fromXML:messageBean];
			[self cardPlayedMessageReceived:card];
		} else if ([messageBean.name isEqualToString:[RoundCompleteMessage elementName] ignoreCase:YES]) {
			RoundCompleteMessage *round = [RoundCompleteMessage new];
			[round fromXML:messageBean];
			[self roundCompleteMessageReceived:round];
		} else if ([messageBean.name isEqualToString:[GameOverMessage elementName] ignoreCase:YES]) {
			GameOverMessage *gameOver = [GameOverMessage new];
			[gameOver fromXML:messageBean];
			[self gameOverMessageReceived:gameOver];
		} else {
			NSLog(@"Message %@ from User %@ in room %@ wasn't processed.", message, user, roomJID);
		}
	}
}

- (void)startGameMessageReceived:(GameStartsMessage *)bean
{
	if (!_gameStarted) {
		_gameStarted = YES;
		_currentRound = [NSNumber numberWithInt:1];
	}
}

- (void)cardPlayedMessageReceived:(CardPlayedMessage *)bean
{
	if(_gameStarted && [bean.round isEqualToNumber:_currentRound]) {
		//update player
	}
}

- (void)roundCompleteMessageReceived:(RoundCompleteMessage *)bean
{
	if (_gameStarted) {
		_currentRound = [NSNumber numberWithInt:[_currentRound intValue]+1];
        [_gamePointsViewController updatePlayersWithPlayerInfos:bean.playerInfos];
        [self hideWaitingView];
	}
}

- (void)gameOverMessageReceived:(GameOverMessage *)bean
{

}

- (void)gameConfigurationReceived:(GetGameConfigurationResponse *)response
{
    self.game = [Game new];
    self.game.players = response.maxPlayers;
    self.game.rounds = response.maxRounds;
    
    [[MXiConnectionHandler sharedInstance] connectToMultiUserChatRoom:response.muc withDelegate:self];
}

- (void)showWaitingView
{
    if (!_waitingView) {
        [self setupWaitingView];
    }
    
    [self.view addSubview:_waitingView];
}
- (void)setupWaitingView
{
    _waitingView = [[UIView alloc] initWithFrame:self.view.frame];
    _waitingView.backgroundColor = [UIColor clearColor];
    
    UILabel *waitingForOthersLabel = [[UILabel alloc] initWithFrame:CGRectZero];
    waitingForOthersLabel.textAlignment = NSTextAlignmentCenter;
    waitingForOthersLabel.text = @"Waiting for other players to finish.";
    waitingForOthersLabel.textColor = [UIColor whiteColor];
    waitingForOthersLabel.font = [UIFont boldSystemFontOfSize:20.0];
    [waitingForOthersLabel setTranslatesAutoresizingMaskIntoConstraints:NO];
    
    UIView *coloredBackgroundView = [[UIView alloc] initWithFrame:CGRectMake(0.0, 0.0, self.view.frame.size.width, 200.0)];
    coloredBackgroundView.backgroundColor = [UIColor colorWithRed:(15.0/255.0) green:(101.0/255) blue:(255.0/255.0) alpha:1.0];
    coloredBackgroundView.layer.cornerRadius = 5.0;
    [coloredBackgroundView setTranslatesAutoresizingMaskIntoConstraints:NO];
    
    [coloredBackgroundView addSubview:waitingForOthersLabel];
    [_waitingView addSubview:coloredBackgroundView];
    
    [_waitingView addConstraint:[NSLayoutConstraint constraintWithItem:waitingForOthersLabel
                                                             attribute:NSLayoutAttributeCenterY
                                                             relatedBy:NSLayoutRelationEqual
                                                                toItem:coloredBackgroundView
                                                             attribute:NSLayoutAttributeCenterY
                                                            multiplier:1.0
                                                              constant:0.0]];
    [_waitingView addConstraint:[NSLayoutConstraint constraintWithItem:waitingForOthersLabel
                                                             attribute:NSLayoutAttributeCenterX
                                                             relatedBy:NSLayoutRelationEqual
                                                                toItem:coloredBackgroundView
                                                             attribute:NSLayoutAttributeCenterX
                                                            multiplier:1.0
                                                              constant:0.0]];
    [_waitingView addConstraint:[NSLayoutConstraint constraintWithItem:coloredBackgroundView
                                                             attribute:NSLayoutAttributeHeight
                                                             relatedBy:NSLayoutRelationEqual
                                                                toItem:waitingForOthersLabel
                                                             attribute:NSLayoutAttributeHeight
                                                            multiplier:2.0
                                                              constant:0.0]];
    [_waitingView addConstraint:[NSLayoutConstraint constraintWithItem:coloredBackgroundView
                                                             attribute:NSLayoutAttributeWidth
                                                             relatedBy:NSLayoutRelationEqual
                                                                toItem:waitingForOthersLabel
                                                             attribute:NSLayoutAttributeWidth
                                                            multiplier:1.25
                                                              constant:0.0]];
    
    [_waitingView addConstraint:[NSLayoutConstraint constraintWithItem:coloredBackgroundView
                                                             attribute:NSLayoutAttributeCenterY
                                                             relatedBy:NSLayoutRelationEqual
                                                                toItem:_waitingView
                                                             attribute:NSLayoutAttributeCenterY
                                                            multiplier:1.0
                                                              constant:0.0]];
    [_waitingView addConstraint:[NSLayoutConstraint constraintWithItem:coloredBackgroundView
                                                             attribute:NSLayoutAttributeCenterX
                                                             relatedBy:NSLayoutRelationEqual
                                                                toItem:_waitingView
                                                             attribute:NSLayoutAttributeCenterX
                                                            multiplier:1.0
                                                              constant:0.0]];
}

- (void)hideWaitingView
{
    [_waitingView removeFromSuperview];
    [self.view setNeedsDisplay];
}

@end
