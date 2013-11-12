//
//  GamePointsViewController.h
//  NineCards
//
//  Created by Martin Weißbach on 11/12/13.
//  Copyright (c) 2013 Mobilis. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface GamePointsViewController : UITableViewController

- (void)addPlayer:(NSString *)player;
- (void)removePlayer:(NSString *)player;

- (NSArray *)allPlayers;

- (void)updatePlayer:(NSString *)player withScore:(int)score;
- (void)updatePlayersWithPlayerInfos:(NSArray *)playerInfos;

@end
