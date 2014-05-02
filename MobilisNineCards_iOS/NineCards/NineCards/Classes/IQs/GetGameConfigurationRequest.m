#import "GetGameConfigurationRequest.h"

#if TARGET_OS_IPHONE
#import "DDXML.h"
#endif

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
    return [self initWithBeanType:GET andBeanContainer:BEAN_CONTAINER_IQ];
}

#pragma mark - NSMutableCopy Protocol

- (id)mutableCopyWithZone:(NSZone *)zone
{
    GetGameConfigurationRequest *mutableCopy = [[GetGameConfigurationRequest alloc] init];

    return mutableCopy;
}

#pragma mark - (De-)Serialization

- (void)fromXML:(NSXMLElement *)xml
{
}

- (NSXMLElement *)toXML
{
    NSXMLElement *serializedObject = [[NSXMLElement alloc] initWithName:[[self class] elementName] URI:[[self class] namespace]];
    @autoreleasepool {
    }
    return serializedObject;
}

@end