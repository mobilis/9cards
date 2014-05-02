#import "CardPlayedMessage.h"

#if TARGET_OS_IPHONE
#import "DDXML.h"
#endif


@implementation CardPlayedMessage

+ (NSString *)elementName
{
    return @"CardPlayedMessage";
}

+ (NSString *)namespace
{
    return @"http://mobilis.inf.tu-dresden.de/apps/9Cards";
}

- (id)mutableCopyWithZone:(NSZone *)zone
{
    CardPlayedMessage *mutableCopy = [[CardPlayedMessage alloc] init];
    mutableCopy.round = self.round;
    mutableCopy.player = self.player;

    return mutableCopy;
}

- (id)init
{
    return [self initWithBeanType:GET andBeanContainer:BEAN_CONTAINER_MESSAGE];
}

#pragma mark - (De-)Serialization

- (void)fromXML:(NSXMLElement *)xml
{
    self.round =
         [NSNumber numberWithDouble:[[[[xml elementsForName:@"round"] firstObject] stringValue] doubleValue]];
            self.player =
         [[[xml elementsForName:@"player"] firstObject] stringValue];
        }

- (NSXMLElement *)toXML
{
    NSXMLElement *serializedObject = [[NSXMLElement alloc] initWithName:[[self class] elementName] URI:[[self class] namespace]];
    @autoreleasepool {
        NSXMLElement *roundElement = [[NSXMLElement alloc] initWithName:@"round"];
        [roundElement setStringValue:[NSString stringWithFormat:@"%@", self.round]];
        [serializedObject addChild:roundElement];
        NSXMLElement *playerElement = [[NSXMLElement alloc] initWithName:@"player"];
        [playerElement setStringValue:[NSString stringWithFormat:@"%@", self.player]];
        [serializedObject addChild:playerElement];
    }
    return serializedObject;
}

@end