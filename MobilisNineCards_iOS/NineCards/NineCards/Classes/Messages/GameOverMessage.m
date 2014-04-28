#import "GameOverMessage.h"

#import "DDXML.h"

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
    self.winner = (NSString *)[[xml elementsForName:@"winner"] firstObject];
    self.score = (NSNumber *)[[xml elementsForName:@"score"] firstObject];
    NSArray *playerInfosElements = [[xml elementsForName:@"playerInfos"] firstObject];
    self.playerInfos = [[NSMutableArray alloc] initWithCapacity:playerInfosElements.count];
    for (NSXMLElement *playerInfosElement in playerInfosElements)
    {
        PlayerInfo * element = [PlayerInfo new];
        [element fromXML:playerInfosElement];
        [self.playerInfos addObject:element];
    }
    self.beanType = GET;
}

- (NSXMLElement *)toXML
{
    NSXMLElement *serializedObject = [[NSXMLElement alloc] initWithName:[[self class] elementName]];
    [serializedObject addNamespace:[NSXMLNode namespaceWithName:@"xml:ns" stringValue:[[self class] namespace]]];
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