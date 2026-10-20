# VirtualXposed-style alternatives

Research snapshot: 2026-10-13. The target is the exact Google Home `2.25.1.5` APK already analyzed in this project, running as a clone inside an Android app sandbox. The sandbox host can be any Android version; an Android guest VM is not required.

## Result

No newer ready-to-install replacement has yet been verified as a fit. The decisive feature is **loading our external Xposed module into the cloned Google Home process**. A container that only clones apps is not enough. The module also needs its Xposed callback/hook APIs and access to its saved locale preference. Google Home must receive Google Play Services and the host's Bluetooth/local-network discovery facilities inside the sandbox.

The project's module expects classic Xposed entry-point and hook APIs, the target app's class loader, and `XSharedPreferences`. Google Home setup may also depend on Google Play Services, local network discovery, and Bluetooth Low Energy. Each item must work inside the guest/container; Xposed support alone is not enough.

## Candidates checked

| Candidate | What the source supports | Assessment |
|---|---|---|
| VMOS (package `com.vmos.virtual`) | The official Play listing describes a self-contained Android 9 guest with root inside the guest. LSPosed supports Android 8.1+ and original Xposed modules, but its documented installation requires Magisk 24+ and installing LSPosed through the Magisk app. VMOS documents a Root mode, not Magisk/Zygisk integration or LSPosed. | Android/API compatibility is plausible; the missing proof is whether VMOS's guest root can run the required Magisk/Zygisk setup. VMOS Pro's older Android 7.1 guest is below LSPosed's minimum. Do not count VMOS root mode itself as LSPosed support. |
| NewBlackBox (`ALEX5402/NewBlackBox`) | The source repository has Android app virtualization and GMS-related code. Its release notes explicitly say it removed the Xposed manager service, module-install UI, and Xposed-related checks. | Not compatible with our external Xposed module in its current documented/source state. Its recent activity does not change that missing capability. |
| Blacks BlackBox (`Black00Z/Blacks-BlackBox`) | A current BlackBox app-virtualization fork, with source changes aimed at work profiles and modern Android compatibility. | Not a guest Android VM. Source search found no Xposed module loader/API; native `hideXposed` code is not module support. Not a replacement for the current module. |
| ZCore (`ZENINXOP/BlackBox`) | The README describes a no-root BlackBox-based app-cloning container and claims Xposed-module plus GMS support. The ZIP is a source archive, not merely an opaque binary. It contains an Xposed-module manager/parser, but the module-injection calls in `BActivityThread.loadXposed()` are commented out, and the build file says the Pine dependency was removed. The app build also sets `minSdk = 28` (Android 9), despite the README's free-edition Android 5.0 claim. | The inspected public source does **not** actually invoke external Xposed modules, so this version is not a fit for our module. Paid edition claims may differ, but its implementation is not in this public archive. Do not rely on the README feature claims without a working build or direct test. |
| VirtualApp | Public documentation describes a customizable sandbox SDK with Xposed callbacks and Google Services. It also says the public source stopped updating in 2017 and the maintained edition is commercial. | A custom-host SDK, not a ready replacement APK. Its documented callback/class-loader behavior also differs from what our module currently assumes. |
| VirtualXposed | Its guide explicitly supports installing the target app and module together in its virtual environment. The module uses ordinary method hooks rather than resource hooks, so the documented resource-hook limitation does not rule it out. The README says Android 5–10; latest stable release `0.22.0` (2022-06-07) says initial Android 12 support. | Still the clearest documented consumer-app baseline for our use case, but it is old and does not establish compatibility with current host Android versions or Home's GMS/Bluetooth/Cast path. It is a comparison point, not the requested newer replacement. |
| LSPatch | Rootless Xposed-style injection into a rebuilt target APK; Android 9+ minimum in its project documentation. | Potential route to run the hook without an Xposed host, but not an isolated guest Android environment. Keep as a distinct fallback. |

## Proof checklist for any app-cloning sandbox

Before adapting the module to a candidate, confirm all of these against current official documentation or on a disposable device:

