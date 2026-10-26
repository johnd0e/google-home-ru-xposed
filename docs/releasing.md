# Publishing an UNTESTED build

The `Untested Release` GitHub Actions workflow builds and publishes the module from source. It runs unit tests, builds the debug APK, signs it with the stable project key, writes `SHA256SUMS.txt`, then creates or updates an `UNTESTED` prerelease.

## One-time setup

A repository maintainer must add these repository Actions secrets:

- `RELEASE_KEYSTORE_BASE64`
- `RELEASE_KEYSTORE_PASSWORD`
- `RELEASE_KEY_PASSWORD`
- `RELEASE_KEY_ALIAS`

The private keystore must stay outside the public repository. The project owner has a private signing-key package and a helper script that installs these secrets without printing their values. Do not commit keystore or password files; ask the project owner for the private package if you are the maintainer responsible for signing.

## Publish the current tag

1. Open **Actions → Untested Release → Run workflow**.
2. Select the default branch and enter `v0.4.0-untested` in the tag field.
3. Wait for tests, build, signature verification, and asset upload to complete.

Future tags matching `vMAJOR.MINOR.PATCH-untested` trigger the workflow automatically when pushed. The first stable-key APK will not update over the existing locally debug-signed APK; uninstall that build first. Later CI builds signed with the same key can update in place.

A passing CI run verifies compilation and unit tests only. Device, Xposed runtime, Google Home behavior, and speaker behavior remain untested.
