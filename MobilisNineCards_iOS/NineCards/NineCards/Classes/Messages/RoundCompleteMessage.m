#import "RoundCompleteMessage.h"

#if TARGET_OS_IPHONE
#import "DDXML.h"
#endif

#import "PlayerInfo.h"

@implementation RoundCompleteMessage

+ (NSString *)elementName
{
    return @"RoundCompleteMessage";
}

+ (NSString *)namespace
{
    return @"http://mobilis.inf.tu-dresden.de/apps/9Cards";
}

- (id)mutableCopyWithZone:(NSZone *)zone
{
    RoundCompleteMessage *mutableCopy = [[RoundCompleteMessage alloc] init];
    mutableCopy.round = self.round;
    mutableCopy.winner = self.winner;
    mutableCopy.playerInfos = self.playerInfos;

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
            self.winner =
         [[[xml elementsForName:@"winner"] firstObject] stringValue];
            NSArray *playerInfosElements = [xml elementsForName:@"playerInfos"];
    self.playerInfos = [[NSMutableArray alloc] initWithCapacity:playerInfosElements.count];
    for (NSXMLElement *playerInfosElement in playerInfosElements)
    {
        PlayerInfo * element = [PlayerInfo new];
        [element fromXML:playerInfosElement];
        [self.playerInfos addObject:element];
    }
}

- (NSXMLElement *)toXML
{
    NSXMLElement *serializedObject = [[NSXMLElement alloc] initWithName:[[self class] elementName] URI:[[self class] namespace]];
    @autoreleasepool {
        NSXMLElement *roundElement = [[NSXMLElement alloc] initWithName:@"round"];
        [roundElement setStringValue:[NSString stringWithFormat:@"%@", self.round]];
        [serializedObject addChild:roundElement];
        NSXMLElement *winnerElement = [[NSXMLElement alloc] initWithName:@"winner"];
        [winnerElement setStringValue:[NSString stringWithFormat:@"%@", self.winner]];
        [serializedObject addChild:winnerElement];
        for (PlayerInfo * element in self.playerInfos)
        {
            [serializedObject addChild:[element toXML]];
        }
    }
    return serializedObject;
}

@end