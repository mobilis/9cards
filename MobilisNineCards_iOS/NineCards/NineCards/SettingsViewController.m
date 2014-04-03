//
//  SettingsViewController.m
//  NineCards
//
//  Created by Markus Wutzler on 11.10.13.
//  Copyright (c) 2013 Technische Universität Dresden. All rights reserved.
//

#import "SettingsViewController.h"
#import "TextFieldCell.h"
#import "MXiConnectionHandler.h"
#import "AccountManager.h"

@interface SettingsViewController ()
@property (retain) NSString *jid;
@property (retain) NSString *password;
@property (retain) NSString *hostName;
@property (retain) NSString *runtimeName;
@property (retain) NSNumber *port;

@property (weak) UITextField *jidField;
@property (weak) UITextField *passwordField;
@property (weak) UITextField *hostNameField;
@property (weak) UITextField *runtimeTextField;
@property (weak) UITextField *portField;

@property (retain) UITableViewController *tvCtr;
@property (weak, nonatomic) IBOutlet UITableView *tableView;
@property (weak, nonatomic) IBOutlet UINavigationBar *navigationBar;

- (IBAction)cancel:(UIBarButtonItem *)sender;
- (IBAction)saveSettings:(UIBarButtonItem *)sender;

@end

@implementation SettingsViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view.
	NSString *imageName = @"9Cards-BG-iPhone";
	if ([UIScreen mainScreen].bounds.size.height == 568.0) {
		imageName = [NSString stringWithFormat:@"%@-568h", imageName];
	}
	self.view.backgroundColor = [UIColor colorWithPatternImage:[UIImage imageNamed:imageName]];
	_tvCtr = [[UITableViewController alloc] init];
	_tvCtr.tableView = self.tableView;
	_tvCtr.tableView.delegate = self;
	_tvCtr.tableView.dataSource = self;
	[self addChildViewController:_tvCtr];

    [self loadStoredAccountData];
}
- (void)loadStoredAccountData
{
    Account *account = [AccountManager account];
    self.hostName = account.hostName;
    self.jid = account.jid;
    self.password = account.password;
    self.port = account.port;
    self.runtimeName = account.runtimeName;
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

-(void)viewWillDisappear:(BOOL)animated {
	[super viewWillDisappear:animated];
}

#pragma mark - UITableViewDataSource
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
	return 5;
}

-(NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
	return 1;
}

-(UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
	TextFieldCell *cell = [tableView dequeueReusableCellWithIdentifier:@"TextFieldCell" forIndexPath:indexPath];
	UITextField *tf = cell.textField;
	tf.delegate = self;
	tf.autocapitalizationType = UITextAutocapitalizationTypeNone;
	tf.autocorrectionType = UITextAutocorrectionTypeNo;
	switch (indexPath.section) {
		case 0:
			self.hostNameField = tf;
			tf.keyboardType = UIKeyboardTypeURL;
			tf.text = self.hostName;
			break;
        case 1:
            self.runtimeTextField = tf;
            tf.keyboardType = UIKeyboardTypeDefault;
            tf.text = self.runtimeName;
            break;
		case 2:
			self.jidField = tf;
			tf.keyboardType = UIKeyboardTypeEmailAddress;
			tf.text = self.jid;
			break;
		case 3:
			self.passwordField = tf;
			tf.secureTextEntry = YES;
			tf.text = self.password;
			break;
		case 4:
			self.portField = tf;
			tf.keyboardType = UIKeyboardTypeNumberPad;
			tf.text = [NSString stringWithFormat:@"%@", self.port];
			break;
        default: break;
	}
	return cell;
}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
{
	switch (section) {
		case 0:
			return @"Host";
        case 1:
            return @"Runtime Name";
		case 2:
			return @"Your Account (JID)";
		case 3:
			return @"Password";
		case 4:
			return @"Port";
        default:
			return @"";
	}
}

#pragma mark - UITableViewDelegate

#pragma mark - UITextFieldDelegate
-(void)textFieldDidEndEditing:(UITextField *)textField
{
	if(textField == self.hostNameField) {
		self.hostName = self.hostNameField.text;
	}
	if(textField == self.jidField) {
		self.jid = self.jidField.text;
	}
	if(textField == self.passwordField) {
		self.password = self.passwordField.text;
	}
	if (textField == self.portField) {
		self.port = [NSNumber numberWithInt:[self.portField.text intValue]];
	}
    if (textField == self.runtimeTextField) {
        self.runtimeName = self.runtimeTextField.text;
    }
}

#pragma mark - Interface Implementation
- (IBAction)cancel:(UIBarButtonItem *)sender {
	[self dismissViewControllerAnimated:YES completion:nil];
}

- (IBAction)saveSettings:(UIBarButtonItem *)sender
{
	Account *account = [Account new];
    account.jid = self.jid;
    account.password = self.password;
    account.hostName = self.hostName;
    account.runtimeName = self.runtimeName;
    account.port = self.port;
    [AccountManager storeAccount:account];
    [[MXiConnectionHandler sharedInstance] reconnectWithJID:account.jid
                                                   password:account.password
                                                   hostName:account.hostName
                                                runtimeName:account.runtimeName
                                                       port:account.port];
	[self dismissViewControllerAnimated:YES completion:nil];
}

#pragma mark - UINavigationBarDelegate
-(UIBarPosition)positionForBar:(id<UIBarPositioning>)bar
{
	return UIBarPositionTopAttached;
}

@end