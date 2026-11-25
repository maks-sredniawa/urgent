---
layout: default
title: Privacy Policy
---

# Privacy Policy

_Last updated: 2026-11-08_

Urgent is an Android app that rings your phone when the same number calls you twice within a few
minutes, or the first time a caller on its *Ring at once* list calls, even while the phone is
silenced. This policy describes what the app reads to do that, and
what it does with it. It is short because the app does very little.

## The app collects nothing

Urgent has no account, no sign-in, and no server. Nothing is transmitted anywhere. There is no
analytics, no crash reporting, no advertising, and no third-party SDK that phones home. The app does
not request internet access at all.

## What the app can access, and why

- **Incoming calls.** When a call comes in, Android tells the app that the phone is ringing and which
  number is calling: on Android 10 and later because you set Urgent as your *caller ID & spam app*,
  and on Android 8 through the phone-state permission. The app always lets the call through
  unchanged. It never blocks, silences or hides a call.
- **A short record of recent calls.** To know whether a number called before, the app keeps the
  number and time of each call that rang in the last 30 minutes, on your phone only. Anything older is
  deleted as the next call comes in. The record is left out of your phone's backup and is never sent
  anywhere. The app does not read your call log.
- **Your contacts, so that calls from them count.** On Android 11 and later, Android passes a call
  from one of your saved contacts to the app only while the app has the contacts permission, so
  switching Urgent on asks for it. Having the permission does not make the app read your contacts:
  it reads them only for the two uses below.
- **Your contacts, when you ask for them.** Pressing *Add from contacts* asks for permission to read
  your contacts. If you allow it, the app lists the phone numbers in your contacts on your phone, and
  keeps only the numbers and names of the contacts you tap. It keeps a name only when it is the
  contact's own name, never a nickname. The list is read on the phone and is never sent anywhere.
  Turning on *Everyone in your contacts* or *Starred contacts* asks for the same permission. Then, for
  each repeated call, and each call from your *Ring at once* list, the app checks the caller's number against your contacts on the
  phone, to decide whether to ring. It keeps nothing from that check and never sends it anywhere.
  Apart from those two uses, the app never reads your contacts.
- **Notifications**, only to show the *Silence* button while it is ringing, and a quiet one while *Run in the background* is on.

The app stores its settings — whether it is switched on or paused, how many minutes count as "again", which contacts choice you made, whether *Run in the background* is on, and which numbers and names you chose — and your theme and language choices.

The app may also send you to two of your phone's own Settings screens. It asks for neither as a
permission. It checks whether the battery exemption is on, to show or hide its reminder, and keeps
nothing from either:

- **Battery use**, so that Android lets it ring while it is in the background.
- **Autostart**, on Xiaomi phones only, so that the phone tells it about a call at all. Xiaomi does
  not report back what you chose.

## The things that happen outside the app

1. **Android's automatic backup.** If it is enabled on your phone, Android backs up the app's
   settings, including which numbers and names you chose, to your own Google Drive. You can turn this off in your phone's settings.
2. **Google Play**, if you installed the app from there. Installing and updating an app is a
   transaction between your phone and Google, with its own privacy policy.
3. **An email you choose to send.** *Help and feedback* opens your own email app with a draft that
   already holds your phone's model, its Android version, the app's version and the language the app
   is showing. You can read and change every line before sending it, or not send it at all. A sent
   email reaches the support mailbox and is used only to answer you.

## Children

The app is not directed at children and collects nothing from anyone.

## Deleting your data

Uninstalling the app removes the settings from your phone. If Android's backup saved them, a copy
stays in your Google Drive until you delete it from your Google account.

## Changes

If this policy changes, the date at the top changes with it, and the history of this file is public
in the repository.

## Contact

**urgentcall.support@maksforge.com**, or <https://github.com/maks-sredniawa/urgent/issues>
