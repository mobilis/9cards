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
#import "PlayerListCardCell.h"
#import "PlayerListSectionHeader.h"
#import "Player.h"

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

typedef enum {
	CardPlayedWaiting,
	CardPlayedSuccess,
	CardPlayedWinner
} CardPlayedState;

@interface GameViewController () <MXiMultiUserChatDelegate, UICollectionViewDataSource, UICollectionViewDelegate>

@property (strong, nonatomic) IBOutletCollection(UIButton) NSArray *cardButtons;
@property (weak, nonatomic) IBOutlet UIButton *startButton;
@property (weak, nonatomic) IBOutlet UICollectionView *playerStatsView;
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
	NSMutableArray *_players;
    
    __strong UIView *_waitingView;
    __strong UIPopoverController *_pointsPopOverView;
    __strong GamePointsViewController *_gamePointsViewController;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	for (UIButton *cardButton in self.cardButtons) {
		cardButton.enabled = NO;
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
	
	[self setupWaitingView];
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

- (void)viewWillDisappear:(BOOL)animated
{
    [[MXiConnectionHandler sharedInstance] removeDelegate:self withSelector:@selector(gameConfigurationReceived:) forBeanClass:[GetGameConfigurationResponse class]];
	[super viewWillDisappear:animated];
}

- (IBAction)quitGame:(UIBarButtonItem *)sender
{
	[self.navigationController popToRootViewControllerAnimated:YES];
}

- (IBAction)cardPlayed:(CardButton *)card {
	for (CardButton *card in _cardButtons) {
		[card setEnabled:NO];
	}
    PlayCardMessage *play = [PlayCardMessage new];
    play.card = card.cardNumber;
    play.round = _currentRound;
    
    [[MXiConnectionHandler sharedInstance] sendMessageString:[[play toXML] XMLString] toJID:[[_game gameJid] full]];
	
	[UIView transitionWithView:card duration:0.5f options:UIViewAnimationOptionTransitionFlipFromTop animations:^{
		card.alpha = 0.f;
	} completion:^(BOOL finished) {
		if(finished) {
			card.hidden = YES;
			card.userInteractionEnabled = NO;
		}
	}];
	
    [self showWaitingView];
    NSLog(@"%@", [[play toXML] XMLString]);
}

- (IBAction)startGame:(UIButton *)startButton
{
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
- (void)connectionToRoomEstablished:(NSString *)roomJID usingRoomJID:(NSString *)myRoomJID
{
    if (!_players) {
        _players = [NSMutableArray arrayWithCapacity:[_game.players unsignedIntegerValue]];
    }
    [_players addObject:[Player playerWithJid:[XMPPJID jidWithString:myRoomJID]]];
    NSLog(@"ConnectionToRoomEstablished: %@", roomJID);
}

- (void)userWithJid:(NSString *)fullJid didJoin:(NSString *)presence room:(NSString *)roomJid
{
    XMPPJID *userJid = [XMPPJID jidWithString:fullJid];
    if([userJid.resource isEqualToString:@"9Cards-Service" ignoreCase:YES]) return;
    
    NSLog(@"User: %@/%@ didJoin: %@", userJid.user, userJid.resource, presence);
    if (!_players) {
        _players = [NSMutableArray arrayWithCapacity:[_game.players unsignedIntegerValue]];
    }
    if (![_players containsObject:[Player playerWithJid:userJid]]) {
        [_players addObject:[Player playerWithJid:userJid]];
        [self.playerStatsView performSelectorOnMainThread:@selector(reloadData) withObject:nil waitUntilDone:NO];
    }
}

- (void)userWithJid:(NSString *)fullJid didLeaveRoom:(NSString *)roomJid
{
    XMPPJID *userJid = [XMPPJID jidWithString:fullJid];
    NSLog(@"User: %@/%@ didLeave", userJid.user, userJid.resource);
    if (!_players) {
        return;
    }
    if ([_players containsObject:[Player playerWithJid:userJid]]) {
        [_players removeObject:[Player playerWithJid:userJid]];
        [self.playerStatsView performSelectorOnMainThread:@selector(reloadData) withObject:nil waitUntilDone:NO];
    }
}

-(void)didReceiveMultiUserChatMessage:(NSString *)message fromUser:(NSString *)user publishedInRoom:(NSString *)roomJID
{
	NSError *error;
	NSXMLElement *messageBean = [[NSXMLElement alloc] initWithXMLString:message error:&error];
	if(!error) {
		NSLog(@"Bean: %@", messageBean.name);
		if ([messageBean.name isEqualToString:[GameStartsMessage elementName] ignoreCase:YES]) {
			GameStartsMessage *start = [GameStartsMessage new];
			[start fromXML:messageBean];
			[self performSelectorOnMainThread:@selector(startGameMessageReceived:) withObject:start waitUntilDone:NO];
		} else if ([messageBean.name isEqualToString:[CardPlayedMessage elementName] ignoreCase:YES]) {
			CardPlayedMessage *card = [CardPlayedMessage new];
			[card fromXML:messageBean];
			[self performSelectorOnMainThread:@selector(cardPlayedMessageReceived:) withObject:card waitUntilDone:NO];
		} else if ([messageBean.name isEqualToString:[RoundCompleteMessage elementName] ignoreCase:YES]) {
			RoundCompleteMessage *round = [RoundCompleteMessage new];
			[round fromXML:messageBean];
			[self performSelectorOnMainThread:@selector(roundCompleteMessageReceived:) withObject:round waitUntilDone:NO];
		} else if ([messageBean.name isEqualToString:[GameOverMessage elementName] ignoreCase:YES]) {
			GameOverMessage *gameOver = [GameOverMessage new];
			[gameOver fromXML:messageBean];
			[self performSelectorOnMainThread:@selector(gameOverMessageReceived:) withObject:gameOver waitUntilDone:NO];
		} else {
			NSLog(@"Message %@ from User %@ in room %@ wasn't processed.", message, user, roomJID);
		}
	}
}

- (void)startGameMessageReceived:(GameStartsMessage *)bean
{
	if (!_gameStarted) {
		_gameStarted = YES;
		for (CardButton *card in _cardButtons) {
			[card setEnabled:YES];
		}
		self.playerStatsView.hidden = NO;
		_currentRound = @1;
	}
}

- (void)cardPlayedMessageReceived:(CardPlayedMessage *)bean
{
	if(_gameStarted && [bean.round isEqualToNumber:_currentRound]) {
//		[[_players objectForKey:[XMPPJID jidWithString:bean.player]] insertObject:@{@"card": @-1} atIndex:bean.round.unsignedIntegerValue -1];
		[_playerStatsView reloadData];
	}
}

- (void)roundCompleteMessageReceived:(RoundCompleteMessage *)bean
{
	if (_gameStarted) {
		_currentRound = [NSNumber numberWithInt:[_currentRound intValue]+1];
		for (PlayerInfo *pInfo in bean.playerInfos) {
            for (Player *player in _players) {
                if ([player.jid isEqualToJID:[XMPPJID jidWithString:pInfo.id]]) {
                    [player setScorePoints:[pInfo.score intValue]];
                    [player setCardPlayed:@{    @"card": pInfo.usedcards.lastObject,
                                                @"winner": ([pInfo.id isEqualToString:bean.winner] ? @YES : @NO)
                                            }
                                  atIndex:bean.round.unsignedIntegerValue - 1];
                }
            }
            NSLog(@"Test");
		}
		[_playerStatsView reloadData];
        [self hideWaitingView];
		for (CardButton *card in _cardButtons) {
			[card setEnabled:YES];
		}
	}
}

- (void)gameOverMessageReceived:(GameOverMessage *)bean
{
    [self hideWaitingView];
    UIAlertView *alertView = [[UIAlertView alloc] initWithTitle:@"Game finished."
                                                        message:[NSString stringWithFormat:@"Player %@ won with %i points", bean.winner, [bean.score intValue]]
                                                       delegate:self
                                              cancelButtonTitle:nil
                                              otherButtonTitles:@"OK", nil];
    [alertView show];
}
- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
    [self.navigationController popToRootViewControllerAnimated:YES];
}

- (void)gameConfigurationReceived:(GetGameConfigurationResponse *)response
{
    self.game = [Game new];
    self.game.players = response.maxPlayers;
    self.game.rounds = response.maxRounds;
	self.game.gameJid = response.from;
    
    [[MXiConnectionHandler sharedInstance] connectToMultiUserChatRoom:response.muc withDelegate:self];
}

- (void)showWaitingView
{
	[_waitingView setHidden:NO];
}
- (void)setupWaitingView
{
    _waitingView = [[UIView alloc] initWithFrame:self.view.frame];
    _waitingView.backgroundColor = [UIColor clearColor];
    _waitingView.exclusiveTouch = YES;
    
    UILabel *waitingForOthersLabel = [[UILabel alloc] initWithFrame:CGRectZero];
    waitingForOthersLabel.textAlignment = NSTextAlignmentCenter;
    waitingForOthersLabel.text = @"Waiting for other players to finish.";
    waitingForOthersLabel.textColor = [UIColor whiteColor];
    waitingForOthersLabel.font = [UIFont boldSystemFontOfSize:20.0];
    [waitingForOthersLabel setTranslatesAutoresizingMaskIntoConstraints:NO];
	waitingForOthersLabel.userInteractionEnabled = NO;
    
    UIView *coloredBackgroundView = [[UIView alloc] initWithFrame:CGRectMake(0.0, 0.0, self.view.frame.size.width, 200.0)];
    coloredBackgroundView.backgroundColor = [UIColor colorWithRed:(15.0/255.0) green:(101.0/255) blue:(255.0/255.0) alpha:1.0];
    coloredBackgroundView.layer.cornerRadius = 5.0;
	coloredBackgroundView.userInteractionEnabled = NO;
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
	
	_waitingView.hidden = YES;
	[self.view addSubview:_waitingView];
}

- (void)hideWaitingView
{
	_waitingView.hidden = YES;
}

#pragma mark - UICollectionViewDataSource
- (NSInteger)numberOfSectionsInCollectionView:(UICollectionView *)collectionView
{
	return [_players count];
}

- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section
{
    return ((Player *)[_players objectAtIndex:section]).cardsPlayed.count;
}

- (UICollectionReusableView *)collectionView:(UICollectionView *)collectionView viewForSupplementaryElementOfKind:(NSString *)kind atIndexPath:(NSIndexPath *)indexPath
{
	PlayerListSectionHeader *view = [collectionView dequeueReusableSupplementaryViewOfKind:kind withReuseIdentifier:@"CollectionViewSectionHeader" forIndexPath:indexPath];
	if (!view) {
		view = [[PlayerListSectionHeader alloc] initWithFrame:CGRectMake(0.f, 0.f, 320.f, 38.f)];
	}
    view.userNameLabel.text = [((Player *)[_players objectAtIndex:indexPath.section]).jid resource];
    view.scoreLabel.text = [NSString stringWithFormat:@"%i", ((Player *)[_players objectAtIndex:indexPath.section]).score];
	return view;
}

- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath
{
	PlayerListCardCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:@"CollectionViewCardCell" forIndexPath:indexPath];
    if ([[[((Player *)[_players objectAtIndex:indexPath.section]).cardsPlayed objectAtIndex:indexPath.item] objectForKey:@"winner"] boolValue] == NO) {
        cell.cardLabel.textColor = [UIColor redColor];
    }
    cell.cardLabel.text = [[[((Player *)[_players objectAtIndex:indexPath.section]).cardsPlayed objectAtIndex:indexPath.item] objectForKey:@"card"] stringValue];
	return cell;
}


@end
