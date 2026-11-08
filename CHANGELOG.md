# Changelog

## [0.8.0](https://github.com/maks-sredniawa/urgent/compare/v0.7.0...v0.8.0) (2026-11-08)


### Features

* pause ringing for a few hours from Home ([2d99fb0](https://github.com/maks-sredniawa/urgent/commit/2d99fb04ff3cc63403538fde04096d123decfcde))

## [0.7.0](https://github.com/srednimax/urgent/compare/v0.6.1...v0.7.0) (2026-10-22)


### Features

* ring at once for callers on a new list ([0daade4](https://github.com/srednimax/urgent/commit/0daade440f75a9ed745a3b90ecd56a61c49fac05))


### Bug Fixes

* keep reading the ringer mode with Do Not Disturb off ([d02e3bd](https://github.com/srednimax/urgent/commit/d02e3bdfc9fc74ee45a1ac56455a1841562b4b37))
* stop ringing over a call Do Not Disturb lets through ([4147d51](https://github.com/srednimax/urgent/commit/4147d51741103417aca4d47851e6ddf26c020bcd))

## [0.6.1](https://github.com/srednimax/urgent/compare/v0.6.0...v0.6.1) (2026-10-20)


### Bug Fixes

* send support mail to urgentcall.support@maksforge.com ([24d5347](https://github.com/srednimax/urgent/commit/24d5347f6ad9a584f8cc0550f1cc43c1cd262eed))
* stop crashing on a repeated call before Android 9 ([dcb1095](https://github.com/srednimax/urgent/commit/dcb1095ee26c6e6ddd1e7bb4ad16f6df1609ac95))

## [0.6.0](https://github.com/srednimax/urgent/compare/v0.5.0...v0.6.0) (2026-10-17)


### Features

* give Home the design's hierarchy and redraw the mark to its spec ([ec4fe9c](https://github.com/srednimax/urgent/commit/ec4fe9c76f396953a56b41bc8539e84318dbaa16))


### Bug Fixes

* categorise the ringing notification as an alarm so Do Not Disturb lets it pop up ([2ccf72e](https://github.com/srednimax/urgent/commit/2ccf72e27f0af8a316e35435cd4cdb350d44db81))
* outline unselected choice chips and drop KDoc that restates the code ([0928682](https://github.com/srednimax/urgent/commit/0928682c1f21a3071894e545719eee96059d8d8c))
* pop the ringing notification up with Silence in reach ([f294efd](https://github.com/srednimax/urgent/commit/f294efd59caf658b9affa4276cfa392129e27b84))

## [0.5.0](https://github.com/srednimax/urgent/compare/v0.4.0...v0.5.0) (2026-10-17)


### Features

* give the app its mark and a warm palette ([0e08941](https://github.com/srednimax/urgent/commit/0e089414ef70338d09ec001817cdf031a6493797))

## [0.4.0](https://github.com/srednimax/urgent/compare/v0.3.0...v0.4.0) (2026-10-17)


### Features

* rename the app to Urgent call ([5aa762b](https://github.com/srednimax/urgent/commit/5aa762bea98c5ae1b7671f7b76140583ccaea0ba))

## [0.3.0](https://github.com/srednimax/urgent/compare/v0.2.0...v0.3.0) (2026-10-16)


### Features

* add starred contacts, a list switch and background keep-alive ([5cb32cd](https://github.com/srednimax/urgent/commit/5cb32cd15e0a5a01a410845d9f65718252d33c54))
* choose callers from an in-app contact list ([465942b](https://github.com/srednimax/urgent/commit/465942b4124c29a07e0d87247796b09ebbed6b02))
* let repeated calls ring for everyone in your contacts ([804bfc4](https://github.com/srednimax/urgent/commit/804bfc49347d589f14a0b78e4e8a7f0f312a3342))
* let repeated calls ring only from chosen callers ([edc3d57](https://github.com/srednimax/urgent/commit/edc3d5779e4326b49e77f091c5ac3433fe55a5eb))
* restart the keep-alive after a reboot or an update ([a742723](https://github.com/srednimax/urgent/commit/a742723f3f0e958326f5c576196c3cfc5ea87834))


### Bug Fixes

* contact scope chips show the effective choice and survive recreation ([7a816cf](https://github.com/srednimax/urgent/commit/7a816cf9bb247b5d5867902d1d70796d9e4ff7f9))
* expect the keep-alive's special-use service in the artifact check ([cdb951e](https://github.com/srednimax/urgent/commit/cdb951eac4488cbf75af91df9fefcf06885e49eb))
* keep the boot restart receiver unexported ([d99aa13](https://github.com/srednimax/urgent/commit/d99aa1360d0f9391b0a54585a4bf58326d285a92))
* keep the keep-alive service from crashing and stacking activities ([b86d45d](https://github.com/srednimax/urgent/commit/b86d45dd31a00d10418422d6910905ab761b387c))
* match both the written and the normalised number from contacts ([5f97c78](https://github.com/srednimax/urgent/commit/5f97c78d2fe69df44cab5b65e76c6012d31f2dc2))
* pick callers by E.164 and apply the digit minimum to both paths ([71e2c82](https://github.com/srednimax/urgent/commit/71e2c821d021e088ace7ff246ecc5dadbedc063a))
* read contacts only for a repeated call, and only with contacts on ([2aa11ae](https://github.com/srednimax/urgent/commit/2aa11ae66125524cacfe393ad602846ee590d2cc))
* read contacts only for repeated calls; respect DND's contact rules ([7511b92](https://github.com/srednimax/urgent/commit/7511b92cda3f9046bed06e80e842a7731a9fd7d8))
* remove all of a contact's numbers together ([df7f2e1](https://github.com/srednimax/urgent/commit/df7f2e1c5533797a3d2baa180248f6b6f4ad0180))
* run the keep-alive only while Ring for repeated calls is on ([7e0ce99](https://github.com/srednimax/urgent/commit/7e0ce99a8173930420b2db8cdbb1f6f222b6f133))
* say one entry per picked number, and show one form per row ([801bc91](https://github.com/srednimax/urgent/commit/801bc914c4e9e333935db77c9947cf4433ce2c47))
* scan every contact row for starred, and catch provider errors ([8c4589f](https://github.com/srednimax/urgent/commit/8c4589f1f29db8563faed4f4b8cecf9e2dd4e85c))
* show the contacts banner only while Urgent is on ([72d1599](https://github.com/srednimax/urgent/commit/72d159915609ec72de76509cd38ef961033d2f0a))

## [0.2.0](https://github.com/srednimax/urgent/compare/v0.1.0...v0.2.0) (2026-10-13)


### Features

* let declined calls stop counting as the first call ([25811da](https://github.com/srednimax/urgent/commit/25811dac03bfb39569a53399f428ba2503d720be))
* ring through silent mode when the same number calls twice ([576c90b](https://github.com/srednimax/urgent/commit/576c90b0c8c3962388fd13cd9e8ba0c9aeff219f))


### Bug Fixes

* count DND's repeat callers only within its 15-minute threshold ([af0d608](https://github.com/srednimax/urgent/commit/af0d608d73240c30a0539d35f5fb35b03d36230e))
* decide ring-through from the DND policy, alarms muting and calls in progress ([899be26](https://github.com/srednimax/urgent/commit/899be265538dd28ea895ec90d9c12a43b82e6ef4))
* drop the gendered verb from the Polish feature-suggestion hint ([8bb5d06](https://github.com/srednimax/urgent/commit/8bb5d06a784b648b6d9a7196655d950d69611eb2))
* let the newest call decide when declined calls do not count ([dcd1b5a](https://github.com/srednimax/urgent/commit/dcd1b5ab8eee49bab9c683b91a5e0a2edf8f9d6f))
* send support mail to urgentapp.support@gmail.com ([b36f457](https://github.com/srednimax/urgent/commit/b36f45734276418d884cec201a4d8d4860218707))

## Changelog

Nothing released yet.
