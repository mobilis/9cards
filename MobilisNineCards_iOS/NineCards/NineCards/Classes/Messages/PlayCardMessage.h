
#import <Foundation/Foundation.h>

#import "MXiBean.h"

@interface PlayCardMessage : MXiBean <NSMutableCopying>

@property(readwrite, nonatomic)     NSNumber * round;
@property(readwrite, nonatomic)     NSNumber * card;

@end