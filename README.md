# 🛡️ CrashGuard

[![Maven Central](https://img.shields.io/maven-central/v/io.github.alims-repo/crash-guard)](https://central.sonatype.com/artifact/io.github.alims-repo/crash-guard)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Android API](https://img.shields.io/badge/API-22%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.21-blue.svg?logo=kotlin)](http://kotlinlang.org)

**CrashGuard** is an industry-grade Android library that provides beautiful, customizable crash screens for your applications. Built with Clean Architecture principles, it offers extensive configuration options for both debug and release builds, crash log management, and seamless integration with analytics platforms.

## ✨ Features

### 🎨 Dual Crash Screens
- **User-Friendly Screen** - Clean, professional crash screen for release builds
- **Developer Screen** - Detailed crash information with full stack traces for debugging
- **Fully Customizable** - Provide your own custom activities for complete control

### 📊 Comprehensive Crash Data
- Exception type and message
- Complete stack trace
- Device information (manufacturer, model, Android version)
- Memory statistics (available/total)
- Battery level and charging status
- Network connectivity type
- Activity stack trace
- Screen orientation and resolution
- Disk space information
- Custom data support

### 💾 Persistent Crash Logs
- Automatic crash log storage
- Configurable log limits (default: 50 crashes)
- Export crashes as JSON or text files
- Programmatic access to crash history
- Automatic cleanup of old logs

### 🔧 Advanced Configuration
- Debug/Release mode switching
- Auto-restart functionality with configurable delay
- Crash interceptors for custom handling
- Analytics integration (Firebase, Crashlytics, etc.)
- Custom data providers
- Secure mode (sanitizes sensitive information)
- Exception exclusion support
- Notification support
- Multiple lifecycle callbacks

### 🏗️ Clean Architecture
- Separation of concerns (Domain, Data, Presentation)
- Easy to test and maintain
- Follows SOLID principles
- Kotlin Coroutines support

## 📦 Installation

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("io.github.alims-repo:crash-guard:1.0.0")
}
```

### Gradle (Groovy)

```groovy
dependencies {
    implementation 'io.github.alims-repo:crash-guard:1.0.0'
}
```

### Maven

```xml
<dependency>
    <groupId>io.github.alims-repo</groupId>
    <artifactId>crash-guard</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 🚀 Quick Start

### Basic Setup

1. **Initialize in your Application class:**

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        CrashGuard.install(
            application = this,
            config = CrashGuardConfig.Builder(this)
                .debugMode(BuildConfig.DEBUG)
                .build()
        )
    }
}
```

2. **Add to AndroidManifest.xml:**

```xml
<application
    android:name=".MyApplication"
    ...>
    <!-- Your activities -->
    </application>
```

That's it! CrashGuard is now active and will display appropriate crash screens based on your build type.

## 📖 Advanced Usage

### Full Configuration Example

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val config = CrashGuardConfig.Builder(this)
            // Basic Settings
            .debugMode(BuildConfig.DEBUG)
            .enableLogging(true)
            .enableCrashReporting(true)
            .maxCrashLogs(100)

            // Custom Activities
            .customUserActivity(MyCustomUserCrashActivity::class)
            .customDeveloperActivity(MyCustomDeveloperCrashActivity::class)

            // Auto-Restart
            .enableAutoRestart(enabled = true, delayMs = 2000L)

            // Notifications
            .showNotification(
                enabled = true,
                config = NotificationConfig(
                    channelId = "app_crashes",
                    channelName = "App Crashes",
                    notificationTitle = "MyApp Crashed",
                    notificationMessage = "Tap to restart",
                    smallIcon = R.drawable.ic_notification
                )
            )

            // Analytics Integration
            .enableAnalytics(
                enabled = true,
                provider = object : AnalyticsProvider {
                    override fun logCrash(crashData: CrashData) {
                        // Send to your analytics platform
                        FirebaseCrashlytics.getInstance()
                            .recordException(crashData.exception)
                    }

                    override fun logEvent(eventName: String, params: Map<String, Any>) {
                        // Log analytics events
                    }
                }
            )

            // Custom Data
            .customDataProvider(object : CustomDataProvider {
                override fun provideCustomData(context: Context): Map<String, String> {
                    return mapOf(
                        "user_id" to getUserId(),
                        "session_id" to getSessionId(),
                        "build_type" to BuildConfig.BUILD_TYPE
                    )
                }
            })

            // Crash Interception
            .crashInterceptor(object : CrashInterceptor {
                override fun onCrashIntercepted(
                    throwable: Throwable,
                    thread: Thread
                ): Boolean {
                    // Return true to prevent crash screen
                    return false
                }
            })

            // Security
            .enableSecureMode(true) // Sanitizes sensitive data

            // Exception Exclusion
            .excludeException(InterruptedException::class)
            .excludeException(CancellationException::class)

            // Lifecycle Callbacks
            .onCrashDetected { crashData ->
                Log.e("CrashGuard", "Crash detected: ${crashData.exceptionType}")
            }
            .onBeforeCrashScreen { crashData ->
                // Return false to prevent showing crash screen
                true
            }
            .onAfterCrashScreen { crashData ->
                // Cleanup after crash screen dismissed
            }
            .onInitialized {
                Log.d("CrashGuard", "Initialized successfully")
            }

            .build()

        CrashGuard.install(this, config)
    }
}
```

