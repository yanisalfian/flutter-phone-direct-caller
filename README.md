# flutter_phone_direct_caller

A simple Flutter plugin to call a number directly from your app without going to the phone dialer. Runtime `CALL_PHONE` permission on Android is handled automatically by the plugin.

## Requirements

- Flutter `>=3.44.0`
- Dart `^3.12.0`
- iOS `13.0+` (Swift Package Manager only)
- Android `minSdk 21+` (built-in Kotlin / AGP 8–9+ compatible)

## Usage

Add dependency to your `pubspec.yaml` file:

```yaml
dependencies:
  flutter_phone_direct_caller: ^3.0.0
```

### Android
No additional configuration is required. `CALL_PHONE` permission is declared by the plugin and requested at runtime when needed.

### iOS
Add `tel` to `LSApplicationQueriesSchemes` in your `ios/Runner/Info.plist`:

```xml
<key>LSApplicationQueriesSchemes</key>
<array>
  <string>tel</string>
</array>
```

## Example

```dart
import 'package:flutter/material.dart';
import 'package:flutter_phone_direct_caller/flutter_phone_direct_caller.dart';

void main() {
  runApp(
    MaterialApp(
      home: Scaffold(
        body: Center(
          child: ElevatedButton(
            onPressed: _callNumber,
            child: const Text('Call Number'),
          ),
        ),
      ),
    ),
  );
}

Future<void> _callNumber() async {
  const number = '08592119XXXX'; // set the number here
  final bool? res = await FlutterPhoneDirectCaller.callNumber(number);
}
```
