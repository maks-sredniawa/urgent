# Urgent call

Rings your Android phone when the same number calls twice within a few minutes — even while the
phone is on silent, on vibrate or in Do Not Disturb. Someone who calls straight back probably needs
you.

Android only lets repeated calls through inside Do Not Disturb; silent and vibrate mode mute them
like any other call. Urgent hears the call as your caller ID app, checks its own short record of
recent calls for an earlier one from the same number, and plays your ringtone at alarm volume. It never changes your phone's sound settings.

Free, no ads, no server, no account. Nothing leaves the phone.

## Status

In closed testing on Google Play. Play refused production access to the call log four times, so
Urgent no longer reads it ([ADR 0007](docs/adr/0007-urgent-remembers-recent-calls-instead-of-reading-the-call-log.md)).

## Setup on the phone

1. Turn on **Ring for repeated calls**, allow the phone, notification and contacts permissions, and
   set Urgent as your **caller ID & spam app** (Android 10+).
2. On Xiaomi, turn on **autostart** for Urgent.

Android 9 cannot run it, and on Android 10 calls from saved contacts never reach it.

## Contributing

Urgent is source-available, not open source: read it, verify it, file issues. Pull requests are not
accepted — see [LICENSE](LICENSE).

## Build

```bash
./gradlew assembleDebug          # build
./gradlew installDebug           # build + install on the connected phone
./gradlew test                   # JVM unit tests
./gradlew connectedAndroidTest   # instrumented tests — needs a device
./gradlew spotlessApply          # format (the CI gate is spotlessCheck)
python3 scripts/project.py       # what the toolchain thinks this app is called
```

Commit subjects are [Conventional Commits](https://www.conventionalcommits.org); release-please
derives the version and `CHANGELOG.md` from them. See [docs/RELEASING.md](docs/RELEASING.md).
