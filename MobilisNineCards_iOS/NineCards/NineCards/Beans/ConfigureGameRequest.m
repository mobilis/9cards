#import "ConfigureGameRequest.h"

@implementation ConfigureGameRequest

- (id)init {
	self = [super initWithBeanType:SET];

	return self;
}

- (NSXMLElement* )toXML {
	NSXMLElement* beanElement = [NSXMLElement elementWithName:[[self class] elementName]
														xmlns:[[self class] iqNamespace]];

	NSXMLElement* gamenameElement = [NSXMLElement elementWithName:@"gamename"];
	[gamenameElement setStringValue:[self gamename]];
	[beanElement addChild:gamenameElement];

	NSXMLElement* playersElement = [NSXMLElement elementWithName:@"players"];
	[playersElement setStringValue:[NSString stringWithFormat:@"%f", [[self players] floatValue]]];
	[beanElement addChild:playersElement];

	NSXMLElement* roundsElement = [NSXMLElement elementWithName:@"rounds"];
	[roundsElement setStringValue:[NSString stringWithFormat:@"%f", [[self rounds] floatValue]]];
	[beanElement addChild:roundsElement];

	return beanElement;
}

+ (NSString* )elementName {
	return @"ConfigureGameRequest";
}

+ (NSString* )iqNamespace {
	return @"mobilisninecards:iq:configuregame";
}

@end