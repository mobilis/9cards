//
//  Player.h
//  NineCards
//
//  Created by Martin Weißbach on 11/12/13.
//  Copyright (c) 2013 Mobilis. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface Player : NSObject

@property (strong, nonatomic, readonly) NSString *name;
@property (nonatomic, readonly) int score;

+ (instancetype)playerWithName:(NSString *)name;
- (instancetype)initWithName:(NSString *)name;

- (void)addScorePoints:(int)points;
- (void)setScorePoints:(int)points;

@end
