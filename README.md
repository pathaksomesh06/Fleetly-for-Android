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

![image](https://github.com/user-attachments/assets/fc730789-daeb-4521-aba5-9ccfc8c1d315)
![image](https://github.com/user-attachments/assets/e136f48e-03a3-4ddd-9f6d-3ad24e061096)
![image](https://github.com/user-attachments/assets/ee6f6724-2827-4931-847e-43b6f6e5e20a)
![Uploading image.png…]()


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

## 🏢 Enterprise Deployment

Fleetly is designed for enterprise deployment through Managed Google Play. The app is distributed as a pre-built APK with enterprise-grade security and MDM integration.

### 📦 Distribution Package

**Current Version**: v3.2 (Version Code 12)

**APK Details**:
- **Package Name**: `com.themavericklabs.fleetly`
- **SHA1 Signature**: `65:1C:77:32:18:B4:16:D4:19:A1:DB:01:A1:54:6A:01:FE:B8:7A:B3`
- **Base64 Signature Hash**: `ZRx3Mhi0FtQZodsBoVRqAf64erM=`

### 🔧 Azure AD Configuration

For each tenant deploying Fleetly, configure the following in Azure AD:

1. **Create App Registration**
   - Register a new application in Azure Portal
   - Add redirect URI: `msauth://com.themavericklabs.fleetly/ZRx3Mhi0FtQZodsBoVRqAf64erM=`
   - Grant required permissions:
     - `User.Read`
     - `DeviceManagementManagedDevices.ReadWrite.All`

2. **Note the Client ID** for MDM configuration

### 📱 Managed Google Play Deployment

1. **Upload APK**
   - Upload the Fleetly APK to your Managed Google Play store
   - Configure as a private app

2. **Configure MDM Settings**
   - Set app restriction `client_id` to your Azure AD Client ID
   - Deploy to managed devices

3. **App Restrictions Configuration**
   ```xml
   <restriction
       android:key="client_id"
       android:title="Azure AD Client ID"
       android:restrictionType="string"
       android:description="Enter your Azure AD App Registration Client ID"
       android:defaultValue="" />
  
<img width="1512" height="750" alt="Screenshot 2025-07-16 at 14 39 48" src="https://github.com/user-attachments/assets/0da5f256-0809-434b-8537-d4d2f3a02710" />
<img width="643" height="557" alt="Screenshot 2025-07-16 at 14 40 06" src="https://github.com/user-attachments/assets/a06dae39-d875-4e24-badf-410f38c21560" />


## 📦 Releases

### Latest Release: v3.2 (Version Code 12)

- **Download**: ([releases/Fleetly.apk](https://github.com/pathaksomesh06/Fleetly-for-Android/releases/tag/v))
- **Release Date**: July 16, 2024
- **Changes**:
  - Initial Android release
  - Modern Jetpack Compose UI
  - Full Intune device management capabilities
  - Enterprise-grade security with MSAL

### 🔐 Security Features

- **Pre-built APK**: No source code distribution required
- **Signature Verification**: Consistent signature across all tenants
- **MDM Integration**: Secure configuration through app restrictions
- **Azure AD Authentication**: Enterprise-grade identity management
- **Network Security**: HTTPS-only API calls
- **Data Protection**: Secure token storage

## 🏗️ Architecture

```
Enterprise Deployment Flow:
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Azure AD      │    │  Managed Google  │    │   Intune MDM    │
│ App Registration│    │      Play         │    │   Devices       │
└─────────────────┘    └──────────────────┘    └─────────────────┘
         │                       │                       │
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│  Client ID      │    │   Fleetly APK    │    │  App Restrictions│
│  Redirect URI   │    │   (Pre-built)    │    │  Configuration  │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

## 📋 Multi-Tenant Support

Fleetly supports deployment across multiple tenants:

- **Same APK**: Single APK works across all tenants
- **Tenant Isolation**: Each tenant has independent configuration
- **Centralized Updates**: Controlled APK distribution
- **No Source Code**: Tenants only need the APK and configuration

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

**Fleetly for Android** - Simplifying Intune device management for IT professionals. 
