#import "ConfigureGameResponse.h"

#if TARGET_OS_IPHONE
#import "DDXML.h"
#endif

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
    return [self initWithBeanType:RESULT andBeanContainer:BEAN_CONTAINER_IQ];
}

#pragma mark - NSMutableCopy Protocol

- (id)mutableCopyWithZone:(NSZone *)zone
{
    ConfigureGameResponse *mutableCopy = [[ConfigureGameResponse alloc] init];
    mutableCopy.muc = self.muc;

    return mutableCopy;
}

#pragma mark - (De-)Serialization

- (void)fromXML:(NSXMLElement *)xml
{
    self.muc =
         [[[xml elementsForName:@"muc"] firstObject] stringValue];
        }

- (NSXMLElement *)toXML
{
    NSXMLElement *serializedObject = [[NSXMLElement alloc] initWithName:[[self class] elementName] URI:[[self class] namespace]];
    @autoreleasepool {
        NSXMLElement *mucElement = [[NSXMLElement alloc] initWithName:@"muc"];
        [mucElement setStringValue:[NSString stringWithFormat:@"%@", self.muc]];
        [serializedObject addChild:mucElement];
    }
    return serializedObject;
}

@end