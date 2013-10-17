#import <Mxi/Mxi.h>

@interface PlayCardMessage : NSObject <MXiIncomingBean, MXiOutgoingBean>

@property (nonatomic) NSNumber* round;
@property (nonatomic) NSNumber* card;

@end