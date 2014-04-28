#import "GetGameConfigurationRequest.h"

#import "DDXML.h"

@implementation GetGameConfigurationRequest

+ (NSString *)elementName
{
    return @"GetGameConfigurationRequest";
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
    GetGameConfigurationRequest *mutableCopy = [[GetGameConfigurationRequest alloc] init];
    mutableCopy.beanType = GET;

    return mutableCopy;
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