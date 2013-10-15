#import "ConfigureGameResponse.h"

@implementation ConfigureGameResponse

- (id)init {
	self = [super initWithBeanType:RESULT];

	return self;
}

- (void)fromXML:(NSXMLElement* )xml {
}

+ (NSString* )elementName {
	return @"ConfigureGameResponse";
}

+ (NSString* )iqNamespace {
	return @"mobilisninecards:iq:configuregame";
}

@end