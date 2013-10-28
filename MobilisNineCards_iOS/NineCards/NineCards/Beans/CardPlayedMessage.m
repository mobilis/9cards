#import "CardPlayedMessage.h"

@implementation CardPlayedMessage

- (NSXMLElement* )toXML {
	NSXMLElement* beanElement = [NSXMLElement elementWithName:[[self class] elementName]
														xmlns:[[self class] iqNamespace]];

	NSXMLElement* roundElement = [NSXMLElement elementWithName:@"round"];
	[roundElement setStringValue:[NSString stringWithFormat:@"%f", [[self round] floatValue]]];
	[beanElement addChild:roundElement];

	NSXMLElement* playerElement = [NSXMLElement elementWithName:@"player"];
	[playerElement setStringValue:[self player]];
	[beanElement addChild:playerElement];

	return beanElement;
}

- (void)fromXML:(NSXMLElement* )xml {
	NSXMLElement* roundElement = [xml elementForName:@"round"];
	[self setRound:[NSNumber numberWithFloat:[[roundElement stringValue] floatValue]]];

	NSXMLElement* playerElement = [xml elementForName:@"player"];
	[self setPlayer:[playerElement stringValue]];
}

+ (NSString* )elementName {
	return @"CardPlayedMessage";
}

+ (NSString* )iqNamespace {
	return @"http://mobilis.inf.tu-dresden.de/MobilisNineCards";
}
@end