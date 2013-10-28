#import "RoundCompleteMessage.h"

@implementation RoundCompleteMessage

- (NSXMLElement* )toXML {
	NSXMLElement* beanElement = [NSXMLElement elementWithName:[[self class] elementName]
														xmlns:[[self class] iqNamespace]];

	NSXMLElement* roundElement = [NSXMLElement elementWithName:@"round"];
	[roundElement setStringValue:[NSString stringWithFormat:@"%f", [[self round] floatValue]]];
	[beanElement addChild:roundElement];

	NSXMLElement* winnerElement = [NSXMLElement elementWithName:@"winner"];
	[winnerElement setStringValue:[self winner]];
	[beanElement addChild:winnerElement];

	for (PlayerInfo* PlayerInfosPart in [self PlayerInfos]) {
		NSXMLElement* PlayerInfosElement = [NSXMLElement elementWithName:@"PlayerInfos"];
		NSXMLElement* idElement = [NSXMLElement elementWithName:@"id"];
		[idElement setStringValue:[PlayerInfosPart id]];
		[PlayerInfosElement addChild:idElement];
		NSXMLElement* scoreElement = [NSXMLElement elementWithName:@"score"];
		[scoreElement setStringValue:[NSString stringWithFormat:@"%f", [[PlayerInfosPart score] floatValue]]];
		[PlayerInfosElement addChild:scoreElement];
		for (NSNumber* usedcardsPart in [PlayerInfosPart usedcards]) {
			NSXMLElement* usedcardsElement = [NSXMLElement elementWithName:@"usedcards"];
			[usedcardsElement setStringValue:[NSString stringWithFormat:@"%f", [usedcardsPart floatValue]]];
			[PlayerInfosElement addChild:usedcardsElement];
		}

		[beanElement addChild:PlayerInfosElement];
	}

	return beanElement;
}

- (void)fromXML:(NSXMLElement* )xml {
	NSXMLElement* roundElement = [xml elementForName:@"round"];
	[self setRound:[NSNumber numberWithFloat:[[roundElement stringValue] floatValue]]];

	NSXMLElement* winnerElement = [xml elementForName:@"winner"];
	[self setWinner:[winnerElement stringValue]];

	[self setPlayerInfos:[NSMutableArray array]];
	NSArray* PlayerInfosElements = [xml elementsForName:@"PlayerInfos"];
	for (NSXMLElement* PlayerInfosElement in PlayerInfosElements) {
		PlayerInfo *PlayerInfosObject = [[PlayerInfo alloc] init];
		NSXMLElement* idElement = [PlayerInfosElement elementForName:@"id"];
		[PlayerInfosObject setId:[idElement stringValue]];
		NSXMLElement* scoreElement = [PlayerInfosElement elementForName:@"score"];
		[PlayerInfosObject setScore:[NSNumber numberWithFloat:[[scoreElement stringValue] floatValue]]];
		[PlayerInfosObject setUsedcards:[NSMutableArray array]];
		NSArray* usedcardsElements = [PlayerInfosElement elementsForName:@"usedcards"];
		for (NSXMLElement* usedcardsElement in usedcardsElements) {
			[[PlayerInfosObject usedcards] addObject:[NSNumber numberWithFloat:[[usedcardsElement stringValue] floatValue]]];
		}
		[[self PlayerInfos] addObject:PlayerInfosObject];
	}
}

+ (NSString* )elementName {
	return @"RoundCompleteMessage";
}

+ (NSString* )iqNamespace {
	return @"http://mobilis.inf.tu-dresden.de/MobilisNineCards";
}
@end