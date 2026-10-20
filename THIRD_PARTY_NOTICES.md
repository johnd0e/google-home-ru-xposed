# Third-party notices

This project uses or includes the following third-party components. Their licenses apply to those components, not to the project's own source code.

## DexKit 2.3.0

Used at runtime for DEX method discovery. License: Apache License 2.0. A copy is included in [`licenses/Apache-2.0.txt`](licenses/Apache-2.0.txt); the original DexKit license file is also retained as [`licenses/DexKit-Apache-2.0.txt`](licenses/DexKit-Apache-2.0.txt). Project: <https://github.com/LuckyPray/DexKit>.

## XposedBridge API 82

`module/libs/api-82-source-built.jar` is a compile-only API archive built from the XposedBridge source at tag `v82`, commit `21c8ea54a54a55b9a3b3c234ea657ca0822e38c0`, and its hidden API stubs. Its SHA-256 is `2c5f2dd4e7070f6455a057b8935fed592aa9cda94acb6541a76acc95bc84ee8d`. The API classes are not packaged in the module APK. The XposedBridge project identifies its main product as Apache License 2.0 and includes modified Apache Commons Lang code; see [`licenses/XposedBridge-NOTICE.txt`](licenses/XposedBridge-NOTICE.txt) and the upstream source at <https://github.com/rovo89/XposedBridge/tree/v82>.

## Build-time dependencies

The Android Gradle Plugin, Gradle, Android SDK, and JUnit are resolved from their published repositories during a build. They are not redistributed by this project. Their respective licenses remain applicable to those tools and libraries.
