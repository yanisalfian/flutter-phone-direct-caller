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
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;

import androidx.annotation.NonNull;

/**
 * FlutterPhoneDirectCallerPlugin
 */
public class FlutterPhoneDirectCallerPlugin implements FlutterPlugin, ActivityAware, MethodCallHandler, PluginRegistry.RequestPermissionsResultListener {
    public static final int CALL_REQ_CODE = 0;
    public static final int PERMISSION_DENIED_ERROR = 20;
    public static final String CALL_PHONE = android.Manifest.permission.CALL_PHONE;
    private String number;
    private Result flutterResult;

    private static final String CHANNEL_QUERY = "flutter_phone_direct_caller";
    private static final String TAG = "phone_direct_caller";
    private ActivityPluginBinding activityPluginBinding;
    private Activity activity;
    private Context ctx;


//    /**
//     * Constructor
//     */
//    private FlutterPhoneDirectCallerPlugin(Registrar registrar) {
//        this.registrar = registrar;
//    }

    /**
     * Plugin registration. Android V1 embedding
     */
    @Deprecated
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "flutter_phone_direct_caller");
        FlutterPhoneDirectCallerPlugin flutterPhoneDirectCallerPlugin = new FlutterPhoneDirectCallerPlugin();
        flutterPhoneDirectCallerPlugin.activity = registrar.activity();
        channel.setMethodCallHandler(flutterPhoneDirectCallerPlugin);
        registrar.addRequestPermissionsResultListener(flutterPhoneDirectCallerPlugin);
    }

    /** Plugin registration. Android V2 embedding */
    private void init(BinaryMessenger binaryMessenger, Context applicationContext) {
        Log.d(TAG, "init. Messanger:" + binaryMessenger + " Context:" + applicationContext);
        final MethodChannel channel = new MethodChannel(binaryMessenger, CHANNEL_QUERY);
        channel.setMethodCallHandler(this);
        ctx = applicationContext;
    }

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        Log.d(TAG, "onAttachedToEngine");
        init(flutterPluginBinding.getBinaryMessenger(), flutterPluginBinding.getApplicationContext());
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        //NO-OP
        Log.d(TAG, "onDetachedFromEngine");
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding activityPluginBinding) {
        this.activityPluginBinding = activityPluginBinding;
        activityPluginBinding.addRequestPermissionsResultListener(this);
        Log.d(TAG, "onAttachedToActivity");
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        Log.d(TAG, "onDetachedFromActivityForConfigChanges");
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding activityPluginBinding) {
        Log.d(TAG, "onReattachedToActivityForConfigChanges");
    }

    @Override
    public void onDetachedFromActivity() {
        Log.d(TAG, "onDetachedFromActivity");
        if (activityPluginBinding != null) {
            activityPluginBinding.removeRequestPermissionsResultListener(this);
            activityPluginBinding = null;
        }
    }

    /**
     * Main plugin methods
     */
    @Override
    public void onMethodCall(MethodCall call, Result result) {
        flutterResult = result;
        if (call.method.equals("callNumber")) {
            this.number = call.argument("number");
            Log.d("Caller", "Message");
            this.number = this.number.replaceAll("#", "%23");
            if (!this.number.startsWith("tel:")) {
                this.number = String.format("tel:%s", this.number);
            }
            if (getPermissionStatus() != 1) {
                requestsPermission();
            } else {
                result.success(callNumber(this.number));
            }
        } else {
            result.notImplemented();
        }
    }

    private void requestsPermission() {
        ActivityCompat.requestPermissions(activity, new String[]{CALL_PHONE}, CALL_REQ_CODE);
    }

    private int getPermissionStatus() {
        if (ContextCompat.checkSelfPermission(activity, CALL_PHONE) == PackageManager.PERMISSION_DENIED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, CALL_PHONE)) {
                return -1;
            } else {
                return 0;
            }
        } else {
            return 1;
        }
    }

    private boolean callNumber(String number) {

        try {
            Intent intent = new Intent(isTelephonyEnabled() ? Intent.ACTION_CALL : Intent.ACTION_VIEW);
            intent.setData(Uri.parse(number));

            activity.startActivity(intent);

            return true;
        } catch (Exception e) {
            Log.d("Caller", "error: " + e.getMessage());
            return false;
        }
    }

    private boolean isTelephonyEnabled() {
        TelephonyManager tm = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
        return tm != null && tm.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE;
    }

    @Override
    public boolean onRequestPermissionsResult(int requestCode, String[] strings, int[] ints) {
        switch (requestCode) {
            case CALL_REQ_CODE:
                for (int r : ints) {
                    if (r == PackageManager.PERMISSION_DENIED) {
                        flutterResult.success(false);
                        return false;
                    }
                }
                flutterResult.success(callNumber(this.number));
                break;
        }
        return true;
    }
}
