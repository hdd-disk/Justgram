# Justgram
Just anоther interesting FOSS Telegram client.

[![Release Build](https://github.com/hdd-disk/Justgram/actions/workflows/release.yml/badge.svg)](https://github.com/hdd-disk/Justgram/actions/workflows/release.yml) [![Channel](https://img.shields.io/badge/Channel-Telegram-blue.svg)](https://t.me/Justgram_client)

### API, Protocol documentation

Telegram API manuals: https://core.telegram.org/api

MTproto protocol manuals: https://core.telegram.org/mtproto

### Building

> [!CAUTION]
>
> **Building on Windows is not supported.**


#### 1. Install ninja-build, gperf, meson, libuv1-dev, nasm, cmake, autoconf, automake, libtool, pkg-config, git

#### 2. Clone repository:
```
git clone --recursive --shallow-submodules https://github.com/hdd-disk/Justgram
```
#### 3. Setup Signing Credentials

1) Place your `release.keystore` file into the `TMessagesProj/config` directory. 

2) Open your `local.properties` file in the root directory and add the following properties: 
```
RELEASE_KEY_ALIAS=your_key_alias 
RELEASE_STORE_PASSWORD=your_keystore_password 
RELEASE_KEY_PASSWORD=your_key_password
```


#### 4. Build
Build with Android Studio or from the command line:
``` 
./gradlew assembleBundleRelease # arm64-v8a and armabi-v7a
./gradlew assembleArm64-v8aRelease # arm64-v8a
./gradlew assembleArmeabi-v7aRelease # armeabi-v7a
```

Native libraries (FFmpeg, BoringSSL, libvpx, dav1d, tde2e) are built from source automatically on the first build and cached for subsequent runs.

### Credits:
- [Mercurygram](https://github.com/Mercurygram/Mercurygram)
- [Forkgram](https://github.com/forkgram/TelegramAndroid)
- [Nekogram](https://github.com/Nekogram/Nekogram)
- [Telegram Monet](https://github.com/mi-g-alex/Telegram-Monet)
- [Nagram XF](https://github.com/Keeperorowner/NagramXF)
- [Cherrygram](https://github.com/arsLan4k1390/Cherrygram)

