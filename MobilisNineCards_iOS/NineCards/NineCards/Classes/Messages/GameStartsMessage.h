
#import <Foundation/Foundation.h>

#import "MXiBean.h"

@interface GameStartsMessage : MXiBean <NSMutableCopying>

@property(readwrite, nonatomic)     NSNumber * rounds;

@end