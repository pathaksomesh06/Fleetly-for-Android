# Fleetly for Android

A modern Android application for managing Intune devices with a beautiful, intuitive interface built using Jetpack Compose.

## 🚀 Features

- **Device Management**: View and manage all your Intune devices from anywhere
- **Cross-Platform Support**: Manage Windows, Mac, iOS, and Android devices
- **Device Actions**: Perform actions like sync, wipe, restart, remote lock, and more
- **Security Features**: Access BitLocker recovery keys and LAPS credentials securely
- **Real-time Monitoring**: Monitor compliance status and device health
- **Search & Filter**: Find devices quickly with powerful search and filtering
- **Enterprise Security**: Built with Microsoft Authentication Library (MSAL)

## 📱 Screenshots

*Screenshots will be added here*

## 🛠️ Technology Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Clean Architecture
- **Dependency Injection**: Hilt
- **Networking**: Retrofit + OkHttp
- **Authentication**: Microsoft Authentication Library (MSAL)
- **Build System**: Gradle with Kotlin DSL
- **Minimum SDK**: API 26 (Android 8.0)
- **Target SDK**: API 34 (Android 14)

## 📋 Prerequisites

- Android Studio Arctic Fox or later
- JDK 17
- Android SDK API 26+
- Azure AD App Registration with proper permissions

## 🔧 Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/pathaksomesh06/Fleetly-for-Android.git
   cd Fleetly-for-Android
   ```

2. **Configure Azure AD App Registration**
   - Create an app registration in Azure Portal
   - Add redirect URI: `msauth://com.themavericklabs.fleetly/[YOUR_SIGNATURE_HASH]`
   - Grant required permissions:
     - `User.Read`
     - `DeviceManagementManagedDevices.ReadWrite.All`

3. **Configure MDM Settings**
   - Add your Azure AD Client ID to your MDM configuration
   - Deploy the app through your MDM solution

4. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ```

## 📦 Releases

### Latest Release: v3.2 (Version Code 12)

- **Download**: [Fleetly.apk](releases/Fleetly.apk)
- **Release Date**: July 16, 2024
- **Changes**:
  - Initial Android release
  - Modern Jetpack Compose UI
  - Full Intune device management capabilities
  - Enterprise-grade security with MSAL

## 🏗️ Project Structure

```
app/
├── src/main/
│   ├── java/com/themavericklabs/fleetly/
│   │   ├── auth/           # MSAL authentication
│   │   ├── config/         # App configuration
│   │   ├── data/           # Data models
│   │   ├── network/        # API services
│   │   ├── ui/             # UI components
│   │   │   ├── screens/    # App screens
│   │   │   ├── theme/      # Material 3 theme
│   │   │   └── viewmodels/ # ViewModels
│   │   └── FleetlyApplication.kt
│   └── res/                # Resources
└── build.gradle.kts        # App-level build config
```

## 🔐 Security

- **Authentication**: Microsoft Authentication Library (MSAL)
- **Network Security**: HTTPS-only API calls
- **Data Protection**: Secure token storage
- **Enterprise Ready**: MDM deployment support

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Microsoft for Intune API and MSAL
- Google for Jetpack Compose and Android development tools
- The Android developer community

## 📞 Support

For support and questions:
- Create an issue in this repository
- Contact: [Your contact information]

---

**Fleetly for Android** - Simplifying Intune device management for IT professionals. 