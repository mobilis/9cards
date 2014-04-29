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
#import "PlayerInfo.h"

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
@property (weak, nonatomic) IBOutlet UIView *cardsView;
- (IBAction)quitGame:(UIBarButtonItem *)sender;
- (IBAction)cardPlayed:(CardButton *)card;
- (IBAction)startGame:(UIButton *)startButton;

- (void) startGameMessageReceived:(GameStartsMessage *)bean;
- (void) cardPlayedMessageReceived:(CardPlayedMessage *)bean;
- (void) roundCompleteMessageReceived:(RoundCompleteMessage *)bean;
- (void) gameOverMessageReceived:(GameOverMessage *)bean;

- (void)showWaitingView;
- (void)hideWaitingView;

@end

@implementation GameViewController {
	BOOL _gameStarted;
	NSNumber *_currentRound;
	NSMutableArray *_players;
    
    __strong UIView *_waitingView;
    
    BOOL _initialLandscape;
    __strong NSMutableArray *_portraitConstraints;
    __strong NSMutableArray *_landscapeConstraints;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	for (UIButton *cardButton in self.cardButtons) {
		cardButton.enabled = NO;
	}
	_gameStarted = NO;
    if ([self.game hasGameConfiguration]) {
        [[MXiConnectionHandler sharedInstance].connection connectToMultiUserChatRoom:[_game roomJid].bare withDelegate:self];
        [[MXiConnectionHandler sharedInstance].connection addBeanDelegate:self
                                                             withSelector:@selector(cardPlayedMessageReceived:)
                                                             forBeanClass:[CardPlayedMessage class]];
        [[MXiConnectionHandler sharedInstance].connection addBeanDelegate:self
                                                             withSelector:@selector(gameOverMessageReceived:)
                                                             forBeanClass:[GameOverMessage class]];
        [[MXiConnectionHandler sharedInstance].connection addBeanDelegate:self
                                                             withSelector:@selector(roundCompleteMessageReceived:)
                                                             forBeanClass:[RoundCompleteMessage class]];
        [[MXiConnectionHandler sharedInstance].connection addBeanDelegate:self
                                                             withSelector:@selector(startGameMessageReceived:)
                                                             forBeanClass:[GameStartsMessage class]];
    } else {
        [[MXiConnectionHandler sharedInstance].connection addBeanDelegate:self
                                                             withSelector:@selector(gameConfigurationReceived:)
                                                             forBeanClass:[GetGameConfigurationResponse class]];
        GetGameConfigurationRequest *gameConfigurationRequest = [GetGameConfigurationRequest new];
        gameConfigurationRequest.to = self.game.gameJid;
        [[MXiConnectionHandler sharedInstance].connection sendBean:gameConfigurationRequest];
        self.startButton.hidden = YES;
    }
    
    GamePointsViewController *gamePointsViewController = [[GamePointsViewController alloc] initWithNibName:@"GamePointsView"
                                                                                                    bundle:nil];
    [self addChildViewController:gamePointsViewController];
	   
	[self setupWaitingView];
    if (UIInterfaceOrientationIsLandscape(self.interfaceOrientation)) {
        _initialLandscape = YES;
        [self didRotateFromInterfaceOrientation:UIInterfaceOrientationPortrait];
    } else _initialLandscape = NO;
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
    CGRect newFrame = CGRectMake(0, 0, self.view.frame.size.height, self.view.frame.size.width);
    [UIView animateWithDuration:duration animations:^{
        _waitingView.frame = newFrame;
    }];
    
    [super willRotateToInterfaceOrientation:toInterfaceOrientation duration:duration];
}

