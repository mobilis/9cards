#import <Mxi/Mxi.h>

@interface PlayerInfo : NSObject <MXiIncomingBean, MXiOutgoingBean>

@property (nonatomic, strong) NSString* id;
@property (nonatomic) NSNumber* score;
@property (nonatomic, strong) NSMutableArray* usedcards;

@end