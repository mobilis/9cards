@interface CardPlayedMessage : NSObject

@property (nonatomic) NSInteger round;
@property (nonatomic, strong) NSString* player;

@end