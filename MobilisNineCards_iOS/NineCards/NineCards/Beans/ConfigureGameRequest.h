#import <MXi/MXi.h>

@interface ConfigureGameRequest : MXiBean <MXiOutgoingBean>

@property (nonatomic, strong) NSString* gamename;
@property (nonatomic) NSInteger players;
@property (nonatomic) NSInteger rounds;

- (id)init;

@end