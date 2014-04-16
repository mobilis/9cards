
#import <Foundation/Foundation.h>

#import "MXiBean.h"

@interface GameOverMessage : MXiBean <NSMutableCopying>

@property(readwrite, nonatomic)     NSString * winner;
@property(readwrite, nonatomic)     NSNumber * score;
@property(readwrite, nonatomic, strong )     NSMutableArray * playerInfos;

@end