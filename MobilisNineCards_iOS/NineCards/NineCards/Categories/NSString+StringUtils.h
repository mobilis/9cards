//
//  NSString+StringUtils.h
//  NineCards
//
//  Created by Markus Wutzler on 24/10/13.
//  Copyright (c) 2013 Mobilis. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface NSString (StringUtils)
- (BOOL) isEqualToString:(NSString *)aString ignoreCase:(BOOL)ignoreCase;
@end
