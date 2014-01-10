#import <MXi/MXi.h>

@interface ConfigureGameResponse : MXiBean <MXiIncomingBean>

@property (nonatomic, strong) NSString* muc;

- (instancetype)init;

@end