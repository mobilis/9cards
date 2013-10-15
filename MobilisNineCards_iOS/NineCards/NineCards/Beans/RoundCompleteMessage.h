@interface RoundCompleteMessage : NSObject

@property (nonatomic) NSInteger round;
@property (nonatomic, strong) NSString* winner;
@property (nonatomic, strong) NSMutableArray* PlayerInfos;

@end