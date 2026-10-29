#!/bin/sh
# Builds app/build/MyThemes.apk with the Android SDK tools directly (no Gradle), which keeps the APK tiny.
set -e
cd "$(dirname "$0")/.."
SDK=${ANDROID_HOME:-$HOME/Android}; BT=$SDK/build-tools/35.0.0; JAR=$SDK/platforms/android-35/android.jar
KS=keys/mythemes.jks
# The key and its password live in keys/ (never committed). A fresh clone makes its own key on first build.
[ -f keys/password ] || { mkdir -p keys; head -c 18 /dev/urandom | base64 > keys/password; }
PASS=$(cat keys/password)
# Draw the wallpaper and icons in a container (rsvg, pngquant, oxipng), then pack them into the app.
python3 -I src/make_wallpaper.py && python3 -I src/make_icons.py
docker build -q -t my-themes-render . >/dev/null
docker run --rm -u "$(id -u):$(id -g)" -v "$PWD":/w my-themes-render ./render.sh >/dev/null 2>&1
python3 -I src/make_app_assets.py
cd app && rm -rf build && mkdir -p build/classes build/gen
"$BT/aapt2" compile --no-crunch --dir res -o build/res.zip  # no-crunch keeps our already-squeezed PNGs
"$BT/aapt2" link -o build/unsigned.apk -I "$JAR" --manifest AndroidManifest.xml -A assets --java build/gen build/res.zip
javac -nowarn -source 17 -target 17 -cp "$JAR" -d build/classes $(find src build/gen -name '*.java') 2>&1 | grep -v "bootstrap classpath\|^1 warning" || true
"$BT/d8" --release --lib "$JAR" --min-api 30 --output build $(find build/classes -name '*.class')
python3 -I -c "import zipfile; zipfile.ZipFile('build/unsigned.apk','a',zipfile.ZIP_DEFLATED).write('build/classes.dex','classes.dex')"
"$BT/zipalign" -f -p 4 build/unsigned.apk build/aligned.apk
cd ..
# One key for every version: Android refuses updates signed with a different key, so never lose keys/.
[ -f $KS ] || { mkdir -p keys; keytool -genkeypair -keystore $KS -storepass "$PASS" -keypass "$PASS" -alias mythemes -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=My Themes" >/dev/null 2>&1; }
"$BT/apksigner" sign --ks $KS --ks-pass "pass:$PASS" --out app/build/MyThemes.apk app/build/aligned.apk
"$BT/apksigner" verify app/build/MyThemes.apk
ls -l app/build/MyThemes.apk
