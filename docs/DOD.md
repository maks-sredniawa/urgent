# Definition of done — what is still open

The **live checklist**. Keep it short: when an item closes, tick it, write the *result* into whatever
long-form record you keep, and delete the detail from here. A session should be able to pick up the
work by reading this file alone.

## The standing schema gate — parked

Urgent has no database (ADR-0001's amendment). The gate's five points come back verbatim from the
template's `docs/DOD.md` the day a feature adds a table.

## Prove it on a real call

- [ ] **Two calls from another phone, this one on silent.** The first stays silent, the second rings
      at alarm volume and stops when it is answered or declined. Then again on vibrate, and once with
      Do Not Disturb on. `PHONE_STATE` cannot be faked over adb, so this is the only end-to-end test.
      Silent, 2026-10-13, R8 release build on HyperOS: the first call stayed silent, the second rang
      and stopped when the call ended unanswered. Answering, declining and *Silence* while it rings,
      vibrate, and Do Not Disturb are still to run on the phone. The API 26 emulator ran answering,
      declining and *Silence* with silent and DND *Alarms only* on together; vibrate, silent without
      DND and *Priority only* did not run there.
- [x] **Decide whether Urgent can be on Google Play at all.** Yes: the `READ_CALL_LOG` declaration
      (*Device automation*, answers in [`play-app-content.md`](play-app-content.md)) passed review;
      0.6.0 installs from Play.
- [x] **Run the call path on an API 26 phone or emulator.** Done 2026-10-18 on an API 26 emulator,
      R8 build, calls from `adb emu gsm call`. It crashed on the second call: before Android 9 reading
      the Do Not Disturb policy needs access Urgent never asks for. With that fixed, silent with DND
      *Alarms only* rings on the alarm stream and vibrates as an alarm, normal mode stays silent, and
      answering, declining and *Silence* each stop it. The emulator reaches silent only through DND
      *Alarms only* (the volume rocker skips vibrate, having no vibrator), so silent alone and vibrate
      did not run. `SoundSettingsTest` now holds the read on CI's API 26 leg. What remains
      unknowable there: in DND *Priority only* Urgent cannot see the policy, so it may ring
      alongside any caller the phone lets through — calls from anyone, contacts or starred
      contacts, or repeat callers.
- [ ] **Chosen callers.** With this phone on silent, add one number from contacts and one typed by
      hand, then call twice from a chosen number and twice from another. Only the chosen one rings.
      Use one contact saved without its country code and one with an extension (`…,567`). The API 26
      emulator passed it on 2026-10-18: a picked contact saved as `5550201` rang for `+15550201`, one
      saved as `5550205,567` rang for `5550205`, a typed number rang, and a stranger and an unchosen
      contact stayed silent. Still to run on the phone.
- [ ] **Everyone in your contacts.** With this phone on silent and the switch on, call twice from a saved
      contact and twice from a number that is not saved. Only the contact rings. Needs a second phone, as above.
      The API 26 emulator passed it on 2026-10-18, and *Starred contacts* too: a starred contact rang,
      a saved unstarred one stayed silent. Still to run on the phone.
- [x] **Ring at once.** Passed 2026-10-22 with real calls on a Xiaomi 24115RA8EG (HyperOS 3,
      Android 16), R8 release build of `main`: it rang on silent, and in DND *Priority only* with the
      ringer normal the phone rang by itself and Urgent stayed quiet.

## Before the first upload

- [x] **Delete what the app does not need** — Room, photos, backup and reminders are gone.
- [x] **Decide `minSdk` deliberately.** 26, kept on 2026-10-13; the reason is beside it in `app/build.gradle.kts`.
- [x] **Replace the placeholder mark.** Done 2026-10-17: two calls, the second wearing a ring,
      authored as four circles in `art/mark.py` with no traced image; provenance in `art/README.md`.
- [x] **Replace the placeholder domain** with `call/` and `ui/home/`.
- [x] **Rewrite `scripts/edge-to-edge.py`'s SCENES** against Home, Settings, Support and Licences.
- [x] **Make the artifact allowlists yours.** All four `aab-*.py` pass on a local `bundleRelease`.
- [x] **Choose the palette.** Done 2026-10-17: slate-blue primary kept, tertiary moved to amber and
      the neutral to a warm grey, so the surfaces lean towards the mark's charcoal ground. Every
      contrast check passed on regeneration, the tightest being the divider (outlineVariant on
      surfaceContainerLow) at 1.55:1 against a 1.2 floor.
- [x] **Put a real support address in `ui/support/SupportConfig.kt`** — `urgentcall.support@maksforge.com`.
- [x] **Write the listing.** English and Polish text in `docs/store-listing.md`, `--strict` green.
- [x] **Capture the screenshots.** Done 2026-10-17 from the release build of the redesign, English and
      Polish, in `art/play-screenshots/`.
- [x] **Write the privacy policy** — served at `https://maksforge.com/urgent/privacy-policy`. The
      first URL, `https://srednimax.github.io/urgent/privacy-policy.html`, was retired on 2026-11-07
      and now returns 404.
- [x] **Answer the Console's App content pages and write [`play-app-content.md`](play-app-content.md)
      in the same sitting** — the reasons are in front of you then and gone a week later. It moves in
      the same commit as the privacy policy, every time.
- [x] **`LICENSE` and `README.md`** — all rights reserved, source-available, as Gloam.
- [x] **Back up the upload keystore.** Backed up to NordPass on 2026-10-13.
- [x] **Set up the GitHub repository.** Done 2026-10-13: ruleset `main: require CI`, rebase-only,
      Pages, `RELEASE_PLEASE_TOKEN`. The token, renewed 2026-11-01, expires 2027-11-01: regenerate it
      and re-set the secret before then, or no release PR appears.
- [x] **Set the five Play secrets and create the service account** (`docs/RELEASING.md`) — the
      service account is the one step CI cannot do for itself. No account permissions; four app
      permissions, on this app only, all granted on 2026-10-13; no expiry date.
- [x] **Plan the closed test.** Testers recruited through a tester-swap community; on 2026-10-31 the
      14-day window had two days to go.
- [x] **Decide the `applicationId` deliberately.** It is fixed the moment the Play entry is created —
      not renameable, not transferable without losing every install and review.
      `io.github.srednimax.urgent`, fixed when the Play entry was created (2026-10-13).

## Before the next upload

- [x] **Re-declare contacts for ring-time reads and *Ring at once*.** Done 2026-10-22. Play has no
      `READ_CONTACTS` form: the "Console's READ_CONTACTS answer" this item named was the Data safety
      answer, and it stays *No*. The `READ_CALL_LOG` review text now names *Ring at once*
      ([`play-app-content.md`](play-app-content.md)).
- [x] **Declare the keep-alive foreground service.** `specialUse` *Other* on file since 2026-10-16; the
      duplicate under `mediaPlayback` unticked 2026-10-22
      ([`play-app-content.md`](play-app-content.md), Foreground service permissions).
- [x] **Send both Console changes for review.** The `READ_CALL_LOG` review text and the unticked
      `mediaPlayback` duplicate were sent on 2026-10-22, ahead of the 0.7.0 upload.

## After the closed test

- [x] **The production-access application is in, with its answers recorded verbatim** in
      [`play-app-content.md`](play-app-content.md). Submitted 2026-11-01; Play says seven days or less.
      Until it is granted the production track does not exist. The app this template was extracted
      from waited one day; that is one datapoint, not a promise. **If it comes back rejected, read
      the reason before changing any artifact.** Changing the build first leaves you arguing the
      reason against a different build.
- [ ] **Decide the first production number.** release-please never jumps to `1.0.0` by itself (with
      the major at 0 even `feat!:` bumps the minor), so it takes a `Release-As: 1.0.0` footer
      ([`RELEASING.md`](RELEASING.md)), or a deliberate decision to stay on 0.x. In order: production
      access granted, *then* that version's release notes in every locale on an ordinary branch (never
      on release-please's own, which it force-pushes), *then* the AAB and the listing go up together.

## Standing checks that never close

- [ ] **Every release: run the artifact checks on the built AAB**, not on the source.
      `aab-permissions.py` is the one that finds what a *dependency* merged into your manifest —
      a permission you never declared, or a `uses-feature` that quietly filters the app off devices.
- [ ] **Every release: read the release notes gate's output** rather than trusting it passed.
- [ ] **Every dependency bump: re-run `licensee`.** The build fails on an unallowed licence, which is
      the point — but the fix is two things, the allowlist *and* the bundled text.
- [ ] **Test the release-shaped build on a device.** A missing R8 keep rule does not crash; it makes
      a feature silently stop working. On 2026-10-22 the R8 release build of `main` (versionCode 95)
      on HyperOS 3 rang for *Ring at once* on real calls. On 2026-10-31 `-PreleaseShapedDebug` of
      0.7.0 (versionCode 99) on a Redmi Note 8 Pro (Android 11) ran Settings (theme, wallpaper
      colours, language), all three Support hand-offs and Licences without a crash.
- [ ] **Confirm Data safety at every Play declaration.** Last re-read 2026-10-22: the Console answer is
      still *No*, because chosen numbers stay on the device and Auto Backup is Android's transfer
      ([`play-app-content.md`](play-app-content.md), Data safety).
- [ ] **Keep `urgentapp.support@gmail.com` alive and check its inbox by hand.** Builds 0.2.0 to 0.6.0
      mail support there and cannot be changed, and it is still the IARC rating contact
      ([`play-app-content.md`](play-app-content.md), Content rating). Never delete the account, and sign
      in at least once every two years: Google deletes a personal account inactive that long. It is not
      forwarded, so read it now and then for mail from those builds.
