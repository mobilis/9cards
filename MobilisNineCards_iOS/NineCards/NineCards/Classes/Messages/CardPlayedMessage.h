
#import <Foundation/Foundation.h>

#import "MXiBean.h"

@interface CardPlayedMessage : MXiBean <NSMutableCopying>

@property(readwrite, nonatomic)     NSNumber * round;
@property(readwrite, nonatomic)     NSString * player;

@end