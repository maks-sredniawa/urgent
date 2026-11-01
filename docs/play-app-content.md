# Play Console — App content answers

The answers this app gave in the Console's **App content** pages, what each one rests on, and **the
fact that would change it**. The Console shows what was answered but never why, and Play asks again:
when the app changes, when the questionnaire changes, and once a year. A re-declaration made from
memory a year from now is how a true answer turns false.

Two rules keep this file honest:

- **The privacy policy and the Data safety form describe the same facts to two audiences**, and Play
  cross-checks them. [`privacy-policy.md`](privacy-policy.md) and this file move in the same commit.
  A change to one without the other is the drift this file exists to stop.
- **A claim about permissions is read off the built artifact, never off the manifest.** A dependency
  can merge a permission the source never declared, and that is invisible from `AndroidManifest.xml`.
  `scripts/aab-permissions.py` is the reader, it runs before every upload, and its `EXPECTED` and
  `FORBIDDEN` tables are this section's twin — edit them together.

## What the artifact carries

Read at **versionCode 88, versionName 0.6.1** by `publish-play.yml`'s verify step on 2026-10-20:

> 10 permissions, all accounted for; none of the 6 forbidden ones present; 0 `<uses-feature>` declared; 0 of the 1 activities lock their orientation, every one of them expected

| Permission | Whose | What it is for | Console consequence |
| --- | --- | --- | --- |
| `READ_PHONE_STATE` | ours | the ringing broadcast that tells the app a call has arrived | none: runtime, asked when the feature is switched on |
| `READ_CALL_LOG` | ours | the caller's number, and whether they called in the last few minutes | **Restricted**: Play's SMS and Call Log policy allows it only for default dialers and listed exceptions. The Permissions Declaration Form decides whether this app can be on Play at all |
| `POST_NOTIFICATIONS` | ours | the ringing notification's Silence button | none |
| `VIBRATE` | ours | vibrating with the ring | none: normal, install-time |
| `RECEIVE_BOOT_COMPLETED` | ours | restarts *Run in the background* after a reboot or an update, without waiting for the user to open Urgent | none: normal, install-time |
| `READ_CONTACTS` | ours | the contacts list behind *Add from contacts*, read on the phone; only the callers the user taps are kept. With *Everyone in your contacts* or *Starred contacts* on, the number of each repeated call, and of each call from the *Ring at once* list, is checked against it when it rings | Runtime, asked when either control is switched on, never at launch. Play has no `READ_CONTACTS` declaration form; Data safety is the only Console answer it touches, re-read on 2026-10-22 |
| `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MEDIA_PLAYBACK`, `FOREGROUND_SERVICE_SPECIAL_USE` | ours | `RingThroughService` plays the ringtone for up to a minute; `KeepAliveService` keeps the app running while *Run in the background* is on | **Foreground service declarations**: one for each type, `mediaPlayback` and `specialUse`, each with a short video, asked for in App content |
| `…DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` | AndroidX | its own non-exported receivers, signature-level | none: not user-visible |

**`READ_CONTACTS` and Play's API 37 policy.**