### Custom Crash Activity

Create your own crash screen by extending `BaseCrashActivity`:

```kotlin
class MyCustomCrashActivity : BaseCrashActivity(), CrashActivityContract {

    override fun setupUI() {
        setContentView(R.layout.activity_my_crash)

        // Access crash data
        crashData?.let { crash ->
            findViewById<TextView>(R.id.errorMessage).text = crash.exceptionMessage
            findViewById<TextView>(R.id.stackTrace).text = crash.stackTrace
        }

        // Setup buttons
        findViewById<Button>(R.id.btnRestart).setOnClickListener {
            onRestartRequested()
        }

        findViewById<Button>(R.id.btnClose).setOnClickListener {
            onCloseRequested()
        }
    }

    override fun onCrashDataReceived(crashData: CrashData) {
        // Handle crash data
    }

    override fun onRestartRequested() {
        restartApp() // Provided by BaseCrashActivity
    }

    override fun onCloseRequested() {
        closeApp() // Provided by BaseCrashActivity
    }
}
```

### Accessing Crash Logs Programmatically

```kotlin
// Get crash repository
val storage = CrashLogStorage(context, CrashGuard.getConfig())
val repository = CrashRepositoryImpl(storage)

// In a coroutine
lifecycleScope.launch {
    // Get all crashes
    repository.getAllCrashes().onSuccess { crashes ->
        crashes.forEach { crash ->
            println("Crash: ${crash.exceptionType} at ${crash.formattedTimestamp}")
        }
    }

    // Get last crash
    repository.getLastCrash().onSuccess { crash ->
        crash?.let {
            println("Last crash: ${it.exceptionMessage}")
        }
    }

    // Get crash count
    repository.getCrashCount().onSuccess { count ->
        println("Total crashes: $count")
    }

    // Delete specific crash
    repository.deleteCrash(crashId)

    // Clear all crashes
    repository.deleteAllCrashes()
}
```

### Export Crash Reports

```kotlin
val storage = CrashLogStorage(context, CrashGuard.getConfig())

// Export as JSON
val jsonReport = storage.exportCrashesAsJson()

// Export specific crash as text file
crashData?.let { crash ->
    val file = storage.exportCrashAsText(crash)
    // Share or upload the file
}
```

## 🎨 Customization Options

### Configuration Builder Options

