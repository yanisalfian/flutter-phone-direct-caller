import 'dart:async';

import 'package:flutter/services.dart';

/// Provides direct phone calling functionality without opening the dialer UI.
class FlutterPhoneDirectCaller {
  static const MethodChannel _channel =
      MethodChannel('flutter_phone_direct_caller');

  /// Initiates a direct phone call to [number].
  ///
  /// Returns `true` if the call intent/URL was launched successfully, or `false`
  /// if the number is invalid, permission was denied, or the platform failed
  /// to place the call.
  static Future<bool?> callNumber(String number) async {
    final trimmed = number.trim();
    if (trimmed.isEmpty) {
      return false;
    }
    return await _channel.invokeMethod<bool>(
      'callNumber',
      <String, Object>{
        'number': trimmed,
      },
    );
  }
}
