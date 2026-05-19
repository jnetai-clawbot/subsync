# SubSync - Smart Subtitle Finder & Editor

Automatically detect video files on your Android device, download matching subtitles, sync correction, translate, and edit offline.

## Features

- **Video Detection** - Auto-scan device storage for video files
- **Subtitle Matching** - Simulated local search for subtitle downloads
- **Sync Correction** - Adjust timing offsets and scale subtitle timing
- **Translation** - Basic multi-language subtitle translation
- **Offline Editor** - Edit subtitle text and timing
- **SRT Export** - Export edited subtitles in SRT format
- **Text Search** - Search subtitle content across all files
- **Dark UI** - Modern Material Design 3 dark theme with neon accents

## Tech Stack

- MinSdk 29, TargetSdk 34, CompileSdk 34
- Kotlin 1.9.22, AGP 8.2.2, Gradle 8.5
- Jetpack Compose with Material Design 3
- Room Database for persistence
- Navigation Compose

## Build

```bash
./gradlew assembleDebug
./gradlew assembleRelease
```

## License

MIT License

## About

Made by [jnetaol.com](https://jnetaol.com)
