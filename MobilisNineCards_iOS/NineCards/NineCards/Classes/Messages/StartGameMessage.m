#import "StartGameMessage.h"

#if TARGET_OS_IPHONE
#import "DDXML.h"
#endif


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

    return mutableCopy;
}

- (id)init
{
    return [self initWithBeanType:GET andBeanContainer:BEAN_CONTAINER_MESSAGE];
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