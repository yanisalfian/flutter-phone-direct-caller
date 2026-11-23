// swift-tools-version: 5.9
// The swift-tools-version declares the minimum version of Swift required to build this package.

import PackageDescription

let package = Package(
    name: "flutter_phone_direct_caller",
    platforms: [
        .iOS("13.0"),
    ],
    products: [
        .library(name: "flutter-phone-direct-caller", targets: ["flutter_phone_direct_caller"]),
    ],
    dependencies: [
        .package(name: "FlutterFramework", path: "../FlutterFramework"),
    ],
    targets: [
        .target(
            name: "flutter_phone_direct_caller",
            dependencies: [
                .product(name: "FlutterFramework", package: "FlutterFramework"),
            ]
        ),
    ]
)
