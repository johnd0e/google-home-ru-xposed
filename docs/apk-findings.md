# APK inventory and static findings

## Provenance and APK inventory

The findings below came from user-supplied Google Home and Google app APK samples. The APKs, decompiled application sources, and machine-local provenance files are not included in the public source set. These applications are proprietary; use lawfully obtained samples for independent reproduction and do not redistribute them with this project.

`GoogleHome_2.25.1.5.apk` is package `com.google.android.apps.chromecast.app`, version `2.25.1.5`, versionCode `22501050`, minSdk 21, targetSdk 29. Its native libraries include `arm64-v8a`, `armeabi-v7a`, `x86`, and `x86_64`; the manifest version is compatible with Android API 22 at the declared minSdk level. This does not establish runtime compatibility with MEmu.

`googlequicksearchbox_10.33.5.21.apk` is package `com.google.android.googlequicksearchbox`, version `10.33.5.21.arm`, versionCode `300989339`, minSdk 21, targetSdk 28, and contains only `armeabi-v7a` native code. Home statically checks/queries this package's installed version and can open its Assistant settings or Play Store page. Whether this ARM-only APK installs/runs in the target MEmu is unknown until its ABI and native bridge are checked.

A read-only string-table scan of all seven DEX files in the Google APK found no literal `set_assistant_language` or `cast/orchestration` endpoint string. It contains generic Google Cast endpoints and language strings, including `ru-RU`, but that alone does not prove it participates in this setup request. The Home APK itself contains the request class, direct setup callers, endpoint, and transport path described below. JADX was not run on this second APK. Android Build Tools 36.1 `dexdump` emitted a future-verifier warning for one interface field in `classes.dex` while still extracting strings; runtime compatibility remains unknown.

`apksigner verify` accepted Home's v1 and v2 signatures and the Google app's v1, v2, and v3 signatures. The signer certificate subjects name Google, but the fingerprints have not been compared against an independently trusted Google reference; therefore this is cryptographic signature validity, not independent proof of provenance. `aapt` emitted v1 warnings for `META-INF/services` entries; the verified v2/v3 schemes cover the APK as a whole.

## Static request path in Home 2.25.1.5

