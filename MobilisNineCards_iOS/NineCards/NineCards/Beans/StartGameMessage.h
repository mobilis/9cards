#import <Mxi/Mxi.h>

@interface StartGameMessage : NSObject <MXiIncomingBean, MXiOutgoingBean>

@property (nonatomic) NSNumber* rounds;
@property (nonatomic, strong) NSString* password;

@end