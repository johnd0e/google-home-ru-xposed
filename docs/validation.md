# Validation status

Last checked: 2026-10-20.

## Recorded local evidence

- `./gradlew clean :module:assembleDebug :module:testDebugUnitTest --no-daemon` completed successfully with JDK 21.0.12.1, Gradle 8.10.2, Android Gradle Plugin 8.7.3, and Android platform 35. All 4 unit tests passed (0 failures/errors).
- The Android SDK reported that the optional Platform-Tools license was not accepted and Platform-Tools were not installed; this did not block the build or unit tests. Compilation emitted Java 8 source/target deprecation warnings under JDK 21, Kotlin annotation metadata warnings from DexKit, and a deprecation note for `MODE_WORLD_READABLE`.
- APK inspection found the Xposed entry point, DexKit request discovery, settings activity, protobuf rewrite implementation, `assets/xposed_init`, and DexKit native libraries for ARM and x86. Xposed API classes are compile-only and are not packaged into the APK.
- The freshly built debug APK is at `dist/google-home-ru-configurable-debug.apk`. It has package `local.googlehomeru`, version name `0.4-semantic-request-hook`, minSdk 22, targetSdk 22, and SHA-256 `6c34a78197d7b65abf3f0473c16f73f37fc90aba518e5c02d3548e6f494d2780`.
- `apksigner verify --verbose --print-certs` accepted its v1 and v2 signatures. The signer is the local debug key (SHA-256 `8c5cef3ba3d4407272fab7159176d51e0af59ca94a5485296bf899b9d89eca37`); the signing key is not part of the intended source publication contents. This is not a release-signed APK.
- The APK had no `INTERNET` permission and did not contain the supplied Google APKs, account data, or an Xposed API implementation.
- Static inspection confirmed the same request name, endpoint, and protobuf language field in Google Home 2.25.1.5 and 4.33.27.1. This is evidence for those examined builds only.

## Not validated

- Xposed framework installation, module load callback, DexKit discovery at runtime, constructor interception, and Toast display.
- Hook behavior in any sandbox/container, LSPosed, or LSPatch.
- The target speaker surface, Google server acceptance of the selected locale, setup completion, or the speaker's resulting voice.
- Recovery/restart behavior on a device.

The module changes only an exact `en-IN` value to the configured locale and passes other languages through. It is an experimental build, not a confirmed working installation package. Refer to [framework compatibility](framework-compatibility.md) and [device baseline](baseline.md) before planning a live run.
