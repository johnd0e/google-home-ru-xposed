# Framework compatibility review

Reviewed against framework documentation on 2026-10-13. Scope is the Android 5.1 target and the named Xposed/virtualization approaches; this is not a claim that the module has been runtime-tested on any of them.

## What the module uses

- Xposed module entry point `IXposedHookLoadPackage`, module metadata, `xposed_init`, `XposedBridge.hookMethod`, and `XC_MethodHook`.
- `Application.attach(Context)` to obtain the target app context, then DexKit 2.3.0 to search the Home class loader for the constructor containing the stable request name and endpoint.
- Exact package and main-process checks. There is no Home version-code allowlist.
- `XSharedPreferences` to read a locale saved by the module settings activity. The settings file is written with `MODE_WORLD_READABLE`, which is the documented cross-process arrangement for classic Xposed preferences on the Android 5.1 target.
- Stable request-name and endpoint markers plus protobuf field 3, statically confirmed in Home 2.25.1.5 and 4.33.27.1. A future Home build may change these markers or the protobuf schema.

## Findings by framework

| Framework | Documentation finding | Fit for this module on Android 5.1 | Remaining check |
|---|---|---|---|
| Classic Xposed | The pinned v82 API includes the entry-point, method-hook, and `XSharedPreferences` APIs used here. | This is the intended API family for the original Android 5.1 plan. | Confirm the installed classic framework version/API and observe the method hook on the target. |
| LSPosed | Its README says modules written for LSPosed are compatible with original Xposed and vice versa. Its documented minimum is Android 8.1. | API-level compatibility is explicitly claimed, but LSPosed itself cannot serve the Android 5.1 target. | Test on a supported Android version only if that becomes a separate target; this does not validate the Android 5.1 flow. |
| LSPatch | Its README calls it a rootless LSPosed implementation that inserts DEX and native libraries into the target APK; documented minimum is Android 9. | The module's Xposed API usage is compatible in principle with its stated design, but LSPatch cannot run on the Android 5.1 target. | Verify that the chosen LSPatch release loads this external API82 module and that its `XSharedPreferences` implementation can read our settings. Install/use the repackaged Home only on a recoverable device; its signature changes. |
| VirtualXposed app | Its guide explicitly says both the target APK and the Xposed module must be installed inside its virtual space. It documents no system hooks and no resource hooks. Our module uses a normal Java method hook, not a resource hook. The README says Android 5–10; latest stable release `0.22.0` (2022-06-07) says it adds initial Android 12 support. | The closest documented consumer sandbox baseline for the exact Home 2.25.1.5 APK. The APK declares minSdk 21 and has x86/ARM ABIs, but neither app installation nor hook invocation has been tested in VirtualXposed. Its release is old. | Install this Home APK and module inside the same virtual space. Verify callback package/process, class loader, preference visibility, and Google Play Services plus speaker discovery (Bluetooth/local network). |
| VirtualApp SDK / commercial edition | The project README describes an Android app sandbox, Xposed hooks, and Google Services. Its developer guide documents `IXposedHookLoadPackage` use inside VAPP, virtual package/process values, and getting the class loader from the virtual context/application. The README says the public GitHub code stopped updating in 2017 while the commercial code continues and requires a license. It claims broad Android support, but says its built-in Xposed implementation is for Android through Q (Android 10). | The closest documented sandbox architecture for this use case and explicitly covers Android 5.0/5.1, Google Services, and Java hooks. It is an SDK/custom-host integration, not a ready replacement APK for VirtualXposed. | Our current hook consumes `lpparam.classLoader`; the VA guide says its `LoadPackageParam` is not fully one-to-one and recommends the virtual app context/application class loader. We would need an adapter, then test `XSharedPreferences` visibility and Home/Cast behavior. |

## Assessment for the current code

- The hook APIs are ordinary Xposed APIs rather than LSPosed-only APIs. That is a positive compatibility signal for classic Xposed, LSPosed, and LSPatch's stated Xposed integration.
- The dynamic DEX scan and DexKit native-library loading need runtime confirmation in each framework. DexKit 2.3.0 declares Android API 21 and ships ARM plus x86 libraries, which covers the old API/x86 target at the library level. The biggest module-specific uncertainty across virtualization/rootless frameworks is settings transport: the module reads its own `language_settings.xml` using `XSharedPreferences`. Their public documentation does not establish that this exact file is visible from every virtualized or patched process.
- The package/process guard and semantic class-loader lookup are deliberately strict. A framework that reports a host/virtual process name, or gives DexKit a class loader that omits Home DEX files, will cause the hook to skip or fail closed. The Android log will show `HOME_RU_LOADED`, `HOME_RU_REWRITE_HOOK_INSTALLED`, or `HOME_RU_UNSUPPORTED`.
- VirtualXposed remains the closest documented ready-made sandbox baseline, but its latest stable release is from 2022. Newer BlackBox-family sources were checked separately in [sandbox-alternatives.md](sandbox-alternatives.md): the examined NewBlackBox release notes say Xposed module management was removed.
- VirtualApp's open-source SDK is explicitly stale and its maintained code is described as commercial. Its docs are evidence of a product/SDK path, not evidence that a current downloadable host APK is available or that this Home APK works inside it.
- A successful hook load proves neither that Google Home reaches the candidate helper nor that a speaker/server accepts the configured target locale. The request name, endpoint, and protobuf language field were statically confirmed in Home 2.25.1.5 and 4.33.27.1. Future builds may change any of these stable-looking markers or the protobuf schema, so runtime matching remains fail-closed rather than guaranteed.
- LSPatch changes the Home APK by embedding framework code and signing the output as a modified APK. It is a separate installation path from enabling the existing module under classic Xposed; do not install the patched APK over an official-signed copy without accounting for the signature mismatch and recoverability.

## Documentation sources

- [XposedBridge API v82 source documentation](https://github.com/rovo89/XposedBridge/blob/v82/app/doclib/api/xposed_082.txt)
- [LSPosed framework README](https://github.com/LSPosed/LSPosed): supported versions and module compatibility statement
- [LSPatch framework README](https://github.com/LSPosed/LSPatch): rootless injection description and supported-version minimum
- [VirtualXposed/VirtualApp README](https://github.com/android-hacker/VirtualXposed) and [Chinese project documentation](https://github.com/android-hacker/VirtualXposed/blob/master/CHINESE.md): virtual app model, Google service integration notes, and documentation limitations
- [VirtualApp project README](https://github.com/asLody/VirtualApp/blob/master/README.md), [English product guide](https://github.com/asLody/VirtualApp/blob/master/README_eng.md), and [developer guide](https://github.com/asLody/VirtualApp/blob/master/doc/VADev.md): sandbox/SDK model, stated OS and Xposed-hook ranges, Google Services support, Xposed callback details, and licensing/maintenance caveats

## Practical order for a real device

1. For a no-root clone-sandbox baseline, install the exact Home APK and module together inside VirtualXposed; it documents the relevant classic Xposed method-hook model but is old.
2. Treat newer app-cloning containers as candidates only if they still load external Xposed modules. The inspected NewBlackBox version does not.
3. Before adapting the module to a different container, confirm its module discovery/loading flow, class-loader behavior, and how virtual storage exposes `XSharedPreferences`.
4. LSPatch is a separate, single-APK rootless route with an Android 9 minimum; it does not provide the requested clone sandbox.
5. Finally check Google Play Services, Bluetooth/local-network Cast discovery, server acceptance, and actual voice response on the intended speaker.
