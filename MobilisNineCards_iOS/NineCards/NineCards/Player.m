//
//  Player.m
//  NineCards
//
//  Created by Martin Weißbach on 11/12/13.
//  Copyright (c) 2013 Mobilis. All rights reserved.
//

#import "Player.h"

#import "XMPPJID.h"

@interface Player ()

@property (nonatomic, readwrite) XMPPJID *jid;
@property (nonatomic, readwrite) int score;
@property (nonatomic, readwrite) NSArray *cardsPlayed;

@end

@implementation Player

+ (instancetype)playerWithJid:(XMPPJID *)jid
{
    return [[self alloc] initWithJid:jid];
}

- (instancetype)initWithJid:(XMPPJID *)jid
{
    NSAssert(jid != nil, @"Assertion failure. The players name must not be nil");
    self = [super init];
    if (self) {
        self.jid = jid;
        self.score = 0;
        self.cardsPlayed = [[NSMutableArray arrayWithCapacity:9] copy];
    }
    return self;
}

- (void)addScorePoints:(int)points
{
    self.score = self.score + points;
}

- (void)setScorePoints:(int)points
{
    self.score = points;
}

- (void)setCardPlayed:(NSDictionary *)cardDictionary atIndex:(NSUInteger)index
{
    NSMutableArray *tempArray = [NSMutableArray arrayWithArray:_cardsPlayed];
    [tempArray insertObject:cardDictionary atIndex:index];
    self.cardsPlayed = [NSArray arrayWithArray:tempArray];
}

- (void)setCardsPlayed:(NSArray *)cardsPlayed
{
    if (cardsPlayed == nil) return;
    
    _cardsPlayed = cardsPlayed;
}

- (BOOL)isEqual:(id)object
{
    if (![object isKindOfClass:[Player class]]) {
        return NO;
    }
    
    Player *otherObject = (Player *)object;
    if ([otherObject.jid isEqualToJID:self.jid]) {
        return YES;
    }
    
    return NO;
}

- (NSUInteger)hash
{
    return [self.jid hash];
}

@end
