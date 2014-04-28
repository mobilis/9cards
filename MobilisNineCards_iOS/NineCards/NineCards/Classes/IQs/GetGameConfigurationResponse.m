#import "GetGameConfigurationResponse.h"

#import "DDXML.h"

@implementation GetGameConfigurationResponse

+ (NSString *)elementName
{
    return @"GetGameConfigurationResponse";
}

+ (NSString *)namespace
{
    return @"http://mobilis.inf.tu-dresden.de/apps/9Cards";
}

- (id)init
{
    return [self initWithBeanType:GET];
}

#pragma mark - NSMutableCopy Protocol

- (id)mutableCopyWithZone:(NSZone *)zone
{
    GetGameConfigurationResponse *mutableCopy = [[GetGameConfigurationResponse alloc] init];
    mutableCopy.muc = self.muc;
    mutableCopy.maxRounds = self.maxRounds;
    mutableCopy.maxPlayers = self.maxPlayers;
    mutableCopy.beanType = RESULT;

    return mutableCopy;
}

#pragma mark - (De-)Serialization

- (void)fromXML:(NSXMLElement *)xml
{
    self.muc = (NSString *)[[xml elementsForName:@"muc"] firstObject];
    self.maxRounds = (NSNumber *)[[xml elementsForName:@"maxRounds"] firstObject];
    self.maxPlayers = (NSNumber *)[[xml elementsForName:@"maxPlayers"] firstObject];
    self.beanType = RESULT;
}

- (NSXMLElement *)toXML
{
    NSXMLElement *serializedObject = [[NSXMLElement alloc] initWithName:[[self class] elementName]];
    [serializedObject addNamespace:[NSXMLNode namespaceWithName:@"xml:ns" stringValue:[[self class] namespace]]];
    @autoreleasepool {
        NSXMLElement *mucElement = [[NSXMLElement alloc] initWithName:@"muc"];
        [mucElement setStringValue:[NSString stringWithFormat:@"%@", self.muc]];
        [serializedObject addChild:mucElement];
        NSXMLElement *maxRoundsElement = [[NSXMLElement alloc] initWithName:@"maxRounds"];
        [maxRoundsElement setStringValue:[NSString stringWithFormat:@"%@", self.maxRounds]];
        [serializedObject addChild:maxRoundsElement];
        NSXMLElement *maxPlayersElement = [[NSXMLElement alloc] initWithName:@"maxPlayers"];
        [maxPlayersElement setStringValue:[NSString stringWithFormat:@"%@", self.maxPlayers]];
        [serializedObject addChild:maxPlayersElement];
    }
    return serializedObject;
}

@end