| Method | Description | Default |
|--------|-------------|---------|
| `debugMode(Boolean)` | Enable developer crash screen | `false` |
| `enableLogging(Boolean)` | Enable crash log storage | `true` |
| `enableCrashReporting(Boolean)` | Enable crash reporting | `true` |
| `maxCrashLogs(Int)` | Maximum stored crash logs | `50` |
| `customUserActivity(KClass)` | Custom user crash activity | `UserCrashActivity` |
| `customDeveloperActivity(KClass)` | Custom developer crash activity | `DeveloperCrashActivity` |
| `crashInterceptor(CrashInterceptor)` | Intercept crashes before handling | `null` |
| `logStoragePath(String)` | Custom storage path | App files dir |
| `enableAutoRestart(Boolean, Long)` | Auto-restart with delay | `false` |
| `showNotification(Boolean, NotificationConfig)` | Show crash notification | `false` |
| `enableAnalytics(Boolean, AnalyticsProvider)` | Analytics integration | `false` |
| `onCrashDetected((CrashData) -> Unit)` | Callback when crash detected | `null` |
| `onBeforeCrashScreen((CrashData) -> Boolean)` | Before showing crash screen | `null` |
| `onAfterCrashScreen((CrashData) -> Unit)` | After crash screen dismissed | `null` |
| `onInitialized(() -> Unit)` | When library is initialized | `null` |
| `customDataProvider(CustomDataProvider)` | Add custom crash context | `null` |
| `enableSecureMode(Boolean)` | Sanitize sensitive data | `false` |
| `excludeException(KClass)` | Exclude exception types | None |

### CrashData Properties

The `CrashData` model contains comprehensive crash information:

```kotlin
data class CrashData(
    val id: String,                          // Unique crash ID
    val exception: Throwable,                // The exception
    val threadName: String,                  // Thread name
    val timestamp: Long,                     // Crash timestamp
    val appVersion: String,                  // App version
    val appPackage: String,                  // Package name
    val deviceInfo: DeviceInfo,              // Device details
    val customData: Map<String, String>,     // Custom data
    val activityStack: List<String>,         // Activity stack
    val availableMemory: Long,               // Available RAM
    val totalMemory: Long,                   // Total RAM
    val diskSpace: Long,                     // Free disk space
    val batteryLevel: Float,                 // Battery percentage
    val isCharging: Boolean,                 // Charging status
    val networkType: String,                 // Network type
    val orientation: String                  // Screen orientation
)
```

## 🔌 Integration Examples

### Firebase Crashlytics Integration

```kotlin
.enableAnalytics(
    enabled = true,
    provider = object : AnalyticsProvider {
        override fun logCrash(crashData: CrashData) {
            val crashlytics = FirebaseCrashlytics.getInstance()

            // Set custom keys
            crashlytics.setCustomKey("crash_id", crashData.id)
            crashlytics.setCustomKey("app_version", crashData.appVersion)
            crashlytics.setCustomKey("device_model", crashData.deviceInfo.model)

            // Add custom data
            crashData.customData.forEach { (key, value) ->
                crashlytics.setCustomKey(key, value)
            }

            // Record exception
            crashlytics.recordException(crashData.exception)
        }

        override fun logEvent(eventName: String, params: Map<String, Any>) {
            FirebaseAnalytics.getInstance(context)
                .logEvent(eventName, bundleOf(*params.toList().toTypedArray()))
        }
    }
)
```

### Sentry Integration

```kotlin
.enableAnalytics(
    enabled = true,
    provider = object : AnalyticsProvider {
        override fun logCrash(crashData: CrashData) {
            Sentry.captureException(crashData.exception) { scope ->
                scope.setTag("crash_id", crashData.id)
                scope.setTag("device", crashData.deviceInfo.model)
                scope.setExtra("crash_report", crashData.getFullReport())
            }
        }

        override fun logEvent(eventName: String, params: Map<String, Any>) {
            Sentry.captureMessage(eventName)
        }
    }
)
```

### Remote Logging

```kotlin
.onCrashDetected { crashData ->
    // Send crash to your backend
    lifecycleScope.launch {
        try {
            api.reportCrash(
                crashId = crashData.id,
                report = crashData.toJson(),
                timestamp = crashData.timestamp
            )
        } catch (e: Exception) {
            Log.e("CrashGuard", "Failed to report crash", e)
        }
    }
}
```

## 🧪 Testing

### Manual Crash Testing

```kotlin
// Trigger a test crash
CrashGuard.triggerCrash(RuntimeException("Test crash"))

// Or in a button click
findViewById<Button>(R.id.btnTestCrash).setOnClickListener {
    throw RuntimeException("Testing crash screen")
}
```

### Unit Testing

