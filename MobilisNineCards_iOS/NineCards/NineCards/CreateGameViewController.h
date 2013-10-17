//
//  CreateGameViewController.h
//  NineCards
//
//  Created by Markus Wutzler on 11.10.13.
//  Copyright (c) 2013 Mobilis. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "Game.h"
@protocol CreateGameDelegate <NSObject>

- (void) gameCreated:(Game *)game;

@end

@interface CreateGameViewController : UIViewController<CreateGameDelegate>

@property (weak) id<CreateGameDelegate> delegate;

@end
