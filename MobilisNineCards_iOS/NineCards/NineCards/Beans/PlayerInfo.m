#import "PlayerInfo.h"

@implementation PlayerInfo

- (NSXMLElement* )toXML {
	NSXMLElement* beanElement = [NSXMLElement elementWithName:[[self class] elementName]
														xmlns:[[self class] iqNamespace]];

	NSXMLElement* idElement = [NSXMLElement elementWithName:@"id"];
	[idElement setStringValue:[self id]];
	[beanElement addChild:idElement];

	NSXMLElement* scoreElement = [NSXMLElement elementWithName:@"score"];
	[scoreElement setStringValue:[NSString stringWithFormat:@"%f", [[self score] floatValue]]];
	[beanElement addChild:scoreElement];

	for (NSNumber* usedcardsPart in [self usedcards]) {
		NSXMLElement* usedcardsElement = [NSXMLElement elementWithName:@"usedcards"];
		[usedcardsElement setStringValue:[NSString stringWithFormat:@"%f", [usedcardsPart floatValue]]];
		[beanElement addChild:usedcardsElement];
	}

	return beanElement;
}

- (void)fromXML:(NSXMLElement* )xml {
	NSXMLElement* idElement = [xml elementForName:@"id"];
	[self setId:[idElement stringValue]];

	NSXMLElement* scoreElement = [xml elementForName:@"score"];
	[self setScore:[NSNumber numberWithFloat:[[scoreElement stringValue] floatValue]]];

	[self setUsedcards:[NSMutableArray array]];
	NSArray* usedcardsElements = [xml elementsForName:@"usedcards"];
	for (NSXMLElement* usedcardsElement in usedcardsElements) {
		[[self usedcards] addObject:[NSNumber numberWithFloat:[[usedcardsElement stringValue] floatValue]]];
	}
}

+ (NSString* )elementName {
	return @"PlayerInfo";
}
@end