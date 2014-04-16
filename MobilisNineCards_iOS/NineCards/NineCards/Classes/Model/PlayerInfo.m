#import "PlayerInfo.h"

#import "DDXML.h"


@implementation PlayerInfo

+ (NSString *)elementName
{
    return @"PlayerInfo";
}

+ (NSString *)namespace
{
    return @"http://mobilis.inf.tu-dresden.de/apps/9Cards";
}

- (id)mutableCopyWithZone:(NSZone *)zone
{
    PlayerInfo *mutableCopy = [[PlayerInfo alloc] init];

    return mutableCopy;
}

#pragma mark - Serialization

- (void)fromXML:(NSXMLElement *)xml
{
    self.id = (NSString *)[[xml elementsForName:@"id"] firstObject];
    self.score = (NSNumber *)[[xml elementsForName:@"score"] firstObject];
    NSArray *usedcardsElements = [[xml elementsForName:@"usedcards"] firstObject];
    self.usedcards = [[NSMutableArray alloc] initWithCapacity:usedcardsElements.count];
    for (NSXMLElement *usedcardsElement in usedcardsElements)
    {
            NSNumber * element =
             [NSNumber numberWithChar:(char )[[usedcardsElement stringValue] UTF8String]];
                    [self.usedcards addObject:element];
    }
}

- (NSXMLElement *)toXML
{
    NSXMLElement *serializedObject = [[NSXMLElement alloc] initWithName:[[self class] elementName]];
    [serializedObject addNamespace:[NSXMLNode namespaceWithName:@"xml:ns" stringValue:[[self class] namespace]]];
    @autoreleasepool {
        NSXMLElement *idElement = [[NSXMLElement alloc] initWithName:@"id"];
        [idElement setStringValue:[NSString stringWithFormat:@"%@", self.id]];
        [serializedObject addChild:idElement];
        NSXMLElement *scoreElement = [[NSXMLElement alloc] initWithName:@"score"];
        [scoreElement setStringValue:[NSString stringWithFormat:@"%@", self.score]];
        [serializedObject addChild:scoreElement];
        for (NSNumber * element in self.usedcards)
        {
            NSXMLElement *childElement = [[NSXMLElement alloc] initWithName:@"usedcards"];
            [childElement setStringValue:[NSString stringWithFormat:@"%@",element]];
            [serializedObject addChild:childElement];
        }
    }
    return serializedObject;
}

@end