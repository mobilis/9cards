#import "ConfigureGameRequest.h"

#import "DDXML.h"

@implementation ConfigureGameRequest

+ (NSString *)elementName
{
    return @"ConfigureGameRequest";
}

+ (NSString *)namespace
{
    return @"http://mobilis.inf.tu-dresden.de/apps/9Cards";
}

- (id)init
{
    return [self initWithBeanType:SET];
}

#pragma mark - NSMutableCopy Protocol

- (id)mutableCopyWithZone:(NSZone *)zone
{
    ConfigureGameRequest *mutableCopy = [[ConfigureGameRequest alloc] init];
    mutableCopy.players = self.players;
    mutableCopy.rounds = self.rounds;
    mutableCopy.beanType = SET;

    return mutableCopy;
}

#pragma mark - (De-)Serialization

- (void)fromXML:(NSXMLElement *)xml
{
    self.players = (NSNumber *)[[xml elementsForName:@"players"] firstObject];
    self.rounds = (NSNumber *)[[xml elementsForName:@"rounds"] firstObject];
    self.beanType = SET;
}

- (NSXMLElement *)toXML
{
    NSXMLElement *serializedObject = [[NSXMLElement alloc] initWithName:[[self class] elementName] URI:[[self class] namespace]];
    @autoreleasepool {
        NSXMLElement *playersElement = [[NSXMLElement alloc] initWithName:@"players"];
        [playersElement setStringValue:[NSString stringWithFormat:@"%@", self.players]];
        [serializedObject addChild:playersElement];
        NSXMLElement *roundsElement = [[NSXMLElement alloc] initWithName:@"rounds"];
        [roundsElement setStringValue:[NSString stringWithFormat:@"%@", self.rounds]];
        [serializedObject addChild:roundsElement];
    }
    return serializedObject;
}

@end