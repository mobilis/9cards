#import <MXi/MXi.h>

@interface ConfigureGameRequest : MXiBean <MXiOutgoingBean>

@property (nonatomic, strong) NSString* gamename;
@property (nonatomic) NSNumber* players;
@property (nonatomic) NSNumber* rounds;

- (instancetype)init;

@end