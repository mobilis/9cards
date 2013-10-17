#import "StartGameMessage.h"

@implementation StartGameMessage

- (NSXMLElement* )toXML {
	NSXMLElement* beanElement = [NSXMLElement elementWithName:[[self class] elementName]
														xmlns:[[self class] iqNamespace]];

	NSXMLElement* roundsElement = [NSXMLElement elementWithName:@"rounds"];
	[roundsElement setStringValue:[NSString stringWithFormat:@"%f", [[self rounds] floatValue]]];
	[beanElement addChild:roundsElement];

	NSXMLElement* passwordElement = [NSXMLElement elementWithName:@"password"];
	[passwordElement setStringValue:[self password]];
	[beanElement addChild:passwordElement];

	return beanElement;
}

- (void)fromXML:(NSXMLElement* )xml {
	NSXMLElement* roundsElement = [xml elementForName:@"rounds"];
	[self setRounds:[NSNumber numberWithFloat:[[roundsElement stringValue] floatValue]]];

	NSXMLElement* passwordElement = [xml elementForName:@"password"];
	[self setPassword:[passwordElement stringValue]];
}

+ (NSString* )elementName {
	return @"StartGameMessage";
}
@end