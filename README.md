# UniRemote for Android TV

A private, modern phone remote for Android TV and Google TV with a sleek Netflix Red dark theme.

![UniRemote in Simple remote mode with Netflix Red theme](app/fastlane/metadata/android/en-US/images/phoneScreenshots/1.png)

UniRemote pairs with your TV seamlessly using Google's pairing protocol, with no accounts, no ads, no analytics, and no tracking. It communicates strictly with your TV over your local Wi-Fi network.

- **Four ways to steer:**
  - **Simple Remote:** Clean, distraction-free remote with dedicated volume controls, directional navigation, Back, Home, Keyboard, and a quick YouTube button.
  - **D-pad:** Focused navigation with OK, direction keys, and media controls.
  - **Touchpad:** Swipe to navigate smoothly, tap to select, hold for options.
  - **Full Remote:** Comprehensive control panel with extra keys for search, captions, info, TV settings, and input.
- **Typing:** When a text field opens on the TV, the phone keyboard opens automatically and text appears as you type.
- **Quick Volume Controls:** Dedicated volume up, volume down, and mute buttons.
- **Playback & Navigation:** Power, Play/Pause, Rewind, Fast Forward, Back, and Home.
- **App Shortcuts:** Quick-access shortcuts for your favorite TV apps like YouTube, Netflix, Prime Video, Disney+, and Spotify.
- **Modern UI:** Built-in Netflix Red color palette, edge-to-edge layout, and native haptic feedback.

UniRemote requires Android 8.0 or later and works with any TV running Google's Android TV Remote Service (including Chromecast with Google TV, Sony Bravia, Xiaomi, TCL, Shield TV, and certified Android TVs).

---

## Download & Install

📥 **[Download Latest UniRemote APK (v1.0.0)](release/uniremote.apk?raw=true)**

1. Download [`release/uniremote.apk`](release/uniremote.apk?raw=true) directly to your phone.
2. Open the downloaded file from your file manager or browser downloads.
3. If Android prompts to allow installation from unknown sources, tap **Settings → Allow**.
4. If Play Protect displays *"App blocked to protect your device"*, choose **More details → Install anyway**.
5. Open **UniRemote**: your TV will appear under **Nearby**. Tap it and type the pairing code shown on your TV screen.

---

## Privacy

- **No Tracking:** No accounts, no analytics, no ads, and no external tracking servers.
- **Local Network Only:** UniRemote communicates directly with your TV on your local Wi-Fi network.
- **Device Privacy:** UniRemote does not transmit your phone's personal identifiers to the TV.
- **Encrypted Keys:** Pairing keys remain on your phone, securely encrypted via Android Keystore.

---

## For Developers

### Build from Source

To compile the release APK:

```sh
./gradlew assembleStandardRelease
```

The compiled release APK will be generated at:
`app/build/outputs/apk/standard/release/app-standard-release.apk`

### Testing

```sh
./gradlew :protocol:test lintStandardDebug
```

---

## License

UniRemote is free software under the [GNU General Public License v3.0 only](LICENSE) (`GPL-3.0-only`). The Material Symbols icons are listed in [NOTICE.md](NOTICE.md).

Android TV and Google TV are trademarks of Google LLC. UniRemote is independently developed to work with Android TV and Google TV devices.
