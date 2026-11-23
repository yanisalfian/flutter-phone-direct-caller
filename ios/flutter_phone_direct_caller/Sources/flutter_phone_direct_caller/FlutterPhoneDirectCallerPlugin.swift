import Flutter
import UIKit

public class FlutterPhoneDirectCallerPlugin: NSObject, FlutterPlugin {
    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(
            name: "flutter_phone_direct_caller",
            binaryMessenger: registrar.messenger()
        )
        let instance = FlutterPhoneDirectCallerPlugin()
        registrar.addMethodCallDelegate(instance, channel: channel)
    }

    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        guard call.method == "callNumber" else {
            result(FlutterMethodNotImplemented)
            return
        }

        guard let args = call.arguments as? [String: Any],
              let number = args["number"] as? String else {
            result(false)
            return
        }

        directCall(number: number, result: result)
    }

    private func directCall(number: String, result: @escaping FlutterResult) {
        let trimmed = number.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else {
            result(false)
            return
        }

        let phoneNumber = trimmed.hasPrefix("tel:") ? String(trimmed.dropFirst(4)) : trimmed
        var allowed = CharacterSet.decimalDigits
        allowed.insert(charactersIn: "+*,;")

        guard let encoded = phoneNumber.addingPercentEncoding(withAllowedCharacters: allowed),
              let url = URL(string: "tel:\(encoded)") else {
            result(false)
            return
        }

        DispatchQueue.main.async {
            guard UIApplication.shared.canOpenURL(url) else {
                result(false)
                return
            }

            UIApplication.shared.open(url, options: [:]) { success in
                result(success)
            }
        }
    }
}
