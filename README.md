# Sony Bravia KD-32W830K Wi-Fi Remote Control (Android)

A modern, high-performance Android remote control app for **Sony Bravia TVs** (specifically optimized for model **KD-32W830K** and Google TV / Android TV models).

Because modern Android smartphones do not have built-in Infrared (IR) blasters, this application connects directly over your local **Wi-Fi network** using Sony's native **Bravia IP Control (IRCC-over-HTTP)** protocol.

---

## 🌟 Key Features

- **Zero-Latency Wi-Fi Control**: Communicates directly with the TV over local HTTP/SOAP. No cloud servers, no subscriptions, works offline on your home network.
- **Auto-Discovery**: Automatically finds your Sony TV on your local Wi-Fi using SSDP (Simple Service Discovery Protocol) and fast local subnet scanning.
- **Complete Remote Interface**:
  - **Power Control**: Turn TV on or off / toggle standby.
  - **D-Pad Directional Cluster**: Up, Down, Left, Right, and central **OK** button.
  - **Volume & Channel Rockers**: Dedicated tactile **VOL +/-**, **CH +/-**, and **Mute**.
  - **System Navigation**: Dedicated **Home**, **Back**, **Options / Action Menu**, and **Input / Source** switcher.
  - **Streaming Shortcuts**: One-tap direct access to **YouTube**, **Netflix**, and **Prime Video**.
  - **Media Controls**: Play, Pause, Rewind, Fast-Forward.
  - **Numeric Keypad**: Modal slide-up 0–9 keypad for entering TV channel numbers or PINs.
- **Haptic Tactile Feedback**: Subtle micro-vibrations on button presses for an authentic physical remote feel.
- **Material 3 Entertainment Dark Theme**: Custom dark UI tailored for low-light living rooms.

---

## 📺 Step 1: Enable IP Control on your Sony TV (1-Minute Setup)

Before using the remote, you must enable **IP Control** on your Sony KD-32W830K:

1. Turn on your Sony TV.
2. Ensure both your **Android phone and Sony TV are connected to the SAME Wi-Fi network**.
3. On your Sony TV remote, press the **Settings** (Gear icon ⚙️) button.
4. Navigate to **Network & Internet** (or **Network**).
5. Select **Home network** or **Remote device settings** -> **IP control**.
6. Set **Authentication** to **Pre-Shared Key (PSK)**.
7. Enter a simple PIN, such as `1234` or `0000` (the app default is `1234`).
8. Set **Simple IP control** to **ON**.
9. Check your TV's IP address by going to **Settings** -> **Network & Internet** -> **Network status** (e.g., `192.168.1.50`).

---

## 🛠️ Step 2: Build the APK

### Option A: Using Android Studio (Recommended)
1. Open **Android Studio**.
2. Click **File -> Open...** and select this project folder:
   ```
   c:\Users\Abhishek Yadav\OneDrive\Desktop\remote
   ```
3. Let Gradle sync the project (takes ~1-2 minutes).
4. From the top menu, click:
   **Build -> Build Bundle(s) / APK(s) -> Build APK(s)**
5. Once completed, a popup will say **"APK(s) generated successfully"**. Click **locate** to find `app-debug.apk`.

### Option B: Build via Command Line / Terminal
In this project directory, run:
```powershell
.\gradlew.bat assembleDebug
```
The output APK file will be located at:
```
app\build\outputs\apk\debug\app-debug.apk
```

---

## 📱 Step 3: Install the APK on Any Android Phone

1. Transfer `app-debug.apk` to your phone using:
   - USB cable
   - Google Drive / WhatsApp / Telegram
   - Quick Share / Nearby Share
2. Tap the file on your phone and choose **Install**.
   *(If prompted, allow "Install from Unknown Sources" for your file manager or browser).*
3. Open **Sony Bravia Remote**.
4. Tap **Scan for Sony TVs Automatically** or enter your TV's IP and PSK (e.g., `1234`).
5. Enjoy full control of your Sony TV right from your phone!

---

## 📡 Technical Details & Protocol

- **Protocol**: Sony Bravia IRCC-IP (SOAP 1.1 XML over HTTP POST)
- **Endpoint**: `http://<TV_IP>/sony/ircc`
- **Header**: `X-Auth-PSK: <PreSharedKey>`
- **Port**: 80 (HTTP) & 20060 (Simple IP Control)
- **Discovery**: SSDP UPnP Multicast to `239.255.255.250:1900` + Subnet Sweep
- **Compatibility**: Android 7.0 (API 24) through Android 14+ (API 34)
