//
//  Game.h
//  NineCards
//
//  Created by Markus Wutzler on 17.10.13.
//  Copyright (c) 2013 Mobilis. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <XMPPFramework/XMPPJID.h>

@interface Game : NSObject

@property (retain) NSString *name;
@property (retain) NSNumber *players;
@property (retain) NSNumber *rounds;
@property (retain) XMPPJID *gameJid;

- (instancetype) initWithName:(NSString *)gameName numberOfPlayers:(NSNumber *)players numberOfRounds:(NSNumber *)rounds andGameJid:(XMPPJID *)gameJid;

@end
