//
//  NSString+StringUtils.m
//  NineCards
//
//  Created by Markus Wutzler on 24/10/13.
//  Copyright (c) 2013 Mobilis. All rights reserved.
//

#import "NSString+StringUtils.h"

@implementation NSString (StringUtils)
-(BOOL)isEqualToString:(NSString *)aString ignoreCase:(BOOL)ignoreCase
{
	if (ignoreCase) {
		return [self compare:aString options:NSCaseInsensitiveSearch] == NSOrderedSame;
	} else {
		return [self isEqualToString:aString];
	}
}
@end
