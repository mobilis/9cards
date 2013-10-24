//
//  ViewController.h
//  NineCards
//
//  Created by Markus Wutzler on 10.10.13.
//  Copyright (c) 2013 Mobilis. All rights reserved.
//

#import <UIKit/UIKit.h>

@class Game;

@interface GameListTableViewController : UITableViewController
- (Game*) gameForIndexPath:(NSIndexPath*)path;
@end
