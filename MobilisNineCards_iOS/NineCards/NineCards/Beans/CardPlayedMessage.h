#import <Mxi/Mxi.h>

@interface CardPlayedMessage : NSObject <MXiIncomingBean, MXiOutgoingBean>

@property (nonatomic) NSNumber* round;
@property (nonatomic, strong) NSString* player;

- (NSXMLElement* )toXML;
- (void)fromXML:(NSXMLElement* )xml;
+ (NSString* )elementName;
+ (NSString* )iqNamespace;

@end