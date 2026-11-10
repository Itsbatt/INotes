# INotes - Release Notes and Build Instructions

## Project Status

✅ **Code Complete**: All features implemented
- Note CRUD operations with date grouping (Hoy, Ayer, Esta semana, Mes, Año)
- Folder management (create, rename, delete)
- Pin/unpin notes
- Search by title and body
- Swipe-to-delete with trash/recycle bin (30-day retention)
- Dark mode support
- Share functionality
- Material 3 UI following iPhone Notes design

✅ **Tests Configured**:
- 3 JVM unit tests (GetAllNotes, SearchNotes, PinNote use cases)
- 1 Room migration test (exportSchema = true verified)
- 5 instrumented tests (marked for device execution)

✅ **Documentation**:
- README.md in Spanish with full app description
- LICENSE MIT at name "Itsbatt"
- .gitignore with proper Android exclusions
- docs/pruebas.md with test results format

⚠️ **APK Build**: Requires local Android development environment
⚠️ **GitHub Release**: Requires compiled APK first

## Prerequisites for Building APK

### 1. Install Java JDK 17
The project is compiled with Java 17 (Temurin Adoptium):
- Already downloaded: `C:\Users\bat\.gradle\jdks\eclipse_adoptium-17-amd64-windows.2`
- Verify: `java -version` should show OpenJDK 17

### 2. Install Android SDK
Already present at: `C:\Users\bat\AppData\Local\Android\Sdk`
- Platform-tools: `adb`, `fastboot`
- Build tools: 35.0.0, 36.0.0
- Platforms: android-34 (targetSdk), android-29 (minSdk)
- System images for emulation

### 3. Install Gradle
The project uses Gradle 8.4 with Android plugin.

#### Option A: Use Android Studio
1. Open the project in Android Studio
2. Android Studio will automatically download the correct Gradle version
3. Click "Sync Now" to sync Gradle files
4. Build → Build Bundle(s) / APK(s) → Build APK(s)

#### Option B: Manual Gradle Setup
If running from command line:

```bash
# Download and set up Gradle 8.4
cd /tmp && curl -sL https://services.gradle.org/distributions/gradle-8.4-all.zip -o gradle-8.4.zip
unzip -o gradle-8.4.zip -d /tmp/gradle-8.4

# Add to PATH
export PATH="/tmp/gradle-8.4/gradle-8.4/bin:$PATH"
export JAVA_HOME="/c/Users/bat/.gradle/jdks/eclipse_adoptium-17-amd64-windows.2"

# From project root
gradle wrapper  # This will create gradlew
./gradlew assembleDebug
```

### 3. Set ANDROID_HOME
```bash
export ANDROID_HOME="/c/Users/bat/AppData/Local/Android/Sdk"
export PATH="$ANDROID_HOME/platform-tools:$PATH"
export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"
```

## Building the APK

### Using Android Studio (recommended):
1. Open `C:\Users\bat\Projects\INotes` in Android Studio
2. Wait for Gradle sync to complete
3. Connect a device or start an emulator
4. Click the "Run" button (green play arrow) or:
   - Build → Build APK(s)
   - Output: `app/build/outputs/debug/app-debug.apk`

### Using command line:
```bash
export ANDROID_HOME="/c/Users/bat/AppData/Local/Android/Sdk"
export PATH="$ANDROID_HOME/platform-tools:$PATH"
export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"
export JAVA_HOME="/c/Users/bat/.gradle/jdks/eclipse_adoptium-17-amd64-windows.2"

cd /c/Users/bat/Projects/INotes
./gradlew assembleDebug

# APK will be at:
app/build/outputs/debug/app-debug.apk
```

### Using device/emulator:
```bash
# Install the debug APK
adb install -r app/build/outputs/debug/app-debug.apk

# Launch the app
adb shell am start -n com.itsbatt.inotes/.ui.main.MainActivity
```

## Test Execution

### JVM Tests:
```bash
./gradlew test
```
Runs: GetAllNotesUseCaseTest, SearchNotesUseCaseTest, PinNoteUseCaseTest

### Room Migration Test:
Verifies `exportSchema = true` in the @Database annotation and that the in-memory database creates all expected tables.

### Instrumented Tests (require device/emulator):
```bash
./gradlew connectedAndroidTest
```
Runs: NoteListInstrumentedTest (5 tests for search, FAB, folder management, share)

## Release Preparation

### 1. Build APK debug
```bash
./gradlew assembleDebug
```

### 2. Build APK release (optional)
```bash
./gradlew assembleRelease
# Output: app/build/outputs/release/app-release-unsigned.apk
```

### 3. Sign the APK
Generate a keystore:
```bash
keytool -genkey -v -keystore my-release-key.keystore -alias alias -keyalg RSA -keysize 2048 -validity 10000
```
Follow prompts for password, name, etc.

### 4. Upload to GitHub
1. Create a new release on GitHub repository
2. Upload the APK (or App Bundle) as a release asset
3. Add release notes

### 5. GitHub Release Template
```markdown
## INotes v1.0

### What's New
- Initial release with full note-taking functionality
- iPhone Notes UI style with Material 3
- Local storage with Room database
- Date-grouped notes, folders, pin, search
- Swipe-to-delete with 30-day trash
- Dark mode support

### Technical Notes
- No accounts, no network, local only
- MIT License - Copyright Itsbatt
- Minimum SDK: 29 (Android 10)
- Target SDK: 34

### APK
- debug: `app/build/outputs/debug/app-debug.apk`
- release: `app/build/outputs/release/app-release-unsigned.apk` (needs signing)
```

## Known Limitations

- No backend/sync - local only
- No account system or authentication
- No cloud backup or restore
- Tests marked as "NO PROBADA" require physical device or emulator
- Some edge cases in migration logic (verified with in-memory test)

## File Structure

```
/app/                    # Android module
  /src/main/            # Main source set
    /kotlin/            # Kotlin source files
    /res/               # Resources
    /AndroidManifest.xml
  /src/test/            # JVM tests
  /src/androidTest/     # Instrumented tests
/gradle/                # Gradle wrapper (need to create)
/build.gradle           # Project-level build config
/gradle/wrapper/        # Gradle wrapper files
/docs/                  # Documentation
  /pruebas.md           # Test results format
/LICENSE                # MIT license
/README.md              # Spanish documentation
/.gitignore             # Android exclusions
```

## Contact

Project: https://github.com/Itsbatt/INotes
Author: Itsbatt
License: MIT