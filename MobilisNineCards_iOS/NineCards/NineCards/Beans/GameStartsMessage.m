#import "GameStartsMessage.h"

@implementation GameStartsMessage

- (NSXMLElement* )toXML {
	NSXMLElement* beanElement = [NSXMLElement elementWithName:[[self class] elementName]
														xmlns:[[self class] iqNamespace]];

	NSXMLElement* roundsElement = [NSXMLElement elementWithName:@"rounds"];
	[roundsElement setStringValue:[NSString stringWithFormat:@"%f", [[self rounds] floatValue]]];
	[beanElement addChild:roundsElement];

	return beanElement;
}

- (void)fromXML:(NSXMLElement* )xml {
	NSXMLElement* roundsElement = [xml elementForName:@"rounds"];
	[self setRounds:[NSNumber numberWithFloat:[[roundsElement stringValue] floatValue]]];
}

+ (NSString* )elementName {
	return @"GameStartsMessage";
}

+ (NSString* )iqNamespace {
	return @"http://mobilis.inf.tu-dresden.de/apps/9cards";
}
@end