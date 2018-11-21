import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_phone_direct_caller/flutter_phone_direct_caller.dart';

void main() => runApp(new MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => new _MyAppState();
}

class _MyAppState extends State<MyApp> {
  TextEditingController _numberCtrl = new TextEditingController();

  @override
  void initState() {
    super.initState();
    _numberCtrl.text = "085921191121";
  }

  @override
  Widget build(BuildContext context) {
    return new MaterialApp(
      home: new Scaffold(
        appBar: new AppBar(
          title: const Text('Plugin example app'),
        ),
        body: new Column(
          children:<Widget>[
            Padding(
              padding: EdgeInsets.all(8.0),
              child: TextField(
                controller: _numberCtrl,
                decoration: InputDecoration(
                  labelText: "Phone Number"
                ),
                keyboardType: TextInputType.number,
              ),
            ),
            RaisedButton(
              child: Text("Test Call"),
              onPressed: () async{
                FlutterPhoneDirectCaller.callNumber(_numberCtrl.text);
              },
            )
          ]
        ),
      ),
    );
  }
}
