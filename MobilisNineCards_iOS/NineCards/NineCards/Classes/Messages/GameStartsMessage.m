#import "GameStartsMessage.h"

#if TARGET_OS_IPHONE
#import "DDXML.h"
#endif


@implementation GameStartsMessage

+ (NSString *)elementName
{
    return @"GameStartsMessage";
}

+ (NSString *)namespace
{
    return @"http://mobilis.inf.tu-dresden.de/apps/9Cards";
}

- (id)mutableCopyWithZone:(NSZone *)zone
{
    GameStartsMessage *mutableCopy = [[GameStartsMessage alloc] init];
    mutableCopy.rounds = self.rounds;

    return mutableCopy;
}

- (id)init
{
    return [self initWithBeanType:GET andBeanContainer:BEAN_CONTAINER_MESSAGE];
}

#pragma mark - (De-)Serialization

- (void)fromXML:(NSXMLElement *)xml
{
    self.rounds =
         [NSNumber numberWithDouble:[[[[xml elementsForName:@"rounds"] firstObject] stringValue] doubleValue]];
        }

- (NSXMLElement *)toXML
{
    NSXMLElement *serializedObject = [[NSXMLElement alloc] initWithName:[[self class] elementName] URI:[[self class] namespace]];
    @autoreleasepool {
        NSXMLElement *roundsElement = [[NSXMLElement alloc] initWithName:@"rounds"];
        [roundsElement setStringValue:[NSString stringWithFormat:@"%@", self.rounds]];
        [serializedObject addChild:roundsElement];
    }
    return serializedObject;
}

@end