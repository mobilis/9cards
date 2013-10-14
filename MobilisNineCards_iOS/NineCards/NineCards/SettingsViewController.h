//
//  SettingsViewController.h
//  NineCards
//
//  Created by Markus Wutzler on 11.10.13.
//  Copyright (c) 2013 Mobilis. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface SettingsViewController : UIViewController <UITableViewDataSource, UITableViewDelegate, UINavigationBarDelegate, UITextFieldDelegate>
@property (weak, nonatomic) IBOutlet UITableView *tableView;
@property (weak, nonatomic) IBOutlet UINavigationBar *navigationBar;

@end