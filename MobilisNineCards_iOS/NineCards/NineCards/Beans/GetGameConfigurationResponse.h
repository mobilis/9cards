#import <MXi/MXi.h>

@interface GetGameConfigurationResponse : MXiBean <MXiIncomingBean>

@property (nonatomic, strong) NSString* muc;
@property (nonatomic) NSNumber* maxRounds;
@property (nonatomic) NSNumber* maxPlayers;

- (instancetype)init;

@end