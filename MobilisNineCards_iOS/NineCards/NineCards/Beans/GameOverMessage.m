#import "GameOverMessage.h"

@implementation GameOverMessage

- (NSXMLElement* )toXML {
	NSXMLElement* beanElement = [NSXMLElement elementWithName:[[self class] elementName]
														xmlns:[[self class] iqNamespace]];

	NSXMLElement* winnerElement = [NSXMLElement elementWithName:@"winner"];
	[winnerElement setStringValue:[self winner]];
	[beanElement addChild:winnerElement];

	NSXMLElement* scoreElement = [NSXMLElement elementWithName:@"score"];
	[scoreElement setStringValue:[NSString stringWithFormat:@"%f", [[self score] floatValue]]];
	[beanElement addChild:scoreElement];

	for (PlayerInfo* playerInfosPart in [self playerInfos]) {
		NSXMLElement* playerInfosElement = [NSXMLElement elementWithName:@"playerInfos"];
		NSXMLElement* idElement = [NSXMLElement elementWithName:@"id"];
		[idElement setStringValue:[playerInfosPart id]];
		[playerInfosElement addChild:idElement];
		NSXMLElement* scoreElement = [NSXMLElement elementWithName:@"score"];
		[scoreElement setStringValue:[NSString stringWithFormat:@"%f", [[playerInfosPart score] floatValue]]];
		[playerInfosElement addChild:scoreElement];
		for (NSNumber* usedcardsPart in [playerInfosPart usedcards]) {
			NSXMLElement* usedcardsElement = [NSXMLElement elementWithName:@"usedcards"];
			[usedcardsElement setStringValue:[NSString stringWithFormat:@"%f", [usedcardsPart floatValue]]];
			[playerInfosElement addChild:usedcardsElement];
		}

		[beanElement addChild:playerInfosElement];
	}

	return beanElement;
}

- (void)fromXML:(NSXMLElement* )xml {
	NSXMLElement* winnerElement = [xml elementForName:@"winner"];
	[self setWinner:[winnerElement stringValue]];

	NSXMLElement* scoreElement = [xml elementForName:@"score"];
	[self setScore:[NSNumber numberWithFloat:[[scoreElement stringValue] floatValue]]];

	[self setPlayerInfos:[NSMutableArray array]];
	NSArray* playerInfosElements = [xml elementsForName:@"playerInfos"];
	for (NSXMLElement* playerInfosElement in playerInfosElements) {
		PlayerInfo *playerInfosObject = [[PlayerInfo alloc] init];
		NSXMLElement* idElement = [playerInfosElement elementForName:@"id"];
		[playerInfosObject setId:[idElement stringValue]];
		NSXMLElement* scoreElement = [playerInfosElement elementForName:@"score"];
		[playerInfosObject setScore:[NSNumber numberWithFloat:[[scoreElement stringValue] floatValue]]];
		[playerInfosObject setUsedcards:[NSMutableArray array]];
		NSArray* usedcardsElements = [playerInfosElement elementsForName:@"usedcards"];
		for (NSXMLElement* usedcardsElement in usedcardsElements) {
			[[playerInfosObject usedcards] addObject:[NSNumber numberWithFloat:[[usedcardsElement stringValue] floatValue]]];
		}
		[[self playerInfos] addObject:playerInfosObject];
	}
}

+ (NSString* )elementName {
	return @"GameOverMessage";
}

+ (NSString* )iqNamespace {
	return @"http://mobilis.inf.tu-dresden.de/apps/9cards";
}
@end