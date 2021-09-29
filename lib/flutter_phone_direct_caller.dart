import 'dart:async';

import 'package:flutter/services.dart';

class FlutterPhoneDirectCaller {
  static const MethodChannel _channel =
      MethodChannel('flutter_phone_direct_caller');

  static Future<bool?> callNumber(String number) async {
    return await directCall(number);
  }

  static Future<bool?> directCall(String number) async {
    final bool? result = await _channel.invokeMethod(
      'callNumber',
      <String, Object>{
        'number': number,
      },
    );
    return result;
  }
}