The original implementation proposal targeted the helper described below. The current module instead hooks the request constructor semantically, as documented in [Version-resilient request hook](#version-resilient-request-hook). The static helper mapping remains useful evidence for how the setup screens create the request.

JADX 1.5.6 produced 25,726 files (about 250 MiB) and reported 45 class-decompilation errors overall. The candidate method descriptor and argument mapping were cross-checked against raw DEX with Android `dexdump`; this verifies the static signature, not runtime behavior.

The setup language screen is `defpackage.jme`. Its `onCreateView` builds the spinner from `abrp.a(surface)`, the app's supported-language list. On Continue, `jme.aF()` reads the selected code and invokes the `jit.a(...)` helper with that language, the linking information, and the surface. The warning confirmation screen `defpackage.jmg.aF()` invokes the same helper with the confirmed language. A source-tree search found these two call sites for that helper.

The candidate narrow hook is static `defpackage.jit.a(jeo, jpf, gzy, ltq, String, String, aajm): void`; the language is argument index 4. It is called by the two setup language-selection screens before the request object is built. The target surface enum is supplied as argument index 6 and must be allowlisted only after the actual MEmu/device surface is known. Do not log serialized request bodies, account identifiers, or tokens; the module logs only the language tag and rewrite result.

The implementation `defpackage.jet.a(String language, String linkingInfo, aajm surface, Consumer)` fills generated protobuf request `defpackage.aawf`; the language is protobuf field 3 and surface is field 1. `defpackage.jgy` names the request `SetSurfaceAssistantLanguageRequest` and constructs path `/set_assistant_language?rt=b` from the configured base `https://clients3.google.com/cast/orchestration`.

The request wrapper `defpackage.luy` sets method code `1`; `defpackage.blg` maps code `1` to HTTP `POST`. `defpackage.lve.g()` serializes the request with `toByteArray()`, `aU()` reports `application/protobuf`, and `defpackage.kqx` writes those bytes directly to the connection. The identified path does not add a compression layer. The response body is parsed as generated protobuf `aawg`. This is static code-path evidence; no live request or response was captured. The transport can add Authorization, CAST device ID, and server-token headers, which must not be logged or copied into evidence.

The Home manifest leaves the application process name empty, meaning Android's default package process for the application. The target classes reside in the APK's DEX, so the candidate module uses Xposed's package `classLoader`; neither process name nor runtime classloader has been observed on the target device. `jit.a` has two direct setup-screen callers in the decompiled source (`jme.aF()` and `jmg.aF()`), and its language argument is available before protobuf serialization. This identifies a useful observational hook, not yet a device-confirmed operation boundary.

The static Home lists contain `en-IN` in `assistant_hub_supported_languages`, `assistant_at_supported_languages`, and the default `assistant_supported_languages`, while they do not include `ru-RU`. The module uses exact `en-IN` as the donor code and rewrites it to the configured valid language tag (default `ru-RU`). These app-side lists do not prove that a particular speaker firmware/account supports `en-IN`, nor that the current server accepts the configured target language.

## Update/setup gate findings

The old Home contains a device-setup upgrade gate. After receiving setup data, `defpackage.mpn` compares the device setup protocol version (`uqt.a`) with `mqg.b`; when the device version is higher, it reports `mqd.APP_UPGRADE`. `mqg.b` is 9 or 10 depending on the remotely configured flag `usi.cw()`. A separate check reports the same upgrade reason when the BLE beacon version exceeds 3. The device-setup flow routes `APP_UPGRADE` to `nee.a(2)`, whose UI says this Home version must be updated to set up the device.

This identifies plausible protocol/firmware gates but not which one a target speaker triggers. Hiding the dialog would not demonstrate compatibility with the newer setup protocol. Do not add an update-gate hook until the exact branch and a safe setup path are observed on a restorable device clone.

## Open validation

- The target descriptor and argument mapping are cross-checked against raw DEX; broader request-path/runtime behavior still needs device validation.
- Independently confirm Google certificate fingerprints from a trusted source.
- Identify the test instance's Android version, API, ABI list, device model/firmware, Google app installation/version, and recovery method. Keep serials and account/device identifiers local; do not include them in public notes.
- Observe the language-selection screens and actual endpoint call on the device; establish whether the update gate fires and which protocol check triggers it.
- Validate the candidate method invocation, package classloader, actual surface enum, server response, and final Russian voice behavior. None of these are proven by static analysis.

## Version-resilient request hook

A later Google Home split bundle, version `4.33.27.1` (versionCode `30585736`), was statically inspected. Its obfuscated request class changed from `defpackage.jgy` to `defpackage.uit`, and the request builder also changed, but both constructors use the exact request name `SetSurfaceAssistantLanguageRequest` and endpoint `/set_assistant_language?rt=b`. The corresponding protobuf is now `defpackage.atot`; its field 3 is still the language string. The new APK requires Android API 29 and its ABI split is arm64-v8a, so this comparison demonstrates code-level compatibility across two releases, not installability on Android 5.1 or MEmu x86.

The module now uses DexKit 2.3.0 to search the loaded Home DEX files for a unique constructor containing both strings. It checks that argument 0 exposes protobuf-lite serialization/parser methods, then hooks the constructor and rewrites only protobuf field 3 when it equals `en-IN`. It does not depend on the obfuscated request class, helper name, method descriptor, or Home version code. If the marker strings are absent, the match is ambiguous, the protobuf field is missing/repeated/not a string, or parsing fails, it leaves the request unchanged and logs a diagnostic. DexKit declares API 21 support and packages x86, x86_64, armeabi-v7a, and arm64-v8a native libraries.

This avoids manually adding a new class/method mapping for each release that preserves the observed request contract. It cannot guarantee compatibility with future protocol/schema changes, and static DEX matches still need live validation inside the chosen Xposed or sandbox framework. DexKit's Apache 2.0 license is included under `licenses/`.
