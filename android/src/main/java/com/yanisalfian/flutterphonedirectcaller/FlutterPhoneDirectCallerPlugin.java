package com.yanisalfian.flutterphonedirectcaller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.telephony.TelephonyManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/** FlutterPhoneDirectCallerPlugin */
public class FlutterPhoneDirectCallerPlugin implements MethodCallHandler, PluginRegistry.RequestPermissionsResultListener {
  private Registrar registrar;
    public static final int CALL_REQ_CODE = 0;
    public static final int PERMISSION_DENIED_ERROR = 20;
    public static final String CALL_PHONE = android.Manifest.permission.CALL_PHONE;
    private String number;

  /** Plugin registration. */
  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "flutter_phone_direct_caller");
    FlutterPhoneDirectCallerPlugin flutterPhoneDirectCallerPlugin = new FlutterPhoneDirectCallerPlugin(registrar);
    channel.setMethodCallHandler(flutterPhoneDirectCallerPlugin);
    registrar.addRequestPermissionsResultListener(flutterPhoneDirectCallerPlugin);
  }

  private FlutterPhoneDirectCallerPlugin(Registrar registrar){
    this.registrar = registrar;
  }

  @Override
  public void onMethodCall(MethodCall call, Result result) {
    if (call.method.equals("getPlatformVersion")) {
      result.success("Android " + android.os.Build.VERSION.RELEASE);
    } else if(call.method.equals("callNumber")) {
      this.number = call.argument("number");
      Log.d("Caller","Message");
      this.number = this.number.replaceAll("#", "%23");
      if (!this.number.startsWith("tel:")) {
        this.number = String.format("tel:%s", this.number);
      }
      if(getPermissionStatus() != 1){
        requestsPermission();
      } else {
        if (callNumber(this.number)) {
          result.success(true);
        } else {
          result.success(false);
        }
      }
    } else {
      result.notImplemented();
    }
  }

  private void requestsPermission() {
    Activity activity = registrar.activity();
    ActivityCompat.requestPermissions(activity, new String[]{CALL_PHONE}, CALL_REQ_CODE );
  }

  private int getPermissionStatus() {
    Activity activity = registrar.activity();
    if (ContextCompat.checkSelfPermission(registrar.activity(), CALL_PHONE) == PackageManager.PERMISSION_DENIED) {
      if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, CALL_PHONE)) {
        return -1;
      } else {
        return 0;
      }
    } else {
      return 1;
    }
  }

  private boolean callNumber(String number){

    try{
      Intent intent = new Intent(isTelephonyEnabled() ? Intent.ACTION_CALL : Intent.ACTION_VIEW);
      intent.setData(Uri.parse(number));

      registrar.activity().startActivity(intent);

      return true;
    } catch (Exception e){
      Log.d("Caller","error: " + e.getMessage());
      return false;
    }
  }

  private boolean isTelephonyEnabled() {
    TelephonyManager tm = (TelephonyManager) registrar.activity().getSystemService(Context.TELEPHONY_SERVICE);
    return tm != null && tm.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE;
  }

  @Override
  public boolean onRequestPermissionsResult(int requestCode, String[] strings, int[] ints) {
    for (int r : ints) {
      if (r == PackageManager.PERMISSION_DENIED) {
        return false;
      }
    }
    switch (requestCode) {
      case CALL_REQ_CODE:
        callNumber(this.number);
        break;
    }
    return true;
  }
}
