#import "ConfigureGameRequest.h"

#if TARGET_OS_IPHONE
#import "DDXML.h"
#endif

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
    return [self initWithBeanType:SET andBeanContainer:BEAN_CONTAINER_IQ];
}

#pragma mark - NSMutableCopy Protocol

- (id)mutableCopyWithZone:(NSZone *)zone
{
    ConfigureGameRequest *mutableCopy = [[ConfigureGameRequest alloc] init];
    mutableCopy.players = self.players;
    mutableCopy.rounds = self.rounds;

    return mutableCopy;
}

#pragma mark - (De-)Serialization

- (void)fromXML:(NSXMLElement *)xml
{
    self.players =
         [NSNumber numberWithDouble:[[[[xml elementsForName:@"players"] firstObject] stringValue] doubleValue]];
            self.rounds =
         [NSNumber numberWithDouble:[[[[xml elementsForName:@"rounds"] firstObject] stringValue] doubleValue]];
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