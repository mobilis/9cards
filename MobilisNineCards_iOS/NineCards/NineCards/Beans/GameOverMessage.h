#import "PlayerInfo.h"

#import <Mxi/Mxi.h>

@interface GameOverMessage : NSObject <MXiIncomingBean, MXiOutgoingBean>

@property (nonatomic, strong) NSString* winner;
@property (nonatomic) NSNumber* score;
@property (nonatomic, strong) NSMutableArray* PlayerInfos;

- (NSXMLElement* )toXML;
- (void)fromXML:(NSXMLElement* )xml;
+ (NSString* )elementName;
+ (NSString* )iqNamespace;

@end