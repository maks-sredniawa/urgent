# Translator brief

Hand this to whoever translates the app — including yourself with a machine translator, which is a
legitimate first pass and never a last one.

**Sections 1 and 2 are yours to write** for this app. Everything from section 3 on is craft that
transfers between apps unchanged, and is the reason this file is worth having rather than a one-line
"please translate `strings.xml`".

Translate from **English only**, never from another translation: every interpretation the first
translator made gets compounded, silently.

## 1. What the app is, in one paragraph

Urgent call is a free, ad-free Android app made by one person. It rings the phone for a **repeated
call**, meaning the same number calling again within a few minutes, while the phone is on silent, on
vibrate or in Do Not Disturb. The reasoning is that someone who calls twice probably needs you. The
second call rings at the **alarm** volume, because the ringer mode does not mute alarms. The user
picks whose repeated calls ring (anyone, their contacts, starred contacts, or callers they choose)
and can put a few people under *Ring at once*, whose first call already rings. There is no account
and no server; nothing leaves the phone. The reader has just missed a call that mattered, or is
setting the app up so they will not: write for someone calm but serious about being reachable.

## 2. The rules that outrank fluency

A fluent translation that breaks one of these is worse than a clumsy one that keeps it, because the
failure is invisible to everyone who cannot read the language.

### 2.1 Urgent never changes the phone's settings

It does not touch the ringer mode, the ring volume or Do Not Disturb; it plays its own ringtone *at*
the alarm volume. `home_ring_through_hint` says so: *"Your phone's own settings are left as they
are."* No string may say or imply that Urgent turns the sound on, raises the volume, switches off
silent mode or overrides Do Not Disturb. *"Rings at your alarm volume"* names a level that already
exists; it must not become "raises the volume to the alarm level".

### 2.2 Hedged claims stay hedged

The app cannot always know what Android or the phone maker will do, and the copy says exactly as
much as it knows.

- `home_intro`: someone who calls twice *probably* needs you. Not "needs you".
- `home_battery_title`: repeated calls *may* stay silent. Not "will".
- `home_autostart_hint`: *"the phone does not tell Urgent what you chose"* is a fact the user needs.
- `support_rate_hint`: rating *"unlocks nothing"*. Keep it.

### 2.3 Privacy sentences are exact

`home_contacts_scope_hint` (reads contacts *on the phone*, only *to check each repeated call*),
`home_permissions_missing_body` and `support_report_hint` (*"nothing you have recorded in the app"*,
*"Nothing is sent until you tap send"*) state what the app reads and why. Translate each claim; do
not drop one, merge two, or add a reassurance the English does not make.

### 2.4 Android's words are Android's

