//
//  PlayerListSectionHeader.h
//  NineCards
//
//  Created by Markus Wutzler on 15.11.13.
//  Copyright (c) 2013 Mobilis. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface PlayerListSectionHeader : UICollectionReusableView
@property (weak, nonatomic) IBOutlet UILabel *userNameLabel;
@property (weak, nonatomic) IBOutlet UILabel *scoreLabel;

@end
