
#import <Foundation/Foundation.h>

#import "MXiBean.h"

@interface GetGameConfigurationResponse : MXiBean <NSMutableCopying>

@property(readwrite, nonatomic) NSString * muc;
@property(readwrite, nonatomic) NSNumber * maxRounds;
@property(readwrite, nonatomic) NSNumber * maxPlayers;

@end