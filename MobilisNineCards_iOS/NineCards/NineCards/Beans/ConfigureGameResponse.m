#import "ConfigureGameResponse.h"

@implementation ConfigureGameResponse

- (id)init {
	self = [super initWithBeanType:RESULT];

	return self;
}

- (void)fromXML:(NSXMLElement* )xml {
	NSXMLElement* mucElement = [xml elementForName:@"muc"];
	[self setMuc:[mucElement stringValue]];
}

+ (NSString* )elementName {
	return @"ConfigureGameResponse";
}

+ (NSString* )iqNamespace {
	return @"http://mobilis.inf.tu-dresden.de/apps/9cards";
}

@end