
#import <Foundation/Foundation.h>

#import "MXiBean.h"

@interface ConfigureGameRequest : MXiBean <NSMutableCopying>

@property(readwrite, nonatomic) NSNumber * players;
@property(readwrite, nonatomic) NSNumber * rounds;

@end