- (void)didRotateFromInterfaceOrientation:(UIInterfaceOrientation)fromInterfaceOrientation
{
    [super didRotateFromInterfaceOrientation:fromInterfaceOrientation];
    if (UIInterfaceOrientationIsPortrait(fromInterfaceOrientation)) {
        if (!_portraitConstraints) {
            _portraitConstraints = [NSMutableArray arrayWithCapacity:self.view.constraints.count];
            for (NSLayoutConstraint *layoutConstraint in self.view.constraints) {
                if (layoutConstraint.firstItem == self.playerStatsView || layoutConstraint.secondItem == self.playerStatsView)
                    [_portraitConstraints addObject:layoutConstraint];
                if (layoutConstraint.firstItem == self.cardsView || layoutConstraint.secondItem == self.cardsView)
                    [_portraitConstraints addObject:layoutConstraint];
            }
        }
        [self.view removeConstraints:_portraitConstraints];
        
        if (!_landscapeConstraints) {
            _landscapeConstraints = [NSMutableArray arrayWithCapacity:10];
            [_landscapeConstraints addObject:[NSLayoutConstraint constraintWithItem:self.playerStatsView
                                                                          attribute:NSLayoutAttributeTop
                                                                          relatedBy:NSLayoutRelationEqual
                                                                             toItem:self.startButton
                                                                          attribute:NSLayoutAttributeBottom
                                                                         multiplier:1.0
                                                                           constant:8.0]];
            [_landscapeConstraints addObject:[NSLayoutConstraint constraintWithItem:self.playerStatsView
                                                                          attribute:NSLayoutAttributeLeading
                                                                          relatedBy:NSLayoutRelationEqual
                                                                             toItem:self.cardsView
                                                                          attribute:NSLayoutAttributeRight
                                                                         multiplier:1.0
                                                                           constant:20.0]];
            [_landscapeConstraints addObject:[NSLayoutConstraint constraintWithItem:self.playerStatsView
                                                                          attribute:NSLayoutAttributeHeight
                                                                          relatedBy:NSLayoutRelationEqual
                                                                             toItem:self.cardsView
                                                                          attribute:NSLayoutAttributeHeight
                                                                         multiplier:1.0
                                                                           constant:0.0]];
            CGFloat constant = 0.f;
            if (_initialLandscape) {
                constant = (self.view.frame.size.height-self.cardsView.frame.size.width-self.playerStatsView.frame.size.width)/2.0;
            } else {
                constant = (self.view.frame.size.width-self.cardsView.frame.size.width-self.playerStatsView.frame.size.width)/2.0;
            }
            [_landscapeConstraints addObject:[NSLayoutConstraint constraintWithItem:self.cardsView
                                                                          attribute:NSLayoutAttributeLeading
                                                                          relatedBy:NSLayoutRelationEqual
                                                                             toItem:self.view
                                                                          attribute:NSLayoutAttributeLeft
                                                                         multiplier:1.0
                                                                           constant:constant]];
            
        }
        [self.view addConstraints:_landscapeConstraints];
        [self.view needsUpdateConstraints];
    } else {
        if (_landscapeConstraints) {
            [self.view removeConstraints:_landscapeConstraints];
        }
        if (_portraitConstraints) {
            [self.view addConstraints:_portraitConstraints];
        }
        [self.view setNeedsUpdateConstraints];
    }
}

- (NSString *)title
{
    return self.game.name;
}

- (void)viewWillDisappear:(BOOL)animated
{
    [[MXiConnectionHandler sharedInstance].connection removeBeanDelegate:self forBeanClass:[GetGameConfigurationResponse class]];
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
    
    NSLog(@"User: %@/%@ didJoin: %@", userJid.user, userJid.resource, roomJid);
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
//	NSError *error;
//	NSXMLElement *messageBean = [[NSXMLElement alloc] initWithXMLString:message error:&error];
//	if(!error) {
//		NSLog(@"Bean: %@", messageBean.name);
//		if ([messageBean.name isEqualToString:[GameStartsMessage elementName] ignoreCase:YES]) {
//			GameStartsMessage *start = [GameStartsMessage new];
//			[start fromXML:messageBean];
//			[self performSelectorOnMainThread:@selector(startGameMessageReceived:) withObject:start waitUntilDone:NO];
//		} else if ([messageBean.name isEqualToString:[CardPlayedMessage elementName] ignoreCase:YES]) {
//			CardPlayedMessage *card = [CardPlayedMessage new];
//			[card fromXML:messageBean];
//			[self performSelectorOnMainThread:@selector(cardPlayedMessageReceived:) withObject:card waitUntilDone:NO];
//		} else if ([messageBean.name isEqualToString:[RoundCompleteMessage elementName] ignoreCase:YES]) {
//			RoundCompleteMessage *round = [RoundCompleteMessage new];
//			[round fromXML:messageBean];
//			[self performSelectorOnMainThread:@selector(roundCompleteMessageReceived:) withObject:round waitUntilDone:NO];
//		} else if ([messageBean.name isEqualToString:[GameOverMessage elementName] ignoreCase:YES]) {
//			GameOverMessage *gameOver = [GameOverMessage new];
//			[gameOver fromXML:messageBean];
//			[self performSelectorOnMainThread:@selector(gameOverMessageReceived:) withObject:gameOver waitUntilDone:NO];
//		} else {
//			NSLog(@"Message %@ from User %@ in room %@ wasn't processed.", message, user, roomJID);
//		}
//	}
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
    
    [[MXiConnectionHandler sharedInstance].connection connectToMultiUserChatRoom:response.muc withDelegate:self];
}

- (void)showWaitingView
{
	[_waitingView setHidden:NO];
}
- (void)setupWaitingView
{
    CGRect frame = CGRectZero;
    if (UIInterfaceOrientationIsPortrait(self.interfaceOrientation)) {
        frame = self.view.frame;
    } else {
        frame = CGRectMake(0.f, 0.f, self.view.frame.size.height, self.view.frame.size.width);
    }
    _waitingView = [[UIView alloc] initWithFrame:frame];
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
    if (![[[((Player *)[_players objectAtIndex:indexPath.section]).cardsPlayed objectAtIndex:indexPath.item] objectForKey:@"winner"] boolValue]) {
        cell.cardLabel.textColor = [UIColor redColor];
    }
    cell.cardLabel.text = [[[((Player *)[_players objectAtIndex:indexPath.section]).cardsPlayed objectAtIndex:indexPath.item] objectForKey:@"card"] stringValue];
	return cell;
}


@end
