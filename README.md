# Watch Anime World - Android APK

This is an Android APK wrapper for the Watch Anime World website (https://watchanimeworld.net/).

## Overview
This project wraps the website in a native Android application using WebView, allowing users to install and use the website as a native app on their Android devices.

## Features
- WebView-based wrapper for seamless website experience
- Internet connectivity check
- Back button navigation support
- Splash screen on startup
- Permissions for internet access

## Building the APK

### Prerequisites
- Android Studio installed
- Android SDK (API level 21 or higher)
- Java Development Kit (JDK)

### Steps to Build
1. Clone this repository
2. Open the project in Android Studio
3. Connect an Android device or use an emulator
4. Click `Build > Build Bundle(s) / APK(s) > Build APK(s)`
5. The APK will be generated in `app/build/outputs/apk/debug/`

## Installation
- Transfer the APK to your Android device
- Allow installation from unknown sources
- Tap the APK file to install

## Project Structure
```
watchanimeworld-apk/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java/
│   │   │   │   └── MainActivity.java
│   │   │   └── res/
│   │   │       ├── layout/
│   │   │       ├── values/
│   │   │       └── drawable/
│   ├── build.gradle
│   └── proguard-rules.pro
├── build.gradle
├── settings.gradle
└── README.md
```

## Permissions
- `INTERNET` - To access the website
- `ACCESS_NETWORK_STATE` - To check internet connectivity

## License
This is a wrapper application for educational purposes.
