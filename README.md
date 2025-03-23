## Key Features ✨

- **Emergency SOS**  
  ⚡ Instant emergency call button with haptic feedback  
  📍 Automatic location sharing with emergency contacts  
  🔄 Fall detection using accelerometer data

- **Real-time Sensor Monitoring**  
  ❤️ Continuous heart rate tracking  
  🌀 Gyroscope orientation visualization  
  🌡️ Ambient temperature sensing

- **Health History**  
  📈 24-hour timeline of sensor data  
  📊 Interactive charts and graphs  
  ⏱️ Custom time range selection

- **Smart Alerts**  
  🔔 Abnormal heart rate notifications  
  ⚠️ Extreme temperature warnings  
  📉 Long-term health trend analysis

## Getting Started 🚀

### Prerequisites

- Android Studio Flamingo (2022.2.1) or newer
- Android SDK 34 (Android 14)
- Java Development Kit (JDK) 17+

### First Time Setup

1. Configure emergency number in:
   ```xml
   <!-- app/src/main/res/values/strings.xml -->
   <string name="emergency_number">YOUR_LOCAL_EMERGENCY_NUMBER</string>
   ```
2. Enable sensor permissions in:
   ```kotlin
   // app/src/main/AndroidManifest.xml
   <uses-permission android:name="android.permission.BODY_SENSORS"/>
   ```

## Screenshots 📸

| Emergency SOS               | Health Analytics                   |
| --------------------------- | ---------------------------------- |
| ![SOS](screenshots/sos.jpg) | ![Analytics](screenshots/data.jpg) |

## Troubleshooting ⚠️

**Issue**: Sensors not showing data  
**Fix**:

- Verify sensor permissions are enabled in device settings
- Ensure device has required hardware sensors
- Restart the application

**Issue**: Build errors related to Android SDK  
**Fix**:

- Verify Android SDK 34 is installed
- Ensure Android Gradle Plugin 8.1.0+ is used
- Clean and rebuild project (Build > Clean Project)

**Issue**: Emergency number not working  
**Fix**:

- Confirm number format uses country code (e.g., "+1234567890")
- Remove any spaces or special characters
- Ensure string resource is properly updated in strings.xml

**Issue**: App crashes on launch  
**Fix**:

- Clear app data/cache in device settings
- Uninstall and reinstall the app
- Ensure USB debugging is enabled for development builds