- **Policy:** an app targeting Android 17 (API 37) or later may request `READ_CONTACTS` only when the
  Android Contact Picker cannot meet its core functionality. A custom picker does not qualify. Play
  starts prompting affected developers in September 2026, and compliance is mandatory from 27 January
  2027 ([Play Console Help 16935362](https://support.google.com/googleplay/android-developer/answer/16935362)).
- **Our status:** `targetSdk` is 36, so the policy does not apply yet. `READ_CONTACTS` is requested only
  when the user presses *Add from contacts* or turns on *Everyone in your contacts* or *Starred contacts*.
  Those choices read contacts when a repeated or *Ring at once* call rings, which the contact picker cannot do.
- **Plan before `targetSdk` goes to 37:** move the flow to the system contact picker and drop
  `READ_CONTACTS`, or file the Console declaration with the core-functionality justification. While those choices exist the picker route is closed, so only the declaration
  is left. Re-read
  this section when `targetSdk` changes; the exact picker API is checked when that migration starts.

**Forbidden, and asserted absent**: `INTERNET`, `AD_ID`, `QUERY_ALL_PACKAGES`,
`REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`, `ACCESS_NOTIFICATION_POLICY`, `USE_EXACT_ALARM`. The first
two are what the Data safety and advertising-ID answers rest on; `ACCESS_NOTIFICATION_POLICY` keeps
Urgent out of Settings' *Do Not Disturb access* list, the usual route to changing Do Not Disturb.

**Two asks are not permissions and appear on no Play list**: the battery-optimisation exemption and
Xiaomi's autostart. Both are hand-offs to Settings. They need no declaration, and the privacy policy
names them anyway, because a user sent to two Settings screens should find both explained.

---

## Ads

**No.** Answered 2026-10-13.

Would change: any ad SDK, ever. `AD_ID` absent from the artifact is the check that would catch one
arriving through a dependency.

## App access

**All functionality is available without special access.** Answered 2026-10-13. The Console now calls this page *Sign-in details*.

There is no account and no sign-in. Would change: a feature behind a login.

## Content rating

**The IARC questionnaire.** Answered 2026-10-13, category *All other app types*, rating contact `urgentapp.support@gmail.com`. Every question answered No; the result was Everyone (ESRB), All ages (ClassInd), PEGI 3.

Not *Social or communication*: that category means the app's purpose is people talking to each other, and Urgent carries no conversation of its own.

Would change: user-generated content, communication between users, or a link out to anything that
has either. A support `mailto:` hand-off is not user-to-user communication — it opens the user's own
mail app, addressed to the developer — and a rate row opens Play's own listing, whose reviews are
Play's surface rather than the app's.

## Target audience and content

**18 and over.** Answered 2026-10-13.

Would change: lowering it. Any age band under 18 brings in the Families policy and its design and
SDK requirements.

## Government, financial and health features

**No to all three.** Answered 2026-10-13.

Health is the one to guard. **Keep health claims out of the listing, the site and the tags** —
Play's enforcement has treated a linked page as part of the listing. Would change: copy or a tag
that sells the app as good for you.

## Data safety

**No data collected. No data shared.** Answered 2026-10-13 against versionCode 18. Re-read on
2026-10-22 for the ring-time contacts check and *Ring at once*: "Does your app collect or share any of
the required user data types?" is still **No**.

Play's definitions, from its Data safety help page (support.google.com, answer 10787469) — re-read
them at each re-declaration rather than trusting this quote:

> "'Collect' means transmitting data from your app off a user's device."
>
> "User data accessed by your app that is only processed locally on the user's device and not sent
> off device does **not** need to be disclosed."

The answer rests on one fact per route data could take:

1. **The app cannot transmit anything.** `INTERNET` is absent from the artifact, and
   `aab-permissions.py` fails the upload if it ever appears. This one fact is most of the answer.
2. **The call log is read only on the device.** A few minutes of it, at the moment a call rings, to
   compare one number. Nothing from it is stored or sent. Still disclose it if the form asks about
   *access* rather than collection: the Permissions Declaration Form does.
3. **Auto Backup is still "no data collected".** The platform moves the user's backup to the user's
   own Google Drive; the developer never sees it. That is Android transmitting the user's backup, not
   the app transmitting to the developer. ⚠ Every file the backup carries lands on the user's *next*
   phone too — and here that is the settings, the chosen callers' numbers and names, and the theme (ADR-0005's amendment). Re-read on 2026-10-15 when the chosen callers landed, again when they kept names, and on 2026-10-22: the answer stands, because nothing reaches the developer.
4. **A support mail is still "no data collected", and the reason is the definition rather than an
   exemption.** A `mailto:` hands a draft to **another app**; the user reads it, can edit every line,
   and sends it from their own mail client, or does not. ⚠ **Do not write "Play exempts support
   flows" in a re-declaration.** The help page has no such exemption; the claim is true because of
   what "collect" means, and that is the reason to give.
5. **The contacts list is read only on the device: when the user presses *Add from contacts*, and, with *Everyone in your contacts* or *Starred contacts* on, to check the number of each repeated or *Ring at once* call when it rings.**
   Only the callers the user taps are kept, nothing from the list is sent, and the ring-time check keeps nothing. Still disclose it if the form
   asks about *access* rather than collection, as for the call log.

Would change any of it:

- **`INTERNET` on the artifact**, from our code or a dependency. Every answer here becomes a question.
- **Anything the app sends by itself**, such as crash reporting or analytics. There is none today.
- **Storing call-log rows, or contacts beyond the callers the user taps,** in the app's own storage, where a backup would carry them.
- **Reading the contacts list anywhere other than the *Add from contacts* button and the ring-time check.** The Data safety answer and the Console's permission questions need re-reading.

## Advertising ID

**No.** The artifact carries no `com.google.android.gms.permission.AD_ID`, and `aab-permissions.py`
forbids it. Answered 2026-10-13.

## SMS and call log permissions

**Device automation**, for `READ_CALL_LOG`. Saved 2026-10-13 and sent for review the same day with
the first closed-testing release (0.1.0, versionCode 18).

Play's list of permitted uses decides this, and Urgent fits none of it cleanly. *Device automation*
— "automate repetitive actions across multiple areas of the OS, based on one or more conditions
(triggers) set by the user" — is the closest: the triggers are a number calling again within the 5,
10, 15 or 30 minutes the user picks, or a number on the user's *Ring at once* list, and the action is
the ring. *Physical safety/emergency alert*
allows only `SEND_SMS`, and *Caller ID, spam detection and blocking* describes a different app.

*Instructions for review*, as saved for *Ring at once* and sent for review on 2026-10-22 (496 of
500 characters):

> Urgent rings through silent, vibrate or DND for user-set triggers: the same number calling again
> within 5, 10, 15 or 30 minutes, or a number on the user's Ring at once list. On a ringing call it
> reads the last few minutes of the call log on-device to compare one number; nothing is stored or
> sent, and there is no INTERNET permission. Android 9+ gives the ringing number only with
> READ_CALL_LOG; CallScreeningService skips contacts unless default dialer. Test: turn it on, set
> silent, call twice.

Would change: a refusal (Plan B is distribution off Play), the window choices, a new trigger, or any
use of the call log beyond comparing one number.

## Foreground service permissions

**`mediaPlayback`**, for `call/RingThroughService.kt`. Answered 2026-10-13 and sent for review the
same day with 0.1.0 (versionCode 18); the Console's task is *Media playback*.

- **What the user sees:** the ringtone playing on the alarm stream while a repeated call, or a call from the *Ring at once* list, rings, and
  a notification with a *Silence* button. It stops when the call is answered, declined or missed, or
  after a minute at most.
- **Why it cannot wait:** it exists only while the phone is ringing. Deferred, it rings after the
  call is over, which is no use to anyone.
- **Why this type:** the service's whole job is playing a sound the user hears. `phoneCall` is for
  the app that owns the call — a default dialer or one holding `MANAGE_OWN_CALLS` — and Urgent is
  neither. `specialUse` would claim no named type fits, and one does.
- **The video:** two calls from another phone to this one on silent, the second ringing and the
  notification visible. Link: https://drive.google.com/file/d/1OyPG5FtZvUvMMBXS_4AmYt_fBsvuuok4/view
  — 38 s, no sound (`adb screenrecord` has none, and Android's playback capture excludes the alarm
  stream anyway), the caller's name blurred. Anyone with the link can view it; keep it viewable
  while this declaration is on file — Play can re-review it on a later update or at the yearly
  re-declaration.

Would change: a change of type, or the ringing outliving the call.

**`specialUse`**, for `call/KeepAliveService.kt`. Answered 2026-10-16 under the task *Other*, with the
same description also filed by mistake as an *Other* task under `mediaPlayback`; that duplicate was
unticked and sent for review on 2026-10-22.

- **What the user sees:** a quiet, ongoing notification, *Urgent is running*, for as long as *Run in the background* is on.
- **Why it cannot wait:** the phone-state receiver should not wait for Android or the ROM to start Urgent again between calls.
- **Why this type:** the service plays no sound, carries no call and moves no data, so `mediaPlayback`, `phoneCall` and `dataSync` do not fit. The manifest carries the subtype text.
- **The video:** the notification with *Run in the background* on, while the app is not open. Link:
  https://drive.google.com/file/d/1vGGDZ3yOkAU2wD9lGxKNgaBtPqF9v9WU/view — keep it viewable while
  this declaration is on file, as for the `mediaPlayback` one.

Would change: a change of type, or the service running without *Run in the background* on.

## Store settings (not App content, recorded here because it is asked for the same reason)

- **Category:** Communication, not Tools — the reason is in [`store-listing.md`](store-listing.md).
- **Tags:** _<tags>_.
- **Contact email:** `urgentcall.support@maksforge.com`, public on the listing.
- **Privacy policy:** `https://maksforge.com/urgent/privacy-policy.html`, served from `docs/` by
  Pages. It updates on a merge to `main`, with no release, which is why it has to be right before the
  app it describes ships rather than after. Switched in the Console on 2026-10-25 from
  `https://maksforge.github.io/urgent/privacy-policy.html`, which still redirects here.

## Production access — the application, and the answers it was given

**Submitted 2026-11-01, 17:29.** Asked once, from the Console's Dashboard, after the closed test's 14
days. Recorded **verbatim**: the Console asks the same set again for the next app on this account, and
an answer re-read a year later has to be the one that was given. Each box takes at most 300
characters, which is why some answers are shorter than the 2026-10-31 draft (`b42270d`).

**About your closed test**

- *How did you recruit users for your closed test? For example, did you ask friends and family or use
  a paid testing provider?* — Through a community where Android developers test each other's apps
  during their closed tests.
- *How easy was it to recruit testers for your app?* — Easy.
- *Describe the engagement you received from testers during your closed test* — Urgent has no internet
  permission and no analytics, so I can't see which features testers used or how often. What I can
  see: testers stayed opted in through the 14 days, and their remarks in the community, which say they
  used the app.
- *Provide a summary of the feedback that you received from testers. Include how you collected the
  feedback.* — Testers posted remarks in the tester community where we test each other's apps. They
  said they liked the interface and that they had used the app.

**About your app**

- *Who is the intended audience of your app?* — Adults who keep their phone on silent, on vibrate or in
  Do Not Disturb, and still want to hear it when someone calls them twice in a row.
- *Describe how your app provides value to users* — Android lets a repeated call ring only in Do Not
  Disturb; silent and vibrate mode mute it. Urgent checks the call log for an earlier call from the
  same number within minutes and, if there was one, rings at alarm volume. It never changes sound
  settings. No ads, no account, nothing leaves the phone.
- *How many installs do you expect your app to have in your first year?* — 0 – 10K.

**Your production readiness**

- *What changes did you make to your app based on what you learned during your closed test?* —
  Testers liked the interface and asked for no change. From my own testing in the same window: fixed a
  crash on Android 8 when a repeated call came in; Urgent no longer rings over a call Do Not Disturb
  lets through; added a Ring at once list and a dedicated support address.
- *How did you decide that your app is ready for production?* — Real calls on Xiaomi phones, release
  builds: on silent the first call stayed silent, the second rang; a Ring at once caller rang on
  Android 16, and Urgent stayed quiet when Do Not Disturb let a call through. On Android 8, answering
  or declining stops it. The call-log declaration passed review.

*Rests on* (not part of the answers): 2026-10-13, a real call on silent, R8 release build on HyperOS:
the first call stayed silent, the second rang. 2026-10-22, *Ring at once* rang on real calls on a
Xiaomi 24115RA8EG (HyperOS 3, Android 16), R8 release build of `main`, and Urgent stayed quiet in Do
Not Disturb's *Priority only*. 2026-10-18, Android 8 was an **API 26 emulator, not a phone**: R8
build, calls from `adb emu gsm call`; answering, declining and *Silence* stopped the ringing. The
`READ_CALL_LOG` declaration passed review. All recorded in [`DOD.md`](DOD.md).

**Three things the first app built from this template kept deliberate, so a later answer does not undo
them:**

1. **The audience answer agrees with *Target audience* above.** A reviewer reads the two side by side.
   A mismatch is a question rather than a rejection, but it can cost days.
2. **Engagement is answered from what the app can know.** With no `INTERNET` permission there is no
   telemetry, so *"I do not know how many used X"* is the true answer. A guessed figure would
   contradict *Data safety* in this same file.
3. **The changes answer says where the changes came from.** If the testers reported nothing, say so and
   name what came from your own use instead. Presenting your own work as tester-driven is an easy
   dishonest sentence, and the application does not need one.

## A tip (Payments policy — not a question Play asks, but one a reviewer answers)

**Default: no tip on any surface the release controls.** Not in the app, and not on the pages the
listing links to. Read this before adding one, because the policy text and its enforcement disagree.

- **The text allows it.** Payments policy §3.2 treats a tip as a peer-to-peer payment, outside Play
  Billing, when **100% reaches the creator** and **nothing unlocks** — no badge, no theme, no
  thanks-screen. §4's anti-steering rule excepts §3. So: one personal payment link, worded as a *tip to
  a person*, never a *donation to a project*.
- **Enforcement has rejected apps anyway.** StreetComplete (February 2022) was rejected for in-app
  Patreon, Liberapay and GitHub Sponsors links — and **for a link to its own home page, because that
  page carried donation information**. A Buy Me a Coffee link in the app this template was extracted
  from was flagged the same way. None of those is the §3.2 shape (tiers unlock things, a platform takes
  a cut, a project is not a person), but the distinction has to survive a reviewer skimming for
  "external payment link", and a pre-launch rejection costs the closed-test window.
- **The reach is one level out.** The first app built from this template moved its tip from the app to
  the repository and the Pages site, then noticed that the listing's *Website* field and the last line
  of its full description point at exactly those pages. So "nothing the app links to" includes what
  the **listing** links to.
- **When to revisit:** once the app is live, with a track record to appeal from. The cheapest shape
  then is a tip on the repository alone, with the listing's Website field and full description kept
  off it. A Play Billing consumable is the unambiguous alternative, at the cost of a billing
  dependency, Google's cut, and an *in-app purchases* badge on the listing.

Region carve-outs (US external links since October 2025, the EEA's external-offers program) do not
help: both are fee programs with reporting obligations, which is not a serious route for occasional
tips. A sentence promising a *future* tip, with nothing to click, is a statement about pricing and is
fine anywhere.

---

## When Play asks again

1. Run `python3 scripts/aab-permissions.py` against the newest bundle, and compare its table with the
   one above. A new row is a new question.
2. Read `docs/privacy-policy.md` against the answers here. If one has moved, both move, in one commit.
3. Copy any value this file does not hold while the Console is open — a category, a date, a
   declaration's exact wording — so the next re-declaration can compare against it.
4. Re-date the sections you re-answered.
