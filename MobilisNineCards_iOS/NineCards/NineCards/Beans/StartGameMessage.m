#import "StartGameMessage.h"

@implementation StartGameMessage

- (NSXMLElement* )toXML {
	NSXMLElement* beanElement = [NSXMLElement elementWithName:[[self class] elementName]
														xmlns:[[self class] iqNamespace]];

	return beanElement;
}

- (void)fromXML:(NSXMLElement* )xml {
}

+ (NSString* )elementName {
	return @"StartGameMessage";
}

+ (NSString* )iqNamespace {
	return @"http://mobilis.inf.tu-dresden.de/apps/9cards";
}
@end