```kotlin
@Test
fun `test crash data collection`() = runTest {
        val exception = RuntimeException("Test exception")
        val repository = CrashRepositoryImpl(mockStorage)

        val crashData = CrashData(
            exception = exception,
            threadName = "main",
            appVersion = "1.0.0",
            appPackage = "com.test",
            deviceInfo = DeviceInfo()
        )

        repository.saveCrash(crashData).onSuccess {
            // Assert crash was saved
            assertTrue(true)
        }
    }
```

## 🔒 ProGuard / R8

Add these rules to your `proguard-rules.pro`:

```proguard
# CrashGuard
-keep class io.github.alimsrepo.crashguard.** { *; }
-keep interface io.github.alimsrepo.crashguard.** { *; }
-keepnames class io.github.alimsrepo.crashguard.domain.model.** { *; }

# Serialization
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep custom crash activities
-keep class * extends io.github.alimsrepo.crashguard.presentation.ui.base.BaseCrashActivity { *; }
```

## 📱 Permissions

CrashGuard uses the following permissions (all optional):

```xml
<!-- For network type detection -->
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <!-- For battery status detection -->
<uses-permission android:name="android.permission.BATTERY_STATS"
tools:ignore="ProtectedPermissions" />
```

These permissions are already included in the library manifest and will be merged automatically.

## 🏗️ Architecture

CrashGuard follows Clean Architecture principles:

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│  (UI, Activities, View Logic)           │
├─────────────────────────────────────────┤
│          Domain Layer                   │
│  (Use Cases, Business Logic, Models)    │
├─────────────────────────────────────────┤
│           Data Layer                    │
│  (Repository, Storage, Data Sources)    │
└─────────────────────────────────────────┘
```

### Key Components

- **CrashGuard** - Main entry point and facade
- **CrashExceptionHandler** - Core exception handling logic
- **CrashRepository** - Data access abstraction
- **CrashLogStorage** - File-based storage implementation
- **BaseCrashActivity** - Base class for crash screens
- **CrashData** - Rich domain model for crash information

## 📊 Crash Data Structure

When a crash occurs, CrashGuard collects:

1. **Exception Details**
    - Type, message, and full stack trace
    - Thread information

2. **Device Information**
    - Manufacturer, model, brand
    - Android version and SDK level
    - CPU architecture
    - Screen density and resolution

3. **System Status**
    - Available and total memory
    - Free disk space
    - Battery level and charging status
    - Network connectivity type

4. **App Context**
    - App version and package name
    - Activity stack
    - Screen orientation
    - Custom data (if provided)

## 🎯 Use Cases

### Production Apps
- Show user-friendly error messages
- Collect crash data for analysis
- Auto-restart app for better UX

### Development
- Debug crashes with full stack traces
- Test error handling
- Export crashes for bug reports

### QA/Testing
- Document crashes during testing
- Share crash reports with developers
- Track crash frequency

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request. For major changes, please open an issue first to discuss what you would like to change.

### Development Setup

1. Clone the repository
2. Open in Android Studio
3. Build the project
4. Run tests

### Guidelines

- Follow Clean Architecture principles
- Write unit tests for new features
- Update documentation
- Follow Kotlin coding conventions

## 📄 License

```
MIT License

Copyright (c) 2024 CrashGuard

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## 🙏 Acknowledgments

- Built with ❤️ using Kotlin
- Follows Clean Architecture by Uncle Bob
- Inspired by the Android development community

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/Alims-Repo/crash-guard/issues)
- **Email**: sourav.0.alim@gmail.com
- **Documentation**: [Wiki](https://github.com/Alims-Repo/crash-guard/wiki)
- **Discussions**: [GitHub Discussions](https://github.com/Alims-Repo/crash-guard/discussions)

## 🔗 Links

- [Maven Central](https://central.sonatype.com/artifact/io.github.alims-repo/crash-guard)
- [GitHub Repository](https://github.com/Alims-Repo/crash-guard)
- [Sample App](https://github.com/Alims-Repo/crash-guard-sample)
- [Changelog](CHANGELOG.md)
- [API Documentation](https://Alims-Repo.github.io/crash-guard/)

---

**Made with ❤️ by [Alims-Repo](https://github.com/Alims-Repo)**

If you find this library helpful, please consider giving it a ⭐ on GitHub!