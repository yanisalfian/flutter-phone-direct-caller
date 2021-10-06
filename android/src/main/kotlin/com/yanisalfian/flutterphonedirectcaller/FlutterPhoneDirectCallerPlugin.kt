package com.yanisalfian.flutterphonedirectcaller

import android.Manifest
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.FlutterPlugin.FlutterPluginBinding
import io.flutter.plugin.common.MethodChannel
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.PluginRegistry.RequestPermissionsResultListener
import io.flutter.plugin.common.MethodCall
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.Intent
import android.net.Uri
import java.lang.Exception
import android.telephony.TelephonyManager
import android.content.Context
import android.app.Activity
import android.util.Log

/** FlutterPhoneDirectCallerPlugin  */
class FlutterPhoneDirectCallerPlugin : FlutterPlugin, ActivityAware {
    private var handler: FlutterPhoneDirectCallerHandler? = null
    override fun onAttachedToEngine(binding: FlutterPluginBinding) {
        handler = FlutterPhoneDirectCallerHandler()
        val channel = MethodChannel(
            binding.binaryMessenger, "flutter_phone_direct_caller"
        )
        channel.setMethodCallHandler(handler)
    }

    override fun onDetachedFromEngine(binding: FlutterPluginBinding) {}
    override fun onAttachedToActivity(activityPluginBinding: ActivityPluginBinding) {
        handler!!.setActivityPluginBinding(activityPluginBinding)
    }

    override fun onDetachedFromActivityForConfigChanges() {}
    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {}
    override fun onDetachedFromActivity() {}
}

internal class FlutterPhoneDirectCallerHandler :
    MethodCallHandler, RequestPermissionsResultListener {
    private var activityPluginBinding: ActivityPluginBinding? = null
    private var number: String? = null
    private var flutterResult: MethodChannel.Result? = null
    fun setActivityPluginBinding(activityPluginBinding: ActivityPluginBinding) {
        this.activityPluginBinding = activityPluginBinding
        activityPluginBinding.addRequestPermissionsResultListener(this)
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        flutterResult = result
        if (call.method == "callNumber") {
            number = call.argument("number")
            Log.d("Caller", "Message")
            number = number!!.replace("#".toRegex(), "%23")
            if (!number!!.startsWith("tel:")) {
                number = String.format("tel:%s", number)
            }
            if (permissionStatus != 1) {
                requestsPermission()
            } else {
                result.success(callNumber(number))
            }
        } else {
            result.notImplemented()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ): Boolean {
        if (requestCode == CALL_REQ_CODE) {
            for (r in grantResults) {
                if (r == PackageManager.PERMISSION_DENIED) {
                    flutterResult!!.success(false)
                    return false
                }
            }
            flutterResult!!.success(callNumber(number))
        }
        return true
    }

    private fun requestsPermission() {
        ActivityCompat.requestPermissions(activity, arrayOf(CALL_PHONE), CALL_REQ_CODE)
    }

    private val permissionStatus: Int
        get() = if (ContextCompat.checkSelfPermission(
                activity,
                CALL_PHONE
            ) == PackageManager.PERMISSION_DENIED
        ) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    CALL_PHONE
                )
            ) {
                -1
            } else {
                0
            }
        } else {
            1
        }

    private fun callNumber(number: String?): Boolean {
        return try {
            val intent = Intent(if (isTelephonyEnabled) Intent.ACTION_CALL else Intent.ACTION_VIEW)
            intent.data = Uri.parse(number)
            activity.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.d("Caller", "error: " + e.message)
            false
        }
    }

    private val isTelephonyEnabled: Boolean
        get() {
            val tm = activity.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            return tm.phoneType != TelephonyManager.PHONE_TYPE_NONE
        }
    private val activity: Activity
        get() = activityPluginBinding!!.activity

    companion object {
        private const val CALL_REQ_CODE = 0
        private const val CALL_PHONE = Manifest.permission.CALL_PHONE
    }
}