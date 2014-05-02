#import "GameOverMessage.h"

#if TARGET_OS_IPHONE
#import "DDXML.h"
#endif

#import "PlayerInfo.h"

@implementation GameOverMessage

+ (NSString *)elementName
{
    return @"GameOverMessage";
}

+ (NSString *)namespace
{
    return @"http://mobilis.inf.tu-dresden.de/apps/9Cards";
}

- (id)mutableCopyWithZone:(NSZone *)zone
{
    GameOverMessage *mutableCopy = [[GameOverMessage alloc] init];
    mutableCopy.winner = self.winner;
    mutableCopy.score = self.score;
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
    self.winner =
         [[[xml elementsForName:@"winner"] firstObject] stringValue];
            self.score =
         [NSNumber numberWithDouble:[[[[xml elementsForName:@"score"] firstObject] stringValue] doubleValue]];
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
        NSXMLElement *winnerElement = [[NSXMLElement alloc] initWithName:@"winner"];
        [winnerElement setStringValue:[NSString stringWithFormat:@"%@", self.winner]];
        [serializedObject addChild:winnerElement];
        NSXMLElement *scoreElement = [[NSXMLElement alloc] initWithName:@"score"];
        [scoreElement setStringValue:[NSString stringWithFormat:@"%@", self.score]];
        [serializedObject addChild:scoreElement];
        for (PlayerInfo * element in self.playerInfos)
        {
            [serializedObject addChild:[element toXML]];
        }
    }
    return serializedObject;
}

@end