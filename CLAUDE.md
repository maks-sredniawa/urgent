# Urgent call

Rings the phone for a **repeated call** — the same number calling again within a few minutes —
while the phone is on silent, on vibrate or in Do Not Disturb. Android's own repeat-callers rule
lives only inside Do Not Disturb; silent and vibrate mode mute the ringer outright, and this app is
the way around that. Free, no ads, no server, no account; nothing leaves the phone.

- **Vocabulary:** [`CONTEXT.md`](CONTEXT.md) — use these terms in code and UI.
- **Decisions and why:** [`docs/adr/`](docs/adr/) — read before changing anything they cover.
- **What's still open:** [`docs/DOD.md`](docs/DOD.md) — the live checklist. **Read this one first.**
- **The arc, in phases:** [`docs/PLAN.md`](docs/PLAN.md) — sequence only; a phase being built gets
  its own `docs/phase-N.md`. Read the phase you're in, not the file.
- **How to work here:** invoke the `lean-loop` skill at the start of every session and keep it
  active. This repo is written before it is built — plans, ADRs and phase documents are long on
  purpose — so the waste is re-reading what is already in the transcript and testing code whose
  shape has not settled.
- **Commits & releasing:** [`docs/RELEASING.md`](docs/RELEASING.md). Commit subjects **must** be
  [Conventional Commits](https://www.conventionalcommits.org) (`feat:`, `fix:`, `feat!:`, `docs:`, …)
  — a `commit-msg` hook rejects anything else, and release-please derives the version and
  `CHANGELOG.md` from them. `versionName` and `versionCode` are automated; never hand-edit either.

Don't restate ADR reasoning here; link to it. This file is loaded every session and must stay short.

## Working with the person who owns this repo

Fluent in JavaScript/TypeScript, new to Kotlin. Comment code where a Kotlin/Compose idiom has no
direct JS analogue — `Flow` vs promises/observables, `suspend` vs `async`, `remember` /
`derivedStateOf` vs `useMemo`, data class `copy()` vs object spread, sealed classes vs discriminated
unions. Explain the *why*, don't restate the line. Prefer explicit and readable over clever.

The Android **platform** is the other half of the gap: the manifest as a declaration the OS reads
before any code runs, permission models that differ per API level, process death, and vendor ROMs
overriding documented behaviour. Say when a constraint comes from the platform rather than from this
codebase.

## Stack

| Choice | Note |
| --- | --- |
| Kotlin, Jetpack Compose, Material 3 | Android only, `minSdk` 26 |
| **Navigation 3** (`androidx.navigation3`) | Replaces Navigation Compose 2.x — don't reintroduce the old one |
| **DataStore only — there is no database** | Settings, the chosen callers and a theme; the recent-call record in a file of its own |
| Manual DI via `AppContainer`, **not Hilt** | Constructor injection is clearer at this size and easy to migrate later |
| A `CallScreeningService`, a manifest `PHONE_STATE` receiver and one foreground service | `call/` — the app's whole mechanism |
| On-device storage only | No backend. Platform Auto Backup, no custom agent |

## House rules

- **Urgent never changes the user's phone settings.** It does not touch the ringer mode, the ring
  volume or Do Not Disturb; it plays the ringtone on the **alarm** stream, which ringer mode does not
  mute. A crash mid-ring therefore leaves the phone exactly as the user set it. Changing the ringer
  mode instead would need Do Not Disturb access and a restore path that has to survive process death.
- **It only rings when the phone would not.** `call/Silence.kt`'s `shouldRingThrough` is the gate;
  ringing on top of the phone's own ringtone is two sounds at once and reads as a bug. Two exceptions:
  with *From your contacts* off, Do Not Disturb's contacts-only or starred-only rule can let a saved contact
  ring the phone and Urgent together. Contacts stay unread unless the user opts in, and the store listing
  says so. Below Android 9 the DND policy is unreadable without access Urgent never asks for, so in
  *Priority only* it may ring alongside any caller the phone lets through.
- **Urgent's own recent-call record is the source of truth for "called before"**, never the call log
  ([ADR 0007](docs/adr/0007-urgent-remembers-recent-calls-instead-of-reading-the-call-log.md)): Play
  will not grant `READ_CALL_LOG` to an app like this. The number comes from the call-screening role,
  which always answers "allow" and decides before it answers.
- **A new preference is a key, a `Flow` that reads it with its default, and a `suspend` setter.**
  Keep the default in the read. Every key is backed up by Auto Backup, so ask what it means on the
  user's next phone: a setting is welcome, live state is not.
- **A branch merges with every shipped language complete.** Adding an English string does *not*
  redden your build — completeness is a merge gate (`scripts/translation-gate.py`), not a test, so
  copy is translated **once** rather than against draft wording and again after review. Everything
  else about a translation — format arguments, plural categories per CLDR, orphans,
  `translatable="false"` — is `TranslationTest` and holds continuously. Translate from **English
  only**, per [`docs/translator-brief.md`](docs/translator-brief.md).
- One `ViewModel` per screen, UI state as a single immutable data class.
- **Developer-only surface lives in `app/src/debug/`, never in `main/` behind `BuildConfig.DEBUG`.**
  With `isMinifyEnabled = false` a statically-false branch is still compiled into the release AAB —
  a hide, not a strip — and its strings are still inside the translation gate. The seam is
  `ui/settings/DebugSettings.kt`, one file per variant, with a no-op in `src/release/`.
- **Colours come from `MaterialTheme`, never literals.** The palette is generated from four seeds by
  `scripts/gen_scheme.py`; edit the seeds and regenerate, never a single role.
- **Spacing comes from `theme/Spacing.kt`.** Six steps on a 4dp grid; no screen invents a seventh.

## Commands

```bash
./gradlew assembleDebug          # build
./gradlew installDebug           # build + install on the connected phone
./gradlew test                   # JVM unit tests
./gradlew connectedAndroidTest   # instrumented — the manifest wiring the OS reads. Needs a device
./gradlew lint
./gradlew spotlessApply          # format (the CI gate is spotlessCheck)
./gradlew assembleDebug -PreleaseShapedDebug   # debug id, R8 on — find minification bugs early

python3 scripts/project.py                      # what the toolchain thinks this app is called
python3 scripts/translation-gate.py --report    # what translations does this branch still owe?
python3 scripts/translation-gate.py origin/main # the gate itself — would this merge?
python3 scripts/notes-gate.py
adb devices                                     # confirm the phone is attached
```

## Layout

```
app/src/main/java/<namespace>/
  MainActivity.kt, MainApplication.kt, AppContainer.kt, Navigation.kt, NavigationKeys.kt
               the app shell and Nav3 wiring stay at the package root, not under ui/
  call/        RepeatCallScreeningService (the way in, Android 10+), PhoneStateReceiver (call end, and
               the way in on Android 8.x), IncomingCall (one call's decision), PhoneState (permissions,
               the role, the sound settings), RepeatCall and Silence (the pure rules), RingThroughService
  data/        AppPreferences.kt — the DataStore keys and their defaults; RecentCalls.kt. No database
  theme/       generated palette, type scale, spacing, the night-mode window half
  ui/          Compose screens + ViewModels — home/ is the app; settings/, support/, common/
  work/        Xiaomi autostart and battery-optimisation hand-offs

app/src/debug/    Settings' debug section: ring now, stop ringing
app/src/release/  only the no-op half of that seam, so main/ can call it unconditionally
scripts/          the Python toolchain. project.py is the one place the app's identity lives
art/             mark.py is the identity; both generators derive from it
```

## Versions

Pinned in `gradle/libs.versions.toml`: **AGP 9.0.1, Kotlin 2.3.20, Gradle 9.1.0, Compose BOM
2026.03.01**. `compileSdk`/`targetSdk` 36, `minSdk` 26, JDK 21 toolchain.

`compileSdk` stays at **36**: it is the combination AGP 9.0.1 validates against. Bump deliberately,
with a build to prove it.

## Environment notes

**No Android Studio — everything is CLI**, `./gradlew` against a physical Xiaomi phone over USB.
`adb devices` before assuming it is attached.

- **Testing the real path needs a second phone, or an emulator.** `PHONE_STATE` is a protected
  broadcast and cannot be faked over adb on the phone; on an emulator `adb emu gsm call <n>` is a real
  incoming call, screening included. Grant the role with `adb shell cmd role add-role-holder
  android.app.role.CALL_SCREENING <package>`.
- **A brand-new `applicationId` is refused on HyperOS** with `INSTALL_FAILED_USER_RESTRICTED` and no
  dialog. `adb push` the APK to `/sdcard/Download/` and tap it in the file manager; updates then
  install over adb normally.
- **Xiaomi kills background work aggressively.** Without **autostart** the ROM may not start the
  process for the ringing broadcast or the screening binding. Autostart has no appop and lapses on
  its own — `scripts/device-gate.py` reads and sets it.
