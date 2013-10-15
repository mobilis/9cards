@interface PlayerInfo : NSObject

@property (nonatomic, strong) NSString* id;
@property (nonatomic) NSInteger score;
@property (nonatomic, strong) NSMutableArray* usedcards;

@end