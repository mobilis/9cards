//
//  Player.m
//  NineCards
//
//  Created by Martin Weißbach on 11/12/13.
//  Copyright (c) 2013 Mobilis. All rights reserved.
//

#import "Player.h"

@interface Player ()

@property (strong, nonatomic, readwrite) NSString *name;
@property (nonatomic, readwrite) int score;

@end

@implementation Player

+ (instancetype)playerWithName:(NSString *)name
{
    return [[self alloc] initWithName:name];
}

- (instancetype)initWithName:(NSString *)name
{
    NSAssert(name != nil, @"Assertion failure. The players name must not be nil");
    NSAssert(![name isEqualToString:@""], @"Assertion failure. The players name must not be empty");
    self = [super init];
    if (self) {
        self.name = name;
        self.score = 0;
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

@end
