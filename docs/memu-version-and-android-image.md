# MEmu release versus Android guest image

MEmu's desktop application version and the Android version inside an emulator instance are separate values. A setup guide naming MEmu 9.2.0 and Android 5.1/API 22 identifies the versions used by that guide; it does not establish that MEmu 9.2.0 is the latest desktop release or that Android 5.1 is the only usable guest image.

## Android 5.1 requirement

Android 5.1 is a reproduction target from the original procedure, not a proven technical requirement of the language hook. The examined Google Home 2.25.1.5 manifest declares minSdk 21 (Android 5.0); Android 5.1/API 22 is above that minimum. Other Android images would need separate validation of Home's setup/update flow, Google Play Services, the Xposed framework, process/class-loader behavior, and speaker discovery.

## What remains unverified

The available manufacturer reference documents MEmu root mode but does not provide a current Android-image availability matrix. The currently offered MEmu releases and guest images have not been independently confirmed in this research. Check the desktop version and Android guest version separately in Multi-MEmu; do not infer one from the other.

## Sources

- The original setup procedure names MEmu 9.2.0 and Android 5.1/API 22 as its test environment; this is a report of that procedure, not a current-version claim.
- The examined Google Home 2.25.1.5 manifest declares minSdk 21 and targetSdk 29.
- [MEmu 3.3 root-mode article](https://www.memuplay.com/blog/three-new-functions-of-memu-3-3.html) supports the root-mode reference only; it does not document the current guest-image catalog.
