#import "PlayCardMessage.h"

#if TARGET_OS_IPHONE
#import "DDXML.h"
#endif


@implementation PlayCardMessage

+ (NSString *)elementName
{
    return @"PlayCardMessage";
}

+ (NSString *)namespace
{
    return @"http://mobilis.inf.tu-dresden.de/apps/9Cards";
}

- (id)mutableCopyWithZone:(NSZone *)zone
{
    PlayCardMessage *mutableCopy = [[PlayCardMessage alloc] init];
    mutableCopy.round = self.round;
    mutableCopy.card = self.card;

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
            self.card =
         [NSNumber numberWithDouble:[[[[xml elementsForName:@"card"] firstObject] stringValue] doubleValue]];
        }

- (NSXMLElement *)toXML
{
    NSXMLElement *serializedObject = [[NSXMLElement alloc] initWithName:[[self class] elementName] URI:[[self class] namespace]];
    @autoreleasepool {
        NSXMLElement *roundElement = [[NSXMLElement alloc] initWithName:@"round"];
        [roundElement setStringValue:[NSString stringWithFormat:@"%@", self.round]];
        [serializedObject addChild:roundElement];
        NSXMLElement *cardElement = [[NSXMLElement alloc] initWithName:@"card"];
        [cardElement setStringValue:[NSString stringWithFormat:@"%@", self.card]];
        [serializedObject addChild:cardElement];
    }
    return serializedObject;
}

@end