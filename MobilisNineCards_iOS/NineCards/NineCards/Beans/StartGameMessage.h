@interface StartGameMessage : NSObject

@property (nonatomic) NSInteger rounds;
@property (nonatomic, strong) NSString* password;

@end