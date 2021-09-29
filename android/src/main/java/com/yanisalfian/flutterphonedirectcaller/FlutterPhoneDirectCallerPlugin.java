package com.yanisalfian.flutterphonedirectcaller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;

/** FlutterPhoneDirectCallerPlugin */
public class FlutterPhoneDirectCallerPlugin implements FlutterPlugin, ActivityAware {
  private FlutterPhoneDirectCallerHandler handler;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
    handler = new FlutterPhoneDirectCallerHandler(binding);
    MethodChannel channel = new MethodChannel(
            binding.getBinaryMessenger(), "flutter_phone_direct_caller"
    );
    channel.setMethodCallHandler(handler);
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {

  }

  @Override
  public void onAttachedToActivity(@NonNull ActivityPluginBinding activityPluginBinding) {
    handler.setActivityPluginBinding(activityPluginBinding);
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    
  }

  @Override
  public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {

  }

  @Override
  public void onDetachedFromActivity() {

  }
}

class FlutterPhoneDirectCallerHandler implements MethodCallHandler, PluginRegistry.RequestPermissionsResultListener {
  public FlutterPlugin.FlutterPluginBinding binding;
  public ActivityPluginBinding activityPluginBinding;

  private static final int CALL_REQ_CODE = 0;
  private static final String CALL_PHONE = android.Manifest.permission.CALL_PHONE;
  private String number;
  private Result flutterResult;

  public FlutterPhoneDirectCallerHandler(FlutterPlugin.FlutterPluginBinding binding){
    this.binding = binding;
  }

  public void setActivityPluginBinding(ActivityPluginBinding activityPluginBinding) {
    this.activityPluginBinding = activityPluginBinding;
    activityPluginBinding.addRequestPermissionsResultListener(this);
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    flutterResult = result;
    if(call.method.equals("callNumber")) {
      this.number = call.argument("number");
      Log.d("Caller","Message");
      this.number = this.number.replaceAll("#", "%23");
      if (!this.number.startsWith("tel:")) {
        this.number = String.format("tel:%s", this.number);
      }
      if(getPermissionStatus() != 1){
        requestsPermission();
      } else {
        result.success(callNumber(this.number));
      }
    } else {
      result.notImplemented();
    }
  }

  @Override
  public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    if (requestCode == CALL_REQ_CODE) {
      for (int r : grantResults) {
        if (r == PackageManager.PERMISSION_DENIED) {
          flutterResult.success(false);
          return false;
        }
      }
      flutterResult.success(callNumber(this.number));
    }
    return true;
  }

  private void requestsPermission() {
    ActivityCompat.requestPermissions(getActivity(), new String[]{CALL_PHONE}, CALL_REQ_CODE );
  }

  private int getPermissionStatus() {
    if (ContextCompat.checkSelfPermission(getActivity(), CALL_PHONE) == PackageManager.PERMISSION_DENIED) {
      if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), CALL_PHONE)) {
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
      getActivity().startActivity(intent);
      return true;
    } catch (Exception e){
      Log.d("Caller","error: " + e.getMessage());
      return false;
    }
  }

  private boolean isTelephonyEnabled() {
    TelephonyManager tm = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
    return tm != null && tm.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE;
  }

  private Activity getActivity() {
    return activityPluginBinding.getActivity();
  }
}