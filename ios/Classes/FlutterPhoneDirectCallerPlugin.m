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

    // Create a character set for the allowed characters
        NSCharacterSet *allowedCharacterSet = [NSCharacterSet URLQueryAllowedCharacterSet];
        // Encode the number string using the new method
        number = [number stringByAddingPercentEncodingWithAllowedCharacters:allowedCharacterSet];
        // Check if the number has the "tel:" prefix, if not add it
        if (![number hasPrefix:@"tel:"]) {
            number = [NSString stringWithFormat:@"tel:%@", number];
        }
    

    NSURL *telURL = [NSURL URLWithString:number];
        
        // Check if the application can open the URL
        if (![[UIApplication sharedApplication] canOpenURL:telURL]) {
            return NO;
        } else {
            // Try to open the URL with options and a completion handler
            [[UIApplication sharedApplication] openURL:telURL options:@{} completionHandler:^(BOOL success) {
                if (!success) {
                    // Handle the failure case
                    NSLog(@"Failed to open URL: %@", telURL);
                }
            }];
            
            return YES;
        };
}



@end
