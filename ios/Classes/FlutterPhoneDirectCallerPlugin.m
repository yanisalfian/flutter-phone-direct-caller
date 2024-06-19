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
    //NSString *escapedPhoneNumber = [number stringByAddingPercentEncodingWithAllowedCharacters:[NSCharacterSet URLQueryAllowedCharacterSet]];

   // number = [number stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
    if( ! [number hasPrefix:@"tel:"]){
        number =  [NSString stringWithFormat:@"tel:%@", number];
    }
    NSURL *url = [NSURL URLWithString:number];

    if (![[UIApplication sharedApplication] canOpenURL:url]) {
        return NO;
    } else {
        if (@available(iOS 10.0, *)) {
            [[UIApplication sharedApplication] openURL:url
                                               options:@{}
                                     completionHandler:^(BOOL success) {
                                         if (!success) {
                                             NSLog(@"Failed to open URL: %@", number);
                                         }
                                     }];
        } else {
            // Suppress the deprecation warning for older iOS versions
    #pragma clang diagnostic push
    #pragma clang diagnostic ignored "-Wdeprecated-declarations"
            BOOL success = [[UIApplication sharedApplication] openURL:url];
    #pragma clang diagnostic pop
            if (!success) {
                // missing phone number
                return NO;
            }
        }
        return YES;
    }
}


@end
