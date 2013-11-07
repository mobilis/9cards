#import <Mxi/Mxi.h>

@interface GameStartsMessage : NSObject <MXiIncomingBean, MXiOutgoingBean>

@property (nonatomic) NSNumber* rounds;

- (NSXMLElement* )toXML;
- (void)fromXML:(NSXMLElement* )xml;
+ (NSString* )elementName;
+ (NSString* )iqNamespace;

@end