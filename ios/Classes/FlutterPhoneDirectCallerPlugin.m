#import "FlutterPhoneDirectCallerPlugin.h"

@implementation FlutterPhoneDirectCallerPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  FlutterMethodChannel* channel = [FlutterMethodChannel
      methodChannelWithName:@"flutter_phone_direct_caller"
            binaryMessenger:[registrar messenger]];
  FlutterPhoneDirectCallerPlugin* instance = [[FlutterPhoneDirectCallerPlugin alloc] init];
  [registrar addMethodCallDelegate:instance channel:channel];
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
  if ([@"getPlatformVersion" isEqualToString:call.method]) {
    result([@"iOS " stringByAppendingString:[[UIDevice currentDevice] systemVersion]]);
  } else if ([@"callNumber" isEqualToString:call.method]) {
      NSString* number = call.arguments[@"number"];
      result(@([self directCall:number]));
  } else {
    result(FlutterMethodNotImplemented);
  }
}

- (BOOL)directCall:(NSString*)number {
    number = [number stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
    if (![number hasPrefix:@"tel:"]) {
        number = [NSString stringWithFormat:@"tel:%@", number];
    }
    
    NSURL *url = [NSURL URLWithString:number];
    if (![[UIApplication sharedApplication] canOpenURL:url]) {
        return NO;
    }
    
    // Use the new `openURL` method
    [[UIApplication sharedApplication] openURL:url options:@{} completionHandler:^(BOOL success) {
        if (!success) {
            NSLog(@"Failed to open URL: %@", number);
        }
    }];
    
    return YES;
}

@end
