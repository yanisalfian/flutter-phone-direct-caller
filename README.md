# flutter_phone_direct_caller

Call number directly from flutter app.

## Usage

Add dependency to pubspec.yaml file
```
flutter_phone_direct_caller:
  git:
    url: https://github.com/yanisalfian/flutter-phone-direct-caller.git
```

## Example

```dart
import 'package:flutter/material.dart';
import 'package:flutter_phone_direct_caller/flutter_phone_direct_caller.dart';

void main() {
  runApp(Scaffold(
    body: Center(
      child: RaisedButton(
        onPressed: _callNumber,
        child: Text('Call Number'),
      ),
    ),
  ));
}

_callNumber() async{
  const number = '08592119XXXX';
  bool res = await FlutterPhoneDirectCaller.callNumber(number);
}
```

## iOS

Add this to your ```info.plist``` under ```dict``` 
```
<key>LSApplicationQueriesSchemes</key>
<array>
  <string>tel</string>
</array>
```