1. It can import and launch the exact Home `2.25.1.5` APK as a cloned app.
2. It can install/load a third-party module, and invokes `IXposedHookLoadPackage` and `XposedBridge.hookMethod` in that clone.
3. The callback reports the cloned target's package and process in a way our module accepts; the target method's class loader is available.
4. The module can read its saved locale preference from the clone's environment.
5. Home receives functioning Google Play Services and can see/set up the intended speaker through the host's Bluetooth and local-network discovery. A generic GMS-compatibility claim does not establish BLE or multicast pass-through.
6. The language setup path constructs a request containing the stable request name and endpoint. Hook loading alone is not proof that DexKit discovers/intercepts the constructor or that the request reaches the server.

If any of steps 2–5 fail, the sandbox is not a practical route for the original goal even if it can clone and launch Home.

## VMOS follow-up

VMOS is a family/brand with multiple products, so do not conflate its current local virtual Android app, older VMOS Pro ROMs, its cloud-phone offerings, or third-party Play Store packages.

- The current official [VMOS app page](https://app.vmos.com/) links Google Play package `com.vmos.virtual` and a direct APK. It describes a complete local Android guest with Root mode and an activation flow for Android 12+ **host** devices. The [Play listing](https://play.google.com/store/apps/details?id=com.vmos.virtual) says the guest is Android 9, has built-in root inside the guest, and was updated 2026-07-03 (developer shown as Minical when checked). This is the current VMOS path relevant to LSPosed; it is distinct from older VMOS Pro ROMs.
- LSPosed's [official README](https://github.com/LSPosed/LSPosed) specifies Android 8.1–14, Magisk 24+, installation of LSPosed in the Magisk app, and compatibility between modules written for original Xposed and LSPosed. Thus Android 9 clears LSPosed's OS minimum, and this module's classic Xposed API usage is compatible in principle. The module still needs an actual load/hook test, especially for `XSharedPreferences`.
- VMOS's published feature material says Root mode, but does not say it provides Magisk, Zygisk, or a supported way to install Magisk modules. Root access is not equivalent to a working Magisk/Zygisk boot integration. This is the unresolved VMOS/LSPosed requirement; neither the official page nor listing proves it.
- A review on the current Play listing says “It do not have Xposed installer.” Treat this only as one user's observation about the app version they tried. LSPosed does not require the old standalone Xposed Installer app, so that review alone neither confirms nor rules out a manual Magisk/LSPosed installation.
- The older [Google Play package `com.vmos.google`](https://play.google.com/store/apps/details?id=com.vmos.google), published by JaxDeveloper, is a separate listing. Its user review mentioning Xposed is not provenance for official `com.vmos.virtual` support. A VMOS Pro package re-uploaded to GitHub explicitly says it belongs to another author; do not use that mirror.

### Fit for our Home test

The current VMOS Android 9 guest is a plausible host for LSPosed: its Android version is high enough, and LSPosed documents compatibility with original Xposed modules like ours. VMOS's advertised guest root does not answer whether Magisk/Zygisk can be installed and survive VM startup. That installation question, rather than LSPosed's API compatibility, is the main blocker.

Before treating it as compatible, verify on the actual current VMOS distribution:

1. Confirm Magisk 24+ can be installed and remains active in the VMOS Android 9 guest; then install the LSPosed Zygisk variant (or the Riru variant with its matching Riru prerequisite) through Magisk.
2. Confirm LSPosed Manager reports the framework active and allow-list Google Home plus the module.
3. Confirm our module APK is accepted and its `IXposedHookLoadPackage` / `XposedBridge.hookMethod` callbacks run against `com.google.android.apps.chromecast.app` versionCode `22501050`.
4. Confirm `XSharedPreferences` can read the saved locale in the guest.
5. Confirm compatible Google Play Services work inside the guest.
6. Confirm Google Home can discover the speaker through Bluetooth LE and local-network/multicast discovery from this guest.

If Magisk/Zygisk cannot be installed in the guest, the documented LSPosed path stops there; VMOS's built-in root by itself does not solve it. Until that is tested, classify current VMOS as a promising but unconfirmed experiment. VMOS Pro Android 7.1 is not an LSPosed option because it is below the documented Android 8.1 minimum.

### Where the VMOS + Xposed claim comes from

The association is visible in user/developer discussions, but these are reports about particular VMOS Pro guest images and particular Xposed implementations, not an official compatibility statement for current VMOS:

- In [SystemEdxposed issue #4](https://github.com/LuigiVampa92/SystemEdxposed/issues/4) (2021-08-25), a user asks for system Xposed because EdXposed does not work on Android 7, “especially on VMOS Pro.” The maintainer says classic Xposed might work on Android 7 and asks whether the user tried it; the thread does not establish that it did.
- GitHub's issue search also surfaced [RollingSkyHack issue #1](https://github.com/colinxu2020/RollingSkyHack/issues/1) (2025-12-27), where a user reports that Xposed in a VMOS Pro Android 7.1 guest “seems not to work.” The maintainer's reply — “only supports LSPosed” — refers to RollingSkyHack, not to VMOS or our module. It does not establish whether LSPosed works in VMOS, so it is not evidence for or against VMOS compatibility and should not inform our framework choice.

The limited forum evidence concerns old VMOS Pro guest ROMs and does **not** establish current support. For the current VMOS app, official materials establish Android 9 and guest root; LSPosed materials establish Android/API compatibility and a Magisk installation path. No source inspected establishes that Magisk/Zygisk works in the VMOS guest. It should not be phrased as either “VMOS supports LSPosed” or “LSPosed does not fit VMOS” before that missing installation step is checked.

## Evidence limits

This review distinguishes vendor documentation, public source code, and individual user reports. A framework feature claim does not establish that the exact Google Home APK, this module, Google Play Services, or speaker discovery works inside that environment. The current VMOS materials establish a rooted Android 9 guest, but do not establish Magisk/Zygisk installation or LSPosed operation there. No claim about working Xposed hooks or speaker pass-through in VMOS is made here.

## Sources inspected

- [VMOS brand homepage](https://www.vmos.com/) — current product-family page and Android 9 guest description.
- [Current VMOS app page](https://app.vmos.com/) and [official Play listing, `com.vmos.virtual`](https://play.google.com/store/apps/details?id=com.vmos.virtual) — direct distribution links, Android 9 guest, guest root, and Play metadata. The listing's Xposed-installer comment is an individual review, not vendor documentation.
- [LSPosed framework README](https://github.com/LSPosed/LSPosed) — Android range, Magisk installation requirement, and Xposed module-API compatibility statement.
- [Google Play VMOS listing](https://play.google.com/store/apps/details?id=com.vmos.google) — package/publisher/update metadata and user reviews; not assumed to be the same app as the download linked from the brand homepage.
- [VMOS-Pro GitHub page](https://github.com/Yau08/VMOS-Pro) — third-party rehost; its own disclaimer says the software belongs to another author.
- [SystemEdxposed issue #4](https://github.com/LuigiVampa92/SystemEdxposed/issues/4) — 2021 user request concerning EdXposed and classic Xposed on VMOS Pro Android 7.
- [RollingSkyHack issue #1](https://github.com/colinxu2020/RollingSkyHack/issues/1) — included only as a user report about one VMOS Pro Android 7.1 guest; the maintainer's LSPosed comment concerns RollingSkyHack's own support and says nothing about LSPosed compatibility with VMOS.
- [VirtualXposed guide](https://github.com/android-hacker/VirtualXposed/blob/vxp/CHINESE.md) and [release history](https://github.com/android-hacker/VirtualXposed/releases) — virtual-install flow, module restrictions, declared Android range, and last stable release date.
- [NewBlackBox source](https://github.com/ALEX5402/NewBlackBox) and its `RELEASE_NOTES.md` — confirms removal of Xposed module management.
- [Blacks BlackBox source](https://github.com/Black00Z/Blacks-BlackBox) — modern app-virtualization fork; source and README checked.
- [ZCore README/source repository](https://github.com/ZENINXOP/BlackBox) — README claims compared with the included `Vspace-ze.zip` source archive; the ZIP checksum and Xposed loader/dependency path were inspected locally.
- [VirtualApp public repository](https://github.com/asLody/VirtualApp) — SDK/licensing and maintenance statements.
- [LSPatch repository](https://github.com/LSPosed/LSPatch) — rootless APK-injection model and supported Android minimum.
