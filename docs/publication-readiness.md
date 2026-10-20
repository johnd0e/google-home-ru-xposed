# Publication readiness

This document separates the source repository from local research and device artifacts.

## Include in the source repository

- Root Gradle project files and wrapper.
- `module/src/`, the compile-only Xposed API JAR, and the existing DexKit license notice.
- The public-facing documentation under `docs/` and the root README/third-party notices.

## Keep out of a public source release

- Google Home and Google app APKs. They are user-supplied proprietary applications and are not needed to build the module.
- Decompiled Google application sources or other extracted proprietary assets.
- Debug APKs, signing keys, local SDK/JDK/Gradle caches, proxy settings, and Android user state.
- Per-device logs, account identifiers, authentication headers, device tokens, and unredacted setup captures.

The root `.gitignore` excludes these generated/local items. Review the staged file list before any upload; ignore rules do not protect against explicitly force-adding a file or creating an archive manually.

## Open before calling this a release

1. Decide whether the diagnostic Toast and request-language logging should remain enabled in a public production build; the current behavior is deliberately noisy.
2. Verify Xposed loading and the hook on a recoverable test device/framework. Record the exact Android, framework, and Google Home versions without account or device secrets.
3. Confirm the server accepts the configured locale and validate the speaker's actual setup and voice behavior. Static inspection and a successful build do not establish this.
4. If distributing a production APK, create a release-signed build with a documented key-management process. The debug APK is only marked untested and is not a production artifact.

The MIT copyright line uses `johnd0e`, inferred from the source repository owner. Replace it if that is not the correct rights holder. Third-party notices remain separate.

No release signing or device-side validation has been performed.
