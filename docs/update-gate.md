# Update/setup gate: static analysis only

The visible `force_upgrade_fragment_subtitle_device_setup` text says Google Home is out of date and must be updated to set up the device. Static code reveals at least two device-compatibility paths that can produce the `APP_UPGRADE` reason before setup proceeds:

1. `defpackage.mpn.a(uqt)` compares the received setup protocol version (`uqt.a`) with `mqg.b`. If it is higher, the app records “Setup protocol version not supported” and calls the device setup handler with `mqd.APP_UPGRADE`.
2. `defpackage.mqg` also reports `APP_UPGRADE` when the device BLE beacon version (`uqt.w`) exceeds 3.

`mqg.b` is initialized to 9 or 10 depending on remotely configured flag `usi.cw()`. The setup activity routes `APP_UPGRADE` to `nee.a(2)`, which shows the device-setup update screen. This means the UI wording alone does not establish that the Home APK's local version comparison is responsible; a device protocol or beacon revision can be the actual trigger.

No runtime observation establishes which condition applies to a target speaker. Do not suppress the dialog or spoof version fields: that could leave Home unable to complete the protocol exchange. Reproduce on a restorable test instance, record the received version and branch without account/device identifiers, then decide whether a documented manual route exists. The language hook does not fix this gate.
