# Urgent remembers recent calls itself instead of reading the call log

## Context

Google Play refused the `READ_CALL_LOG` declaration (*Device automation*) four times between
2026-11-19 and 2026-11-24, each time on the same two findings: the listing and the in-app experience
"do not match the core functionality for your declared use case", and the video "does not
demonstrate the functionality". Listing, instructions and video changed between attempts; the
wording did not. The record is in [`play-app-content.md`](../play-app-content.md#sms-and-call-log-permissions).

The likely reason is in the policy itself. Play's SMS and Call Log policy lists as an **invalid** use
case "SMS or phone notification enhancement and alerts (when the app is not the default handler or
an eligible exception)" ([Play Console Help 9047303](https://support.google.com/googleplay/android-developer/answer/9047303)).
Ringing louder for a call is a phone alert enhancement, and Urgent is not the default dialer. An
appeal was filed on 2026-11-25 (ticket 6-7138000041278), but nothing in the four refusals suggests a
fifth wording will pass.

`READ_CALL_LOG` does two jobs today, and both have to be replaced:

1. **The ringing number.** Since Android 9 the `PHONE_STATE` broadcast carries
   `EXTRA_INCOMING_NUMBER` only to holders of `READ_CALL_LOG`; `PhoneStateListener` passes an empty
   string without it, and `TelephonyCallback` has no number at all
   ([Android 9 changes](https://developer.android.com/about/versions/pie/android-9.0-changes-all)).
2. **"Called before".** The call log is where `RepeatCall.kt` finds the earlier call.

What the platform offers instead, read from the reference and AOSP Telecom (android10-release
through android16-release):

- **`CallScreeningService` with `ROLE_CALL_SCREENING`** (Android 10+) is bound by Telecom for each
  incoming call and gets the full number in `Call.Details.getHandle()` with no permission check
  (`ParcelableCallUtils.toParcelableCallForScreening`). The role needs no permission and no Play
  declaration; Android's own guide says it "eliminates the requirement to obtain the READ_CALL_LOG
  permission".
- **Contacts are screened only with `READ_CONTACTS`**, and only from Android 11:
  `CallScreeningServiceFilter` skips the binding when the number is a contact and the app lacks the
  permission. On Android 10 `CallScreeningServiceController` binds only `if (!contactExists)`, so
  contacts never reach a non-dialer screening app there.
- **Android 8.0 and 8.1** still put the number in the `PHONE_STATE` broadcast for `READ_PHONE_STATE`
  alone. **Android 9** has neither path.
- Non-UI `InCallService` needs `CONTROL_INCALL_EXPERIENCE` or a companion-device role, and reading
  the dialer's notification is vendor-specific. Neither is a way in.

## Decision

**Urgent stops requesting `READ_CALL_LOG`.** It learns the ringing number from
`CallScreeningService` while holding the call-screening role (Android 10+), and from the
`PHONE_STATE` broadcast on Android 8.0–8.1, and it **keeps its own record of recent incoming calls**:
number and time, nothing else, pruned to the longest window it needs (30 minutes, or Do Not
Disturb's 15 when that is longer). "Called before" is a lookup in that record, not in the call log.

- The screening service always answers `allow` with nothing skipped: it never blocks, silences or
  hides a call, and Telecom's own ringing and call log are untouched. It answers **after** deciding,
  and starts the ringing service before it answers: Android lets an app start a foreground service
  while Telecom is bound to it, and refuses once Telecom has let go and battery use is *Optimized*.
- **`READ_CONTACTS` becomes part of setup**, asked with the role, so that repeated calls from
  contacts count on Android 11+. It stays declinable: without it only numbers outside the contacts
  are heard.
- The `PHONE_STATE` receiver stays for what it still does without the number: stopping the ring when
  the call is answered or ends, and the whole path on Android 8.x.
- **On Android 9 the repeated-call rule is unavailable.** Home says so instead of offering a switch
  that cannot work.
- The record is **live state, not a setting**: it is excluded from Auto Backup, it never leaves the
  phone, and the privacy policy and Data safety answers say it exists and how long it lives.
- The record starts empty. A call from before Urgent held the role, or from while it was switched
  off, is not known to it.

## Alternatives

- **Appeal, or a fifth declaration.** Filed as an appeal, and not built on: four refusals with the
  same template against a policy that names this use case as invalid.
- **Distribute off Play only.** Keeps every feature, but MaksForge apps are Play apps, and a
  sideloaded APK reaches almost nobody. It remains the fallback if this build is refused too.
- **Two builds: Play without the call log, a sideloaded one with it.** Two code paths for one
  mechanism, each tested only half the time, for the few users who would sideload. Rejected for now;
  the call-log path lives in git history if it is ever wanted.
- **Become the default dialer.** The only way to hold `READ_CALL_LOG` legitimately as a phone
  handler, and an entire dialer to build and support. Out of all proportion.
- **Read the dialer's notification with a `NotificationListenerService`.** Whether the number is
  there at all depends on the dialer and the ROM, and notification access is a heavier ask than the
  call log it replaces.
- **Raise `minSdk` to 29.** Would delete the Android 8.x path, which works without the call log and
  costs one branch. Android 9 is lost either way.

## Consequences

- **Contacts on Android 10 are never heard**, and **Android 9 loses the feature** entirely. Both are
  platform limits, said plainly in the app and the listing.
- **"Count declined calls" can no longer be honoured as it is.** The call log said whether a call
  was declined; the call state only says a call rang and ended without being answered, which a
  missed call does too. The setting is removed or redefined in the phase that builds this, with
  the reason in this ADR's amendment.
- **The call-screening role is exclusive.** Taking it displaces another screening app the user may
  have, such as a spam blocker, and on some phones it may stop the built-in dialer's own spam
  screening (unconfirmed). Setup must say so before Android's role dialog appears.
- **The process is started by Telecom's binding** rather than by the broadcast. Read on the
  `pixel-api36` emulator on 2026-11-25: with the process killed, the first call started it and the
  second rang. Whether HyperOS's autostart still blocks the binding is unconfirmed until read on the
  phone.
- **The battery exemption stops deciding whether Urgent rings** on Android 10+. Read on the same
  emulator with battery use *Optimized*: answering Telecom first gave
  `ForegroundServiceStartNotAllowedException` and silence; starting the ring before answering rang.
  Home hides its battery card while the role is held. Unconfirmed on HyperOS.
- **`READ_CONTACTS` will need its own Play declaration** once the app targets Android 17 (from
  2027-01-27, [Play Console Help 16935362](https://support.google.com/googleplay/android-developer/answer/16935362)).
  *Call Screening* is one of its listed use cases.
- The rule that "the call log is the source of truth for called before" in `CLAUDE.md` ends with
  this ADR, and so does the `READ_CALL_LOG` declaration in the Console: once a build without the
  permission is live, the declaration is removed.
