/*
 Copyright 2014 Modern Alchemists OG

 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
 */

/*
 * Html 2 Pdf iOS Code: Clément Wehrung <cwehrung@nurves.com> (https://github.com/iclems/iOS-htmltopdf)
 */

#import "Html2pdf.h"

@interface Html2pdf (Private)

- (BOOL) saveHtml:(NSString*)html asPdf:(NSString*)filePath;

@end

@interface UIPrintPageRenderer (PDF)

- (NSData*) printToPDF;

@end

@implementation Html2pdf

@synthesize command, filePath, pageSize, pageMargins, documentController;

- (void)create:(CDVInvokedUrlCommand*)command
{
    self.command = command;
    
    NSArray* arguments = command.arguments;

    NSLog(@"Creating pdf from html has been started.");
    
    NSString* html = [arguments objectAtIndex:0];
    self.filePath  = [[arguments objectAtIndex:1] stringByExpandingTildeInPath];
    
    // Set the base URL to be the www directory.
    NSString* wwwFilePath = [[NSBundle mainBundle] pathForResource:@"www" ofType:nil];
    NSURL*    baseURL     = [NSURL fileURLWithPath:wwwFilePath];
    
    // define page size and margins
    self.pageSize = kPaperSizeA4;
    self.pageMargins = UIEdgeInsetsMake(10, 5, 10, 5);
    
    // Load page into a webview and use its formatter to print the page
    UIWebView* webPage    = [[UIWebView alloc] init];
    webPage.delegate = self;
    webPage.frame = CGRectMake(0, 0, 1, 1); // Make web view small ...
    webPage.alpha = 0.0;                    // ... and invisible.
    [self.webView.superview addSubview:webPage];
    
    [webPage loadHTMLString:html baseURL:baseURL];
}

- (void)success
{
    NSString* resultMsg = [NSString stringWithFormat:@"HTMLtoPDF did succeed (%@)", self.filePath];
    NSLog(@"%@",resultMsg);
    
    // create acordova result
    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                messageAsString:[resultMsg stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
    
    // send cordova result
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

- (void)error:(NSString*)message
{
    NSString* resultMsg = [NSString stringWithFormat:@"HTMLtoPDF did fail (%@)", message];
    NSLog(@"%@",resultMsg);
    
    // create cordova result
    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                                messageAsString:[resultMsg stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
    
    // send cordova result
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

- (void)webViewDidFinishLoad:(UIWebView *)webView
{
    NSLog(@"Html2Pdf webViewDidFinishLoad");
    
    UIPrintPageRenderer *render = [[UIPrintPageRenderer alloc] init];
    
    [render addPrintFormatter:webView.viewPrintFormatter startingAtPageAtIndex:0];
    
    CGRect printableRect = CGRectMake(self.pageMargins.left,
                                      self.pageMargins.top,
                                      self.pageSize.width - self.pageMargins.left - self.pageMargins.right,
                                      self.pageSize.height - self.pageMargins.top - self.pageMargins.bottom);
    
    CGRect paperRect = CGRectMake(0, 0, self.pageSize.width, self.pageSize.height);
    
    [render setValue:[NSValue valueWithCGRect:paperRect] forKey:@"paperRect"];
    [render setValue:[NSValue valueWithCGRect:printableRect] forKey:@"printableRect"];
    
    if (filePath) {
        [[render printToPDF] writeToFile: filePath atomically: YES];
    }
    

    // remove webPage
    [webView stopLoading];
    webView.delegate = nil;
    [webView removeFromSuperview];
    webView = nil;

    // trigger success response
    [self success];

    // show "open pdf with ..." menu
    NSURL* url = [NSURL fileURLWithPath:filePath];
    self.documentController = [UIDocumentInteractionController interactionControllerWithURL:url];

    documentController.delegate = self;

    UIView* view = self.webView.superview;
    CGRect rect = view.frame; // open in top center
    rect.size.height *= 0.02;

    BOOL isValid = [documentController presentOpenInMenuFromRect:rect inView:view animated:YES];
    
    if (!isValid) {
        NSString* messageString = [NSString stringWithFormat:@"No PDF reader was found on your device. Please download a PDF reader (eg. iBooks or Acrobat)."];
        
        UIAlertView* alertView = [[UIAlertView alloc] initWithTitle:@"Error" message:messageString delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
        [alertView show];
        //[alertView release]; // p. leak
    }

}

- (void)webView:(UIWebView *)webView didFailLoadWithError:(NSError *)error
{
    NSLog(@"webViewDidFailLoadWithError");
    
    // trigger error response
    [self error:[error description]];
}


@end


@implementation UIPrintPageRenderer (PDF)

- (NSData*) printToPDF
{
    NSMutableData *pdfData = [NSMutableData data];
    
    UIGraphicsBeginPDFContextToData( pdfData, self.paperRect, nil );
    
    [self prepareForDrawingPages: NSMakeRange(0, self.numberOfPages)];
    
    CGRect bounds = UIGraphicsGetPDFContextBounds();
    
    for ( int i = 0 ; i < self.numberOfPages ; i++ )
    {
        UIGraphicsBeginPDFPage();
        
        [self drawPageAtIndex: i inRect: bounds];
    }
    
    UIGraphicsEndPDFContext();
    
    return pdfData;
}

@end