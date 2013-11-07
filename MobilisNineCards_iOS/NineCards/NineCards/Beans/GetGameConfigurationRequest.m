#import "GetGameConfigurationRequest.h"

@implementation GetGameConfigurationRequest

- (id)init {
	self = [super initWithBeanType:SET];

	return self;
}

- (NSXMLElement* )toXML {
	NSXMLElement* beanElement = [NSXMLElement elementWithName:[[self class] elementName]
														xmlns:[[self class] iqNamespace]];

	return beanElement;
}

+ (NSString* )elementName {
	return @"GetGameConfigurationRequest";
}

+ (NSString* )iqNamespace {
	return @"http://mobilis.inf.tu-dresden.de/apps/9cards";
}

@end