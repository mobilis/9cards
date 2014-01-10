//
//  Player.h
//  NineCards
//
//  Created by Martin Weißbach on 11/12/13.
//  Copyright (c) 2013 Mobilis. All rights reserved.
//

#import <Foundation/Foundation.h>

@class XMPPJID;

@interface Player : NSObject

@property (strong, nonatomic, readonly) XMPPJID *jid;
@property (nonatomic, readonly) int score;
@property (nonatomic, readonly) NSArray *cardsPlayed;

+ (instancetype)playerWithJid:(XMPPJID *)jid;
- (instancetype)initWithJid:(XMPPJID *)jid;

- (void)addScorePoints:(int)points;
- (void)setScorePoints:(int)points;

- (void)setCardPlayed:(NSDictionary *)cardDictionary atIndex:(NSUInteger)index;
- (void)setCardsPlayed:(NSArray *)cardsPlayed;

@end