#import "PlayCardMessage.h"

#import "DDXML.h"


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
    self.round =
         [NSNumber numberWithDouble:[[[[xml elementsForName:@"round"] firstObject] stringValue] doubleValue]];
            self.card =
         [NSNumber numberWithDouble:[[[[xml elementsForName:@"card"] firstObject] stringValue] doubleValue]];
            self.beanType = GET;
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