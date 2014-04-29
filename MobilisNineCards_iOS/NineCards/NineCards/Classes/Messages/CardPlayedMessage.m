#import "CardPlayedMessage.h"

#import "DDXML.h"


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
    mutableCopy.beanType = GET;

    return mutableCopy;
}

- (id)init
{
    return [self initWithBeanType:GET];
}

#pragma mark - (De-)Serialization

- (void)fromXML:(NSXMLElement *)xml
{
    self.round = (NSNumber *)[[xml elementsForName:@"round"] firstObject];
    self.player = (NSString *)[[xml elementsForName:@"player"] firstObject];
    self.beanType = GET;
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