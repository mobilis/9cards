#import <Mxi/Mxi.h>

@interface StartGameMessage : NSObject <MXiIncomingBean, MXiOutgoingBean>

- (NSXMLElement* )toXML;
- (void)fromXML:(NSXMLElement* )xml;
+ (NSString* )elementName;
+ (NSString* )iqNamespace;

@end