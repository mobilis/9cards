//
//  GameViewController.m
//  NineCards
//
//  Created by Markus Wutzler on 10.10.13.
//  Copyright (c) 2013 Mobilis. All rights reserved.
//



#import "GameViewController.h"
#import "CardButton.h"

#import <MXi/MXi.h>
#import "StartGameMessage.h"

#import "CardPlayedMessage.h"
#import "GameOverMessage.h"
#import "RoundCompleteMessage.h"
#import "GameStartsMessage.h"
#import "PlayCardMessage.h"

#import "NSString+StringUtils.h"

@interface GameViewController () <MXiMultiUserChatDelegate>
@property (strong, nonatomic) IBOutletCollection(UIButton) NSArray *cardButtons;
- (IBAction)quitGame:(UIBarButtonItem *)sender;
- (IBAction)cardPlayed:(CardButton *)card;
- (IBAction)startGame:(UIButton *)startButton;

- (void) startGameMessageReceived:(GameStartsMessage *)bean;
- (void) cardPlayedMessageReceived:(CardPlayedMessage *)bean;
- (void) roundCompleteMessageReceived:(RoundCompleteMessage *)bean;
- (void) gameOverMessageReceived:(GameOverMessage *)bean;

@end

@implementation GameViewController {
	BOOL _gameStarted;
    BOOL _roundCompleted;
	NSNumber *_rounds;
	NSNumber *_currentRound;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	self.navigationItem.title = _game.name;
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
    _roundCompleted = YES;
	[[MXiConnectionHandler sharedInstance] connectToMultiUserChatRoom:[_game roomJid].bare withDelegate:self];
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

- (IBAction)cardPlayed:(CardButton *)card {
    if (_roundCompleted) {
        card.enabled = NO;
        PlayCardMessage *play = [PlayCardMessage new];
        play.card = card.cardNumber;
        play.round = _currentRound;
        
        [[MXiConnectionHandler sharedInstance] sendMessageString:[[play toXML] XMLString] toJID:[[_game gameJid] full]];
        _roundCompleted = NO;
        NSLog(@"%@", [[play toXML] XMLString]);
    }
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

#pragma mark - MXiMUCDelegate
- (void)connectionToRoomEstablished:(NSString *)roomJID
{
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
        _roundCompleted = YES;
	}
}

- (void)gameOverMessageReceived:(GameOverMessage *)bean
{

}


@end
