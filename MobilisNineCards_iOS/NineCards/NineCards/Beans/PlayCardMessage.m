#import "PlayCardMessage.h"

@implementation PlayCardMessage

- (NSXMLElement* )toXML {
	NSXMLElement* beanElement = [NSXMLElement elementWithName:[[self class] elementName]
														xmlns:[[self class] iqNamespace]];

	NSXMLElement* roundElement = [NSXMLElement elementWithName:@"round"];
	[roundElement setStringValue:[NSString stringWithFormat:@"%d", [[self round] intValue]]];
	[beanElement addChild:roundElement];

	NSXMLElement* cardElement = [NSXMLElement elementWithName:@"card"];
	[cardElement setStringValue:[NSString stringWithFormat:@"%d", [[self card] intValue]]];
	[beanElement addChild:cardElement];

	return beanElement;
}

- (void)fromXML:(NSXMLElement* )xml {
	NSXMLElement* roundElement = [xml elementForName:@"round"];
	[self setRound:[NSNumber numberWithFloat:[[roundElement stringValue] intValue]]];

	NSXMLElement* cardElement = [xml elementForName:@"card"];
	[self setCard:[NSNumber numberWithFloat:[[cardElement stringValue] intValue]]];
}

+ (NSString* )elementName {
	return @"PlayCardMessage";
}

+ (NSString* )iqNamespace {
	return @"http://mobilis.inf.tu-dresden.de/apps/9cards";
}
@end