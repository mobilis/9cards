#import "ConfigureGameRequest.h"

@implementation ConfigureGameRequest

- (id)init {
	self = [super initWithBeanType:SET];

	return self;
}

- (NSXMLElement* )toXML {
	NSXMLElement* beanElement = [NSXMLElement elementWithName:[[self class] elementName]
														xmlns:[[self class] iqNamespace]];

	NSXMLElement* playersElement = [NSXMLElement elementWithName:@"players"];
	[playersElement setStringValue:[NSString stringWithFormat:@"%d", [[self players] intValue]]];
	[beanElement addChild:playersElement];

	NSXMLElement* roundsElement = [NSXMLElement elementWithName:@"rounds"];
	[roundsElement setStringValue:[NSString stringWithFormat:@"%d", [[self rounds] intValue]]];
	[beanElement addChild:roundsElement];

	return beanElement;
}

+ (NSString* )elementName {
	return @"ConfigureGameRequest";
}

+ (NSString* )iqNamespace {
	return @"http://mobilis.inf.tu-dresden.de/apps/9cards";
}

@end