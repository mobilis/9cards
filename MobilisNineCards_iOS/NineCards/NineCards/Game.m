//
//  Game.m
//  NineCards
//
//  Created by Markus Wutzler on 17.10.13.
//  Copyright (c) 2013 Mobilis. All rights reserved.
//

#import "Game.h"

@implementation Game

- (instancetype)initWithName:(NSString *)gameName numberOfPlayers:(NSNumber *)players numberOfRounds:(NSNumber *)rounds andGameJid:(XMPPJID *)gameJid
{
	self = [self init];
	if (self) {
		self.name = gameName;
		self.players = players;
		self.rounds = rounds;
		self.gameJid = gameJid;
	}
	return self;
}

@end
