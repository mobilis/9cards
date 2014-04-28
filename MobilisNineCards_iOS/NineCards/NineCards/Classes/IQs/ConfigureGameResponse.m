#import "ConfigureGameResponse.h"

#import "DDXML.h"

@implementation ConfigureGameResponse

+ (NSString *)elementName
{
    return @"ConfigureGameResponse";
}

+ (NSString *)namespace
{
    return @"http://mobilis.inf.tu-dresden.de/apps/9Cards";
}

- (id)init
{
    return [self initWithBeanType:GET];
}

#pragma mark - NSMutableCopy Protocol

- (id)mutableCopyWithZone:(NSZone *)zone
{
    ConfigureGameResponse *mutableCopy = [[ConfigureGameResponse alloc] init];
    mutableCopy.muc = self.muc;
    mutableCopy.beanType = RESULT;

    return mutableCopy;
}

#pragma mark - (De-)Serialization

- (void)fromXML:(NSXMLElement *)xml
{
    self.muc = (NSString *)[[xml elementsForName:@"muc"] firstObject];
    self.beanType = RESULT;
}

- (NSXMLElement *)toXML
{
    NSXMLElement *serializedObject = [[NSXMLElement alloc] initWithName:[[self class] elementName]];
    [serializedObject addNamespace:[NSXMLNode namespaceWithName:@"xml:ns" stringValue:[[self class] namespace]]];
    @autoreleasepool {
        NSXMLElement *mucElement = [[NSXMLElement alloc] initWithName:@"muc"];
        [mucElement setStringValue:[NSString stringWithFormat:@"%@", self.muc]];
        [serializedObject addChild:mucElement];
    }
    return serializedObject;
}

@end