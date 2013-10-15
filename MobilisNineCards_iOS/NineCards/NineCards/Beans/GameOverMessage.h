@interface GameOverMessage : NSObject

@property (nonatomic, strong) NSString* winner;
@property (nonatomic) NSInteger score;
@property (nonatomic, strong) NSMutableArray* PlayerInfos;

@end