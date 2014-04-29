#import "PlayerInfo.h"

#import "DDXML.h"


@interface PlayerInfo ()

+ (NSString *)elementName;
+ (NSString *)namespace;

@end

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
    self.id =
     [[[xml elementsForName:@"id"] firstObject] stringValue];
            self.score =
     [NSNumber numberWithDouble:[[[[xml elementsForName:@"score"] firstObject] stringValue] doubleValue]];
            NSArray *usedcardsElements = [xml elementsForName:@"usedcards"];
    self.usedcards = [[NSMutableArray alloc] initWithCapacity:usedcardsElements.count];
    for (NSXMLElement *usedcardsElement in usedcardsElements)
    {
            NSNumber * element =
             [NSNumber numberWithDouble:[[usedcardsElement stringValue] doubleValue]];
                    [self.usedcards addObject:element];
    }
}

- (NSXMLElement *)toXML
{
    NSXMLElement *serializedObject = [[NSXMLElement alloc] initWithName:[[self class] elementName] URI:[[self class] namespace]];
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