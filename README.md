# Google Home Assistant language hook

An experimental Xposed module that rewrites one Google Home setup request. It detects the `SetSurfaceAssistantLanguageRequest` constructor by its request name and endpoint, then changes protobuf string field 3 from exactly `en-IN` to the locale saved in the module settings (default `ru-RU`). Other values and protobuf fields are left unchanged. If request discovery is missing or ambiguous, the hook fails closed.

This is a research prototype, not a confirmed way to set a speaker's language. The request and field were found by static inspection in Google Home `2.25.1.5` and `4.33.27.1`; no Xposed runtime, Google server response, setup completion, or Russian voice response has been verified. The server may reject a locale even when the module changes the request.

The project is unofficial and is not affiliated with Google. Google Home and related APKs are proprietary software. This repository does not include those APKs or their decompiled sources. Obtain any test APKs from a source you are entitled to use; do not redistribute them with this project.

## Project contents

- `module/src/main/` — Java Xposed module source and settings screen.
- `module/src/test/` — local policy unit tests.
- `docs/` — static APK findings, compatibility research, test requirements, and validation status.
- `licenses/` and `THIRD_PARTY_NOTICES.md` — notices for bundled third-party components.
- `module/libs/api-82-source-built.jar` — compile-only Xposed API 82 classes. They are not packaged into the module APK.

Local APK samples, decompilation output, build products, debug keys, and SDK caches are intentionally excluded from source publication. See `.gitignore` and [publication readiness](docs/publication-readiness.md).

## Build

Requirements: JDK 17 or later, Android SDK platform 35, and Android Build Tools 36.1. The project was last recorded building with JDK 21.0.12.1, Gradle 8.10.2, and Android Gradle Plugin 8.7.3. The Gradle wrapper verifies the distribution checksum.

From the project root:

```sh
./gradlew :module:assembleDebug
```

On Windows, use `gradlew.bat :module:assembleDebug`. The debug APK is written under `module/build/outputs/apk/debug/`. It is debug-signed and is not a production release. To run the existing unit tests locally:

```sh
./gradlew :module:testDebugUnitTest
```

The build requires network access to the configured Google, Maven Central, and Gradle Plugin Portal repositories. No local SDK, JDK, Gradle distribution cache, or cloud proxy configuration is required in the repository.

The expected upstream source revision and checksum for the compile-only Xposed API JAR are recorded in [third-party notices](THIRD_PARTY_NOTICES.md).

## Hook behavior and diagnostics

The module only targets the main `com.google.android.apps.chromecast.app` process. It searches Home's loaded DEX files for a unique constructor containing both the stable request name and endpoint, then verifies that the first constructor argument exposes protobuf-lite serialization and parser methods. It rewrites only field 3 when its current value exactly matches `en-IN`. Missing, ambiguous, or malformed matches leave the request unchanged and produce a diagnostic log entry.

For each matching constructor call, the prototype logs the language code and displays a Toast with the before/after values. It does not log serialized request bodies, account identifiers, or device tokens. The Toasts are intentionally repeated for diagnostics and may be noisy. The module has no `INTERNET` permission.

## Releases

GitHub Actions builds and publishes tagged builds marked `UNTESTED`. See [release instructions](docs/releasing.md) for the current tag and future releases.

The settings screen accepts a restricted language-tag syntax and stores a locale for `XSharedPreferences`. This checks syntax only; it does not establish that Google Home or a speaker supports the selected locale. Cross-process preference access uses `MODE_WORLD_READABLE` for the legacy Xposed setup and should be re-evaluated before supporting newer Android targets.

## Compatibility and validation

The module uses the classic Xposed API 82 entry point and hook APIs. API compatibility does not guarantee that a particular framework or container can load the module. No runtime compatibility has been confirmed for VirtualXposed, BlackBox, TaiChi, LSPosed, LSPatch, or VMOS. See [framework compatibility](docs/framework-compatibility.md) and [sandbox research](docs/sandbox-alternatives.md).

The current evidence and remaining device-side checks are listed in [validation status](docs/validation.md). Do not treat a successful local build as proof that the hook loads, the server accepts the locale, or the speaker responds in that language.

## License

The project's own source code is licensed under MIT; see [`LICENSE`](LICENSE). Third-party components retain their own licenses; see [third-party notices](THIRD_PARTY_NOTICES.md).
