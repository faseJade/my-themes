# My Themes

A tiny Android app for your own phone themes, with no ads and no theme store. It sets the wallpaper,
works as an icon pack, and has its own home screen (launcher), so every icon follows your theme.

The whole app is about 200 KB. The first theme, **Amber Pocket**, is amber outlines on near-black:
49 icons that weigh 82 KB together, plus a 52 KB wallpaper.

## What it can and can't do

| Part | Status |
|---|---|
| Wallpaper (home, lock or both) | One tap from the app |
| App icons | On the My Themes home screen, or any launcher that takes icon packs (Lawnchair, Nova) |
| Samsung's own home screen (One UI Home) | Only through Samsung's Theme Park, which isn't offered in every country |
| Quick settings and system screens | Not possible: Android only lets the phone maker's signed apps restyle these |

## Build

You need Linux or WSL, Docker, Python 3, Java 17 or newer, and the Android SDK
(`platforms;android-35`, `build-tools;35.0.0`) in `$ANDROID_HOME` (default `~/Android`). There's no Gradle.

```sh
./app/build.sh        # draws the theme, packs it, builds and signs app/build/MyThemes.apk
```

The first build makes a signing key in `keys/`. Back that folder up: Android only installs an update
over the old app if both were signed with the same key.

## Add a theme

1. Add `themes/<id>.json` with a name, a one-line description and its colours.
2. Make its wallpaper and icons (for now `src/make_wallpaper.py` and `src/make_icons.py` draw Amber Pocket).
3. Link icons to apps in `src/components.txt`. The app's "Copy my app list" button gives you the exact lines.

## Credits

- Icon shapes: [Lucide](https://lucide.dev) (ISC licence, `vendor/lucide/LICENSE`)
- Heading font: Chakra Petch (SIL Open Font License, `app/assets/fonts/OFL.txt`)

Licensed under MIT. See `LICENSE`.
