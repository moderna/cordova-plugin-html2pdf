/*
 * Copyright (C) 2014
 * Html 2 Pdf iOS Code: Clément Wehrung <cwehrung@nurves.com> (https://github.com/iclems/iOS-htmltopdf)
 * Cordova 3.3 Plugin & Html 2 Pdf Android Code: Modern Alchemists OG <office@modalog.at> (http://modalog.at)
 */

#import <Foundation/Foundation.h>

#import <Cordova/CDVPlugin.h>

#import "AppDelegate.h"

#define kPaperSizeA4 CGSizeMake(595.2,841.8)
#define kPaperSizeLetter CGSizeMake(612,792)

@interface Html2pdf : CDVPlugin <UIWebViewDelegate, UIDocumentInteractionControllerDelegate>
{
}

// read / write
-(void) create: (CDVInvokedUrlCommand*)command;

// retain command for async repsonses
@property (nonatomic, strong) CDVInvokedUrlCommand* command;
@property (nonatomic, strong) NSString* filePath;
@property (nonatomic, assign) CGSize pageSize;
@property (nonatomic, assign) UIEdgeInsets pageMargins;
@property (retain) UIDocumentInteractionController* documentController;

@end
