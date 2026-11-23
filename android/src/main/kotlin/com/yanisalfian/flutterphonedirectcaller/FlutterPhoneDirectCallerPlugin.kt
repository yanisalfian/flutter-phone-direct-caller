package com.yanisalfian.flutterphonedirectcaller

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.FlutterPlugin.FlutterPluginBinding
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.PluginRegistry.RequestPermissionsResultListener

/** FlutterPhoneDirectCallerPlugin */
class FlutterPhoneDirectCallerPlugin : FlutterPlugin, ActivityAware {
    private var channel: MethodChannel? = null
    private var handler: FlutterPhoneDirectCallerHandler? = null

    override fun onAttachedToEngine(binding: FlutterPluginBinding) {
        handler = FlutterPhoneDirectCallerHandler()
        channel = MethodChannel(binding.binaryMessenger, CHANNEL_NAME).apply {
            setMethodCallHandler(handler)
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPluginBinding) {
        channel?.setMethodCallHandler(null)
        channel = null
        handler?.detachActivity()
        handler = null
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        handler?.attachActivity(binding)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        handler?.detachActivity()
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        handler?.attachActivity(binding)
    }

    override fun onDetachedFromActivity() {
        handler?.detachActivity()
    }

    companion object {
        private const val CHANNEL_NAME = "flutter_phone_direct_caller"
    }
}

internal class FlutterPhoneDirectCallerHandler :
    MethodCallHandler, RequestPermissionsResultListener {

    private var activityPluginBinding: ActivityPluginBinding? = null
    private var pendingNumber: String? = null
    private var pendingResult: MethodChannel.Result? = null

    fun attachActivity(binding: ActivityPluginBinding) {
        detachActivity()
        activityPluginBinding = binding
        binding.addRequestPermissionsResultListener(this)
    }

    fun detachActivity() {
        activityPluginBinding?.removeRequestPermissionsResultListener(this)
        activityPluginBinding = null
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        if (call.method != "callNumber") {
            result.notImplemented()
            return
        }

        val rawNumber = call.argument<String>("number")?.trim()
        if (rawNumber.isNullOrEmpty()) {
            result.success(false)
            return
        }

        val currentActivity = activityPluginBinding?.activity
        if (currentActivity == null) {
            result.success(false)
            return
        }

        val formattedNumber = formatTelUri(rawNumber)
        if (hasCallPermission(currentActivity)) {
            result.success(callNumber(currentActivity, formattedNumber))
        } else {
            pendingResult?.success(false)
            pendingNumber = formattedNumber
            pendingResult = result
            ActivityCompat.requestPermissions(
                currentActivity,
                arrayOf(CALL_PHONE),
                CALL_REQ_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ): Boolean {
        if (requestCode != CALL_REQ_CODE) {
            return false
        }

        val result = pendingResult
        val number = pendingNumber
        pendingResult = null
        pendingNumber = null

        if (result == null || number == null) {
            return false
        }

        val granted = grantResults.isNotEmpty() &&
            grantResults.all { it == PackageManager.PERMISSION_GRANTED }

        if (!granted) {
            result.success(false)
            return true
        }

        val currentActivity = activityPluginBinding?.activity
        if (currentActivity == null) {
            result.success(false)
        } else {
            result.success(callNumber(currentActivity, number))
        }
        return true
    }

    private fun formatTelUri(rawNumber: String): String {
        val encoded = rawNumber.replace("#", "%23")
        return if (encoded.startsWith("tel:")) encoded else "tel:$encoded"
    }

    private fun hasCallPermission(activity: Activity): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun callNumber(activity: Activity, number: String): Boolean {
        return try {
            val action = if (isTelephonyEnabled(activity)) {
                Intent.ACTION_CALL
            } else {
                Intent.ACTION_DIAL
            }
            val intent = Intent(action, Uri.parse(number))
            activity.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initiate direct call", e)
            false
        }
    }

    private fun isTelephonyEnabled(activity: Activity): Boolean {
        val tm = activity.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        return tm != null && tm.phoneType != TelephonyManager.PHONE_TYPE_NONE
    }

    companion object {
        private const val TAG = "DirectCaller"
        private const val CALL_REQ_CODE = 1001
        private const val CALL_PHONE = Manifest.permission.CALL_PHONE
    }
}
