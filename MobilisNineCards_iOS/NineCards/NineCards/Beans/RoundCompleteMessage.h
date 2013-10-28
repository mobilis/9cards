#import "PlayerInfo.h"

#import <Mxi/Mxi.h>

@interface RoundCompleteMessage : NSObject <MXiIncomingBean, MXiOutgoingBean>

@property (nonatomic) NSNumber* round;
@property (nonatomic, strong) NSString* winner;
@property (nonatomic, strong) NSMutableArray* PlayerInfos;

- (NSXMLElement* )toXML;
- (void)fromXML:(NSXMLElement* )xml;
+ (NSString* )elementName;
+ (NSString* )iqNamespace;

@end