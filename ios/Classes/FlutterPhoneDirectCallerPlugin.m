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
    if( ! [number hasPrefix:@"tel:"]){
        number =  [NSString stringWithFormat:@"tel:%@", number];
    }
    if(![[UIApplication sharedApplication] canOpenURL:[NSURL URLWithString:number]]) {
        return NO;
    } else if(![[UIApplication sharedApplication] openURL:[NSURL URLWithString:number]]) {
        // missing phone number
        return NO;
    } else {
        return YES;
    }
}

@end
