import 'dart:async';

import 'package:flutter/services.dart';

class FlutterPhoneDirectCaller {
  static const MethodChannel _channel =
      const MethodChannel('flutter_phone_direct_caller');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<bool> callNumber(String number) async{
    return await directCall(number);
  }

  static Future<bool> directCall(String number) async{
    print("test");
    final bool result = await _channel.invokeMethod('callNumber',<String, Object>{
      'number': number
    });
    return result;
  }
}
