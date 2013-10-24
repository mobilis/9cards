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

#import "NSString+StringUtils.h"

@interface GameViewController () <MXiMultiUserChatDelegate>
@property (strong, nonatomic) IBOutletCollection(UIButton) NSArray *cardButtons;
- (IBAction)quitGame:(UIBarButtonItem *)sender;
- (IBAction)cardPlayed:(CardButton *)card;
- (IBAction)startGame:(UIButton *)startButton;

- (void) startGameMessageReceived:(StartGameMessage *)bean;
- (void) cardPlayedMessageReceived:(CardPlayedMessage *)bean;
- (void) roundCompleteMessageReceived:(RoundCompleteMessage *)bean;

@end

@implementation GameViewController

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
	[[MXiConnectionHandler sharedInstance] connectToMultiUserChatRoom:[_game.gameJid full] withDelegate:self];
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
	NSLog(@"Played Card %@", card.cardNumber);
}

- (IBAction)startGame:(UIButton *)startButton
{
	for (CardButton *card in _cardButtons) {
		[card setEnabled:YES];
	}
	startButton.enabled = NO;
	startButton.hidden = YES;
}

#pragma mark - MXiMUCDelegate
- (void)connectionToRoomEstablished:(NSString *)roomJID
{
	NSLog(@"%@", roomJID);
}

-(void)didReceiveMultiUserChatMessage:(NSString *)message fromUser:(NSString *)user publishedInRoom:(NSString *)roomJID
{
	NSError *error;
	NSXMLElement *messageBean = [[NSXMLElement alloc] initWithXMLString:message error:&error];
	if(!error) {
		if ([messageBean.name isEqualToString:[StartGameMessage elementName] ignoreCase:YES]) {
			StartGameMessage *start = [StartGameMessage new];
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
		}
	}
}

- (void)startGameMessageReceived:(StartGameMessage *)bean
{
	
}

- (void)cardPlayedMessageReceived:(CardPlayedMessage *)bean
{
	
}

- (void)roundCompleteMessageReceived:(RoundCompleteMessage *)bean
{
	
}
@end