The user will look for these on their own phone, so use the words **Android itself uses in your
language** (AOSP's system strings and the Settings app), not a fresh translation: *silent*,
*vibrate*, *Do Not Disturb*, *alarm volume*, *Settings*, *app permissions*, *call log*, *battery
use* and its *Unrestricted* option, *Recents*, *starred* contacts, *notification*. Xiaomi's
*autostart* is Xiaomi's word: use what MIUI and HyperOS show in your language.

### 2.5 A label quoted elsewhere keeps its exact words

The UI names some of its own controls inside other sentences, and each quotation must match the
label letter for letter.

- *Ring at once*: `home_ring_at_once` (the list), `ringing_at_once_title`, `ringing_at_once_body`,
  `home_ring_through_paused_hint`, `channel_ringing_description`.
- *Repeated call*: `ringing_title`, and the same noun in every sentence about it.

## 3. Register

- **Source is British English** (*colours*, *licences*). Use your language's standard written form.
- **Second person, informal, as in every MaksForge app:** de *du*, fr *tu*, it *tu*, es *tú*, pt-BR
  *você*, cs *ty*, uk *ти*, pl *ty*. Hold it across every string.
- **The app is "Urgent" in sentences**, never "we". The developer speaks as "I" only in
  `settings_language_report_hint` (*"Tell me if…"*).
- **Sentence case** for buttons, titles and labels. Not Title Case, and not ALL CAPS.
- **Calm and plain.** No exclamation marks and no emoji.
- Use your language's own quotation marks: German „ “, French « », Polish „ ”, Czech „ “,
  Ukrainian « », Italian and Spanish « », Portuguese “ ”.

## 4. Do not translate

Three kinds of string, marked `translatable="false"` in `strings.xml` so lint and the gate leave
them alone. If you find yourself wanting to translate one, that is worth a conversation rather than
an edit.

- **The app's own name.**
- **Language names in the switcher.** They are **endonyms** — each language named *in its own
  language*, in every locale — so someone who has landed in a language they cannot read can find
  their way out by a name they recognise. `settings_language_english` reads "English" in every file.
- **Brand names, and anything with a legal identity.** A product name that is the same in every
  market stays the same; translating it invents a different product.

### Things that look untranslatable and are not

- **Unit symbols.** SI symbols look invariant and are not: spacing, the decimal separator, and
  sometimes the symbol itself change by locale. Keep them in resources.
- **Numbers inside sentences.** Grouping and decimal separators are locale-specific. Never build a
  number into a string by hand.
- **Dates and times.** Format them through the platform, never by concatenating a resource.
- **Sample and debug data.** Not resources at all — they live in `src/debug/` and never ship.

## 5. Vocabulary

Keep a table here of **every English word that appears more than once**, with the target word for
each context. This is the single highest-value section of the file.

The trap it exists for: **one English word is often several target words.** English *Normal* covers
six different nouns in six different genders; a translator working string-by-string will produce a
different form each time, all of them defensible, and the app will look sloppy in a way no test
catches. Likewise *Name*: a person's given name and an organisation's name are different words in
most languages.

| English | Resource ids | Why they differ | Example |
| --- | --- | --- | --- |
| Ring at once | `home_ring_at_once`, `ringing_at_once_title`, `ringing_at_once_body` | One name everywhere: the list's name, quoted in the notification | pl: *Dzwoń od razu* |
| repeated call | `ringing_title`, `home_ring_through`, every `home_callers_*` | The app's one concept: the same number calling again. One noun everywhere | pl: *powtórne połączenie* |
| caller, callers | `home_callers`, `home_callers_*`, `home_ring_at_once_hint` | Whoever is calling, a person or a number. Not a contact entry | pl: *rozmówca* |
| ring, ringing | `home_ring_through`, `ringing_*`, `channel_ringing_*` | What the phone does audibly. Not "call", not "notify" | pl: *dzwonić* |
| silent, vibrate | `home_intro` | Android's ringer modes, in Android's words (§2.4) | pl: *wyciszony*, *wibracje* |
| contacts, starred contacts | `home_scope_*`, `home_callers_*`, `home_contacts_*` | Android's Contacts and its star, in Android's words | pl: *kontakty*, *oznaczone gwiazdką* |
| Pause, Paused, Resume | `home_pause_for`, `home_paused_until`, `home_ring_through_paused_hint`, `home_pause_resume` | One verb pair everywhere | pl: *Wstrzymaj*, *Wznów* |
| Settings | `tab_settings`, `home_*_denied` | In a sentence it is the phone's Settings app; capitalise as your Android does | pl: *Ustawienia* |
| Help and feedback | `settings_support`, `support_title` | One name, shared by every MaksForge app | |

Words that genuinely *are* one word everywhere — *Settings*, *Close*, *Edit*, *Delete* — belong in
the same table with a note saying so, otherwise someone will vary them for variety.

## 6. Traps

### 6.1 A substituted fragment has to fit its sentence

English composes freely because it barely inflects. `"in the last %1$s"` with `"30 days"` works;
the same substitution in an inflecting language needs the fragment in the right case, and the case
depends on the host sentence.

**Pre-inflect the fragment for its host, and say in a comment which host it belongs to.** If one
fragment is used by two hosts that need different cases, it needs to become two resources — that is a
code change, and it is cheaper to find now than after the translation.

### 6.2 The app knows nobody's gender

Not the user's, not the subject's, not any third party's. English hides this behind *they* and *you*.
Most other languages do not, and a translator forced to choose will pick one, producing copy that is
wrong for half the users and reads as an apology to the rest.

**Every string with a human doing something in it needs checking**, and the fix is usually to rewrite
the *English* into a form that does not need a gender — a noun phrase instead of a verb with a
subject. That is a change to the base language, made once, rather than a workaround in eight files.

### 6.3 Names the app cannot touch

Anything the user typed — a name, a label, a note — is substituted verbatim into a sentence. It
cannot be inflected, so the sentence has to be built so that it does not need to be. Watch for
possessives and for any preposition that would govern a case.

### 6.4 Plurals

**A count-dependent string is a `<plurals>`, never string concatenation, and never `if (n == 1)`.**

Which categories a language uses is **CLDR's** decision, not the translator's. English has `one` and
`other`; Polish has `one`, `few`, `many` and `other`; Arabic has six. Supplying only the English pair
in a language that needs four is a silently wrong app — `TranslationTest` fails the build on it,
which is the whole reason that test exists.

`%d` is not always the right placeholder either: some categories in some languages read better
without the number, and `quantity="one"` in Polish means *exactly* one, not "the singular".

### 6.5 Format arguments

- **Positional (`%1$s`), never bare (`%s`), whenever there is more than one.** Word order changes; a
  translator must be able to reorder the arguments, and bare placeholders make that impossible.
- **The same arguments must appear in every locale.** A missing one crashes at format time, on a
  screen you did not test in that language. `TranslationTest` checks this continuously.
- **A literal `%` must be escaped as `%%`.**

## 7. What happens to your draft

1. It goes into `res/values-<tag>/strings.xml`, or — while it is still a draft — into
   `translations/<tag>/` one directory up, which is staged and reported but **never shipped**.
2. `TranslationTest` checks it mechanically: format arguments, plural categories, orphans,
   untranslatable resources, array lengths. This runs on every build.
3. `scripts/translation-gate.py` checks it is *complete*, and that nothing has gone **stale** — the
   English changed and the translation did not. A stale translation is invisible to every other
   check: it parses, it carries its arguments, and it says something the app no longer means.
4. The language is offered to users only when its tag is added to `res/xml/locales_config.xml` and
   `AppLanguage`. Until then it exists and reaches nobody, which is the correct state for a draft.

## 8. What a language ships on

**An audit against §2, a look at it on a phone, and a route back — a native speaker's read-through
when you can get one, but not as the gate.** The first app built from this template promised a
native read-through per language and shipped nine languages; seven reviewers never materialised, and
the promise had one outcome — mechanically green drafts sitting unshipped while they rotted against
every English string added after them. A read-through was doing two jobs. *Fluency* needs a native
speaker; the rules that outrank fluency (§2) are a bounded checklist answerable from this brief and
the string. So the second half is the gate, and the first half gets a channel after shipping.

Before a language goes into `locales_config.xml`:

- **Every string that §2 governs is read against §2**, in that language. Nothing softened, sharpened
  or promised beyond what the app can know.
- **The §5 table is checked for consistency**, because a term translated two ways is the failure that
  makes an app look sloppy in a way no test catches.
- **Every string that names another — a button, a section header — uses that string's exact word.**
  Both halves read fluently on their own, so only the pairing is wrong. Once a pairing is found, add it
  to `quotedPairs` in `TranslationTest`, which holds it in every locale from then on.
- **It is compiled, installed and looked at.** A staged draft has never met `aapt2`: clipped labels and
  lint's plural warnings are invisible until it does, and neither needs a native speaker to see.

After it ships, a way to report a bad string is how fluency findings arrive, as ordinary bug
reports. **That route is already wired**: *Settings → Language → Something read wrong?* opens a mail
tagged `#language` with the resolved locale already in it (`ui/support/SupportHandoff.kt`), which is
the one fact such a report cannot do without and the one nobody should have to type. It sits beside
the picker rather than on the Support screen because that is where somebody is standing the moment a
translation reads wrong. **What a report must not do is talk
the app back across §2**: the reporter has the fluency, and this file has the reasoning.

If this is not your policy, replace this section *and* say so in ADR-0004 — `translation-gate.py`
cites it as the thing no script stands in for.
