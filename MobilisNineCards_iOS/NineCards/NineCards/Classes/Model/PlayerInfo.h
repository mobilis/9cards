
#import <Foundation/Foundation.h>

#import "DDXML.h"

@interface PlayerInfo : NSObject <NSMutableCopying>

@property(readwrite, nonatomic) NSString * id;
@property(readwrite, nonatomic) NSNumber * score;
@property(readwrite, nonatomic) NSMutableArray * usedcards;

- (NSXMLElement *)toXML;
- (void)fromXML:(NSXMLElement *)xmlElement;

@end