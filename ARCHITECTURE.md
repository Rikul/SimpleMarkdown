# Simple Markdown - Architecture Documentation

## Table of Contents
1. [Overview](#overview)
2. [Module Structure](#module-structure)
3. [Technology Stack](#technology-stack)
4. [Architecture Patterns](#architecture-patterns)
5. [Key Components](#key-components)
6. [Data Flow](#data-flow)
7. [Build System](#build-system)
8. [Testing Strategy](#testing-strategy)
9. [Code Organization](#code-organization)
10. [Dependencies](#dependencies)

## Overview

Simple Markdown is an open-source Android Markdown editor that provides a simple and intuitive interface for creating and editing Markdown documents on Android devices. The application follows modern Android development practices using Jetpack Compose for UI, Kotlin coroutines for asynchronous operations, and a modular architecture for maintainability and extensibility.

**Key Features:**
- Real-time Markdown rendering
- File operations (open, save, share)
- Readability statistics
- Dark mode support
- Autosave functionality
- Customizable settings
- Both free (F-Droid) and Play Store variants

## Module Structure

The project is organized into several Gradle modules:

### 1. `:app` - Main Application Module
The primary application module containing the UI and main application logic.

**Responsibilities:**
- Activities and navigation
- Compose UI screens
- ViewModel and state management
- File operations
- User preferences
- Theme and styling

**Key Packages:**
- `com.wbrawner.simplemarkdown` - Main activity and application class
- `com.wbrawner.simplemarkdown.ui` - Compose UI screens
- `com.wbrawner.simplemarkdown.utility` - Helper classes for file and preference management
- `com.wbrawner.simplemarkdown.model` - Data models (Readability, Sentence)

### 2. `:core` - Core Module
Shared core functionality used across different build variants.

**Responsibilities:**
- Error reporting (ACRA integration)
- Logging infrastructure
- Common utilities

**Key Components:**
- `ErrorReporterTree` - Timber tree for error reporting
- `LocalOnlyException` - Wrapper to prevent certain exceptions from being reported

### 3. `:free` - Free Variant Module
Contains implementations specific to the F-Droid/free version.

**Responsibilities:**
- Review helper stub (no-op implementation)
- Support link provider for free version

### 4. `:non-free` - Non-Free Variant Module
Contains implementations specific to the Google Play Store version.

**Responsibilities:**
- In-app review integration (Google Play Review API)
- Billing functionality
- Support link provider for Play Store version

### 5. `:baselineprofile` - Performance Optimization
Module for generating baseline profiles to improve app startup time.

**Responsibilities:**
- Startup benchmarks
- Baseline profile generation

## Technology Stack

### Core Technologies
- **Language:** Kotlin 2.2.0
- **Build System:** Gradle with Kotlin DSL
- **Min SDK:** 23 (Android 6.0)
- **Target SDK:** 36
- **Java Version:** 1.8

### UI Framework
- **Jetpack Compose** - Modern declarative UI toolkit
  - Compose BOM 2025.06.01
  - Material 3 components
  - Navigation Compose
  - Window Size Class for responsive layouts

### Markdown Processing
- **CommonMark** (v0.25.0) - Markdown parser and renderer
  - Extensions: GFM Tables, Strikethrough, Autolink, Task Lists, YAML Front Matter, Image Attributes, Heading Anchor

### Concurrency
- **Kotlin Coroutines** (v1.10.2)
  - Dispatchers for background operations
  - Flow for reactive state management

### Dependency Injection
- **Manual DI** via companion objects and factory patterns
  - No Dagger/Hilt - lightweight approach for simple dependency graph

### Logging & Error Reporting
- **Timber** (v5.0.1) - Logging framework
- **ACRA** (v5.12.0) - Application Crash Reports for Android

### Storage
- **SharedPreferences** - User preferences
- **Android Storage Access Framework** - File operations

### Testing
- **JUnit 4** - Unit testing
- **Robolectric** - Android unit tests
- **Espresso** - UI testing
- **Compose UI Testing** - Compose-specific UI tests
- **Fladle** - Firebase Test Lab integration
- **Test Orchestrator** - Isolated test execution

## Architecture Patterns

### MVVM (Model-View-ViewModel)
The app follows the MVVM architecture pattern:

```
┌─────────────┐
│    View     │  (Compose UI)
│  (Screen)   │
└──────┬──────┘
       │ observes state
       │ sends events
┌──────▼──────┐
│  ViewModel  │  (MarkdownViewModel)
└──────┬──────┘
       │ uses
┌──────▼──────┐
│   Helpers   │  (FileHelper, PreferenceHelper)
└─────────────┘
```

**Components:**
- **View (Compose Screens):** Declarative UI that observes ViewModel state
- **ViewModel:** Manages UI state and business logic
- **Helpers/Repositories:** Handle data operations (file I/O, preferences)

### Unidirectional Data Flow
State flows in one direction through the app:

```
User Action → ViewModel → State Update → UI Recomposition
```

**Example Flow:**
1. User types in editor → `markdownUpdated()` called
2. ViewModel updates state → `_state.emit(newState)`
3. UI observes state → `state.collectAsState()`
4. UI recomposes with new state

### State Management
**EditorState** is the single source of truth for the editor:

```kotlin
data class EditorState(
    val fileName: String = "Untitled.md",
    val textFieldState: TextFieldState = TextFieldState(),
    val path: URI? = null,
    val toast: ParameterizedText? = null,
    val alert: AlertDialogModel? = null,
    val dirty: Boolean = false,
    val enableReadability: Boolean = false,
    val enableAutosave: Boolean = false,
    // ... other fields
)
```

- Immutable data class
- ViewModel exposes `StateFlow<EditorState>`
- UI collects and reacts to state changes

### Dependency Injection
Simple manual DI pattern using companion objects:

```kotlin
class MarkdownApplication : Application() {
    companion object {
        lateinit var fileHelper: FileHelper
        lateinit var preferenceHelper: PreferenceHelper
    }
}
```

Benefits:
- No complex DI framework
- Clear dependency graph
- Easy to understand and maintain
- Sufficient for app's scope

## Key Components

### MainActivity
**Purpose:** Single activity that hosts all navigation

**Responsibilities:**
- Setting up Compose UI
- Navigation management
- Handling keyboard shortcuts
- Receiving and processing intents (file opening)
- Theme management

**Navigation:**
- Uses Jetpack Navigation Compose
- Routes: Main, Settings, Help, Libraries, Privacy Policy, Support

### MarkdownViewModel
**Purpose:** Core business logic and state management

**Key Functions:**
- `markdownUpdated()` - Tracks changes and triggers autosave
- `save()` / `load()` - File operations
- `new()` - Create new document
- `autosave()` - Background autosave with debouncing
- State management for editor, alerts, toasts

**Design Patterns:**
- Uses `Mutex` for thread-safe save operations
- Debounces autosave with coroutine delay
- Observes preferences reactively with Flow

### FileHelper (Interface + AndroidFileHelper)
**Purpose:** Abstract file operations

**Key Operations:**
- `open(source: URI)` - Open and read files
- `save(destination: URI, content: String)` - Write files
- Handles Android Storage Access Framework
- Manages persistent URI permissions

**Design:**
- Interface for testability
- Android-specific implementation
- Uses coroutines for async I/O

### PreferenceHelper (Interface + AndroidPreferenceHelper)
**Purpose:** Type-safe preference management

**Features:**
- Enum-based preference keys
- Reactive observation with StateFlow
- Type-safe getters/setters

**Preferences Include:**
- Dark mode settings
- Autosave configuration
- Custom CSS
- Readability features
- Lock swiping behavior

### UI Screens
**MainScreen:**
- Horizontal pager for editor/preview
- Top app bar with actions
- Navigation drawer
- Keyboard shortcuts
- File picker integration

**MarkdownTextField:**
- Custom text field for Markdown editing
- Line numbers
- Syntax highlighting support

**MarkdownText:**
- WebView-based Markdown rendering
- Custom CSS support
- Theme-aware rendering

**SettingsScreen:**
- Preference UI using Compose
- Theme selection
- Feature toggles

### Markdown Processing
**Rendering Pipeline:**

```
Markdown Text → CommonMark Parser → AST → HTML Renderer → WebView
```

**Parser Configuration:**
```kotlin
val markdownParser = Parser.builder()
    .extensions(listOf(
        TablesExtension.create(),
        StrikethroughExtension.create(),
        AutolinkExtension.create(),
        TaskListItemsExtension.create(),
        YamlFrontMatterExtension.create(),
        ImageAttributesExtension.create(),
        HeadingAnchorExtension.create()
    ))
    .build()
```

### Readability Analysis
**Purpose:** Calculate readability metrics for Markdown content

**Metrics:**
- Reading time estimation
- Sentence analysis
- Word count
- Syllable counting (using `syllable-counter` library)

**Models:**
- `Sentence` - Represents analyzed sentence
- `Readability` - Container for readability stats

## Data Flow

### Opening a File
```
1. User selects file → File picker intent
2. MainActivity receives URI
3. ViewModel.load(uri) called
4. FileHelper.open(uri) reads content
5. State updated with content
6. UI recomposes with loaded content
```

### Saving a File
```
1. User triggers save
2. ViewModel.save() called
3. Check if autosave enabled and path exists
4. Mutex locks to prevent concurrent saves
5. FileHelper.save(uri, content) writes to storage
6. State updated with clean flag
7. Toast notification shown
```

### Autosave Flow
```
1. User types in editor
2. markdownUpdated() called
3. Debounce timer starts (500ms)
4. After delay, autosave() called
5. If enabled and path exists → save silently
6. If no path → show toast prompting manual save
```

### Preference Changes
```
1. User changes setting in SettingsScreen
2. PreferenceHelper.set() updates SharedPreferences
3. PreferenceHelper emits new value to StateFlow
4. ViewModel observes Flow and updates state
5. UI recomposes with new preference
```

## Build System

### Gradle Modules
Project uses Gradle with Kotlin DSL and version catalogs (`libs.versions.toml`).

### Product Flavors
**Two flavors for different distribution channels:**

```kotlin
flavorDimensions.add("platform")
productFlavors {
    create("free") {
        applicationIdSuffix = ".free"
        versionNameSuffix = "-free"
    }
    create("play") {
        signingConfig = signingConfigs["playRelease"]
    }
}
```

**Variants:**
- `freeDebug` / `freeRelease` - F-Droid builds
- `playDebug` / `playRelease` - Google Play builds

**Differences:**
- Free uses `:free` module (no Play services)
- Play uses `:non-free` module (includes Play billing, review API)

### Build Types
**Debug:**
- Includes logging
- ACRA error reporting to custom endpoint
- StrictMode enabled
- Custom CSS enabled

**Release:**
- ProGuard/R8 minification
- Optimized
- Custom CSS disabled (security)

### Build Configuration
**Key Files:**
- `build.gradle.kts` - Root build script
- `settings.gradle.kts` - Module configuration
- `libs.versions.toml` - Centralized dependency versions
- `gradle.properties` - Build properties
- `keystore.properties` - Signing configuration (not in VCS)

### Build Commands
```bash
# Build free debug variant
./gradlew assembleFreeDebug

# Build play release variant (requires keystore)
./gradlew assemblePlayRelease

# Run tests
./gradlew test
./gradlew connectedAndroidTest

# Run lint
./gradlew lint
```

## Testing Strategy

### Unit Tests (`/app/src/test`)
**Framework:** JUnit 4 + Robolectric

**Test Coverage:**
- `MarkdownViewModelTest` - ViewModel logic
- `ReadabilityTest` - Readability calculations
- `FakeFileHelper` / `FakePreferenceHelper` - Test doubles

**Approach:**
- Pure Kotlin unit tests where possible
- Robolectric for Android-dependent code
- Coroutine testing with `kotlinx-coroutines-test`

### Instrumented Tests (`/app/src/androidTest`)
**Framework:** Espresso + Compose UI Test

**Test Coverage:**
- `MarkdownTests` - Core editing functionality
- `HelpTest` - Help screen navigation
- Robot pattern for reusable test actions

**Robot Pattern:**
```kotlin
// Encapsulates common UI actions
class MainScreenRobot {
    fun typeMarkdown(text: String)
    fun clickSave()
    fun assertMarkdownEquals(expected: String)
}
```

**Test Orchestrator:**
- Runs each test in isolation
- Prevents state leakage
- Enabled in configuration

### Firebase Test Lab
**Configuration in Fladle:**
```kotlin
fladle {
    variant.set("playDebug")
    useOrchestrator.set(true)
    testTimeout.set("7m")
    devices.add(mapOf("model" to "Pixel2.arm", "version" to "33"))
}
```

### Baseline Profiles
**Purpose:** Optimize app startup

**Module:** `:baselineprofile`
- Microbenchmarks for startup
- Generates profiles for ART optimization
- Improves cold start time

## Code Organization

### Package Structure
```
com.wbrawner.simplemarkdown/
├── model/              # Data models
│   ├── Readability.kt
│   └── Sentence.kt
├── ui/                 # Compose UI
│   ├── MainScreen.kt
│   ├── MarkdownInfoScreen.kt
│   ├── MarkdownTextField.kt
│   ├── MarkdownText.kt
│   ├── SettingsScreen.kt
│   ├── SupportScreen.kt
│   └── theme/          # Theming
├── utility/            # Helper classes
│   ├── Extensions.kt
│   ├── FileHelper.kt
│   ├── PreferenceHelper.kt
│   └── PersistentTree.kt
├── MainActivity.kt
├── MarkdownApplication.kt
└── MarkdownViewModel.kt
```

### Naming Conventions
- **Activities:** `*Activity.kt`
- **ViewModels:** `*ViewModel.kt`
- **Screens:** `*Screen.kt` (Compose)
- **Helpers:** `*Helper.kt`
- **Interfaces:** Same name as implementation without "Android" prefix
- **Tests:** `*Test.kt`

### Code Style
- Kotlin idiomatic style
- Jetpack Compose best practices
- Coroutines for async operations
- Immutable data classes for state
- Extension functions for utilities

## Dependencies

### Core Android Libraries
- **AndroidX Core KTX** - Kotlin extensions
- **AppCompat** - Backward compatibility
- **Activity KTX** - Activity extensions
- **Lifecycle ViewModel** - ViewModel support
- **Navigation Compose** - Compose navigation
- **Preference KTX** - Settings UI

### Compose Libraries
- **Compose BOM** - Bill of materials for version alignment
- **Material 3** - Material Design 3 components
- **Material Icons Extended** - Icon library
- **Activity Compose** - Compose integration
- **UI Tooling** - Preview and debugging

### Markdown Processing
- **CommonMark** - Core Markdown parser
- **Extensions:** Tables, Strikethrough, Autolink, Task Lists, YAML, Image Attributes, Heading Anchors

### Utilities
- **Timber** - Logging
- **ACRA** - Crash reporting
- **Syllable Counter** - Readability analysis
- **Browser** - Custom Tabs for links
- **Core Splashscreen** - Splash screen API

### Testing Libraries
- **JUnit 4** - Testing framework
- **Robolectric** - Android unit tests
- **Espresso** - UI testing
- **Compose UI Test** - Compose testing
- **Coroutines Test** - Coroutine testing
- **Orchestrator** - Test isolation

### Build & Development
- **Desugar JDK Libs** - Java 8+ API desugaring for older Android
- **Fladle** - Firebase Test Lab Gradle plugin
- **Dependency Analysis** - Detect unused dependencies
- **Baseline Profile** - Performance optimization
- **ProGuard/R8** - Code shrinking and obfuscation

### Play Store Variant Only
- **Google Play Billing** - In-app purchases
- **Play Review KTX** - In-app review API

### Version Catalog
All versions managed in `gradle/libs.versions.toml` for centralized dependency management.

---

## Additional Resources

- **README.md** - Quick start and building instructions
- **LICENSE** - Apache License 2.0
- **NOTICE-ASL.txt** - Attribution notices
- **Libraries.md** - Third-party library acknowledgments
- **Privacy Policy.md** - Privacy policy
- **Cheatsheet.md** - Markdown syntax reference

## Development Workflow

### Setting Up Development Environment
1. Clone repository
2. Open in Android Studio
3. Create `keystore.properties` (for release builds)
4. Create `acra.properties` in `:core` module (for error reporting)
5. Sync Gradle
6. Run `assembleFreeDebug`

### Adding New Features
1. Create UI in appropriate `*Screen.kt`
2. Add business logic to `MarkdownViewModel`
3. Update `EditorState` if needed
4. Add helper methods if required
5. Write unit tests
6. Write instrumented tests for UI
7. Update documentation

### Release Process
1. Update version in `app/build.gradle.kts`
2. Update release notes in `app/src/play/play/release-notes/`
3. Build release: `./gradlew assemblePlayRelease`
4. Upload to Play Console
5. F-Droid builds automatically from tags

---

**Last Updated:** 2025-12-17
**Version:** Based on v2025.09.00
