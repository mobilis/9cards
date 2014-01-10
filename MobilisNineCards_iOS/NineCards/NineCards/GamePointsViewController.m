//
//  GamePointsViewController.m
//  NineCards
//
//  Created by Martin Weißbach on 11/12/13.
//  Copyright (c) 2013 Mobilis. All rights reserved.
//

#import "GamePointsViewController.h"

#import "Player.h"
#import "PlayerInfo.h"

@interface GamePointsViewController ()

@end

@implementation GamePointsViewController
{
    __strong NSMutableArray *_players;
}

- (void)dealloc
{
    _players = nil;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    _players = [[NSMutableArray alloc] initWithCapacity:3];
}

- (void)viewDidAppear:(BOOL)animated
{
    [((UITableView *)self.view) reloadData];
}

#pragma mark – Manage Players

- (void)addPlayer:(NSString *)player
{
    Player *newPlayer = [Player playerWithJid:[XMPPJID jidWithString:player]];
    [_players addObject:newPlayer];
}

- (void)removePlayer:(NSString *)player
{
    [_players removeObject:player];
}

- (NSArray *)allPlayers
{
    return [NSArray arrayWithArray:_players];
}

- (void)updatePlayer:(NSString *)player withScore:(int)score
{
    for (Player *_player in _players) {
        if ([_player isEqual:[Player playerWithJid:[XMPPJID jidWithString:player]]]) {
            [_player addScorePoints:score];
            break;
        }
    }
}

- (void)updatePlayersWithPlayerInfos:(NSArray *)playerInfos
{
    for (PlayerInfo *playerInfo in playerInfos) {
        BOOL playerFound = NO;
        for (Player *player in _players) {
            if ([player.jid isEqualToJID:[XMPPJID jidWithString:playerInfo.id]]) {
                [player setScorePoints:[playerInfo.score intValue]];
                playerFound = YES;
                break;
            }
        }
        if (!playerFound) {
            [self addPlayer:playerInfo.id];
            [self updatePlayer:playerInfo.id withScore:[playerInfo.score intValue]];
        }
    }
}

#pragma mark - UITableViewDataSource

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return _players ? _players.count : 0;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *CellIdentifier = @"PlayerCell";
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleValue1 reuseIdentifier:CellIdentifier];
    }
    
    Player *player = [_players objectAtIndex:indexPath.row];
    cell.textLabel.text = player.jid.full;
    cell.detailTextLabel.text = [NSString stringWithFormat:@"%i", player.score];
    
    return cell;
}

@end
