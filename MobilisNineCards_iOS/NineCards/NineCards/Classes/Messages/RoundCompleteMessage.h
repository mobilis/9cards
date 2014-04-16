
#import <Foundation/Foundation.h>

#import "MXiBean.h"

@interface RoundCompleteMessage : MXiBean <NSMutableCopying>

@property(readwrite, nonatomic)     NSNumber * round;
@property(readwrite, nonatomic)     NSString * winner;
@property(readwrite, nonatomic, strong )     NSMutableArray * playerInfos;

@end