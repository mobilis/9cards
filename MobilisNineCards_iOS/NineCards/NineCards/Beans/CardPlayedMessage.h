#import <Mxi/Mxi.h>

@interface CardPlayedMessage : NSObject <MXiIncomingBean, MXiOutgoingBean>

@property (nonatomic) NSNumber* round;
@property (nonatomic, strong) NSString* player;

@end