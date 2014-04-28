#import "StartGameMessage.h"

#import "DDXML.h"


@implementation StartGameMessage

+ (NSString *)elementName
{
    return @"StartGameMessage";
}

+ (NSString *)namespace
{
    return @"http://mobilis.inf.tu-dresden.de/apps/9Cards";
}

- (id)mutableCopyWithZone:(NSZone *)zone
{
    StartGameMessage *mutableCopy = [[StartGameMessage alloc] init];
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
    self.beanType = GET;
}

- (NSXMLElement *)toXML
{
    NSXMLElement *serializedObject = [[NSXMLElement alloc] initWithName:[[self class] elementName]];
    [serializedObject addNamespace:[NSXMLNode namespaceWithName:@"xml:ns" stringValue:[[self class] namespace]]];
    @autoreleasepool {
    }
    return serializedObject;
}

@end