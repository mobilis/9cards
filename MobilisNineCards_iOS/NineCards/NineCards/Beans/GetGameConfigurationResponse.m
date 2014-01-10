#import "GetGameConfigurationResponse.h"

@implementation GetGameConfigurationResponse

- (id)init {
	self = [super initWithBeanType:RESULT];

	return self;
}

- (void)fromXML:(NSXMLElement* )xml {
	NSXMLElement* mucElement = [xml elementForName:@"muc"];
	[self setMuc:[mucElement stringValue]];

	NSXMLElement* maxRoundsElement = [xml elementForName:@"maxRounds"];
	[self setMaxRounds:[NSNumber numberWithFloat:[[maxRoundsElement stringValue] floatValue]]];

	NSXMLElement* maxPlayersElement = [xml elementForName:@"maxPlayers"];
	[self setMaxPlayers:[NSNumber numberWithFloat:[[maxPlayersElement stringValue] floatValue]]];
}

+ (NSString* )elementName {
	return @"GetGameConfigurationResponse";
}

+ (NSString* )iqNamespace {
	return @"http://mobilis.inf.tu-dresden.de/apps/9cards";
}

@end