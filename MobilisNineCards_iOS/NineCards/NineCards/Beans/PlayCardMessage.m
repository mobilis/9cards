#import "PlayCardMessage.h"

@implementation PlayCardMessage

- (NSXMLElement* )toXML {
	NSXMLElement* beanElement = [NSXMLElement elementWithName:[[self class] elementName]
														xmlns:[[self class] iqNamespace]];

	NSXMLElement* roundElement = [NSXMLElement elementWithName:@"round"];
	[roundElement setStringValue:[NSString stringWithFormat:@"%f", [[self round] floatValue]]];
	[beanElement addChild:roundElement];

	NSXMLElement* cardElement = [NSXMLElement elementWithName:@"card"];
	[cardElement setStringValue:[NSString stringWithFormat:@"%f", [[self card] floatValue]]];
	[beanElement addChild:cardElement];

	return beanElement;
}

- (void)fromXML:(NSXMLElement* )xml {
	NSXMLElement* roundElement = [xml elementForName:@"round"];
	[self setRound:[NSNumber numberWithFloat:[[roundElement stringValue] floatValue]]];

	NSXMLElement* cardElement = [xml elementForName:@"card"];
	[self setCard:[NSNumber numberWithFloat:[[cardElement stringValue] floatValue]]];
}

+ (NSString* )elementName {
	return @"PlayCardMessage";
}

+ (NSString* )iqNamespace {
	return @"http://mobilis.inf.tu-dresden.de/MobilisNineCards";
}
@end