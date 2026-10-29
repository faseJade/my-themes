package com.fasejade.mythemes;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Insets;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.view.WindowInsets;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TreeSet;

// The whole UI is assets/index.html; this class only hosts it and does the jobs a web page cannot.
public class MainActivity extends Activity {
    private WebView web;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        web = new WebView(this);
        web.setBackgroundColor(0xFF080604);
        web.getSettings().setJavaScriptEnabled(true);
        web.addJavascriptInterface(new Bridge(), "App");
        // Android 15 draws apps under the status and navigation bars; keep the page clear of them.
        web.setOnApplyWindowInsetsListener((v, in) -> {
            Insets i = in.getInsets(WindowInsets.Type.systemBars());
            v.setPadding(i.left, i.top, i.right, i.bottom);
            return WindowInsets.CONSUMED;
        });
        setContentView(web);
        if (state == null) web.loadUrl("file:///android_asset/index.html");
        else web.restoreState(state);
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        web.saveState(out);
    }

    @Override
    public void onBackPressed() {
        if (web.canGoBack()) web.goBack();
        else super.onBackPressed();
    }

    // Theme ids come from our own page, but they become asset paths, so only allow plain ids.
    private static String themeDir(String id) {
        if (id == null || !id.matches("[a-z0-9-]+")) throw new IllegalArgumentException("bad theme id");
        return "themes/" + id;
    }

    private class Bridge {
        @JavascriptInterface
        public String setWallpaper(String id, String where) {
            int flags = "home".equals(where) ? WallpaperManager.FLAG_SYSTEM
                    : "lock".equals(where) ? WallpaperManager.FLAG_LOCK
                    : WallpaperManager.FLAG_SYSTEM | WallpaperManager.FLAG_LOCK;
            try (InputStream in = getAssets().open(themeDir(id) + "/wallpaper.png")) {
                WallpaperManager.getInstance(MainActivity.this).setStream(in, null, true, flags);
                return "Wallpaper set";
            } catch (Exception e) {
                return "Could not set the wallpaper: " + e.getMessage();
            }
        }

        // Saves the given icons ("drawable|App name" per line) to Pictures/<folder> for Theme Park. No permission needed.
        @JavascriptInterface
        public String saveIcons(String icons, String folder) {
            try {
                String[] lines = icons.split("\n");
                for (String line : lines) {
                    String[] p = line.split("\\|", 2);
                    int id = getResources().getIdentifier(p[0], "drawable", getPackageName());
                    if (id == 0) throw new IllegalArgumentException("no icon " + p[0]);
                    ContentValues v = new ContentValues();
                    v.put(MediaStore.Images.Media.DISPLAY_NAME, p[1] + ".png");
                    v.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                    v.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/" + folder);
                    Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, v);
                    try (InputStream in = getResources().openRawResource(id);
                         OutputStream out = getContentResolver().openOutputStream(uri)) {
                        in.transferTo(out);
                    }
                }
                return lines.length + " icons saved to Pictures/" + folder;
            } catch (Exception e) {
                return "Could not save the icons: " + e.getMessage();
            }
        }

        // Samsung lets only Theme Park apply icons and system colours, so the best an app can do is open it.
        @JavascriptInterface
        public String openThemePark() {
            String pkg = "com.samsung.android.themedesigner";
            Intent i = getPackageManager().getLaunchIntentForPackage(pkg);
            if (i == null) i = new Intent(Intent.ACTION_VIEW, Uri.parse("samsungapps://ProductDetail/" + pkg));
            try {
                startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                return "ok";
            } catch (Exception e) {
                return "Theme Park is not installed. Get Good Lock from Galaxy Store first.";
            }
        }

        // Copies "Label=ComponentInfo{package/activity}" for every app in the drawer, so icons can be linked exactly.
        @JavascriptInterface
        public String copyAppList() {
            Intent i = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
            TreeSet<String> lines = new TreeSet<>();
            for (ResolveInfo r : getPackageManager().queryIntentActivities(i, 0))
                lines.add(r.loadLabel(getPackageManager()) + "=ComponentInfo{" + r.activityInfo.packageName + "/" + r.activityInfo.name + "}");
            copy(String.join("\n", lines));
            return lines.size() + " apps copied. Paste them to Claude.";
        }

        @JavascriptInterface
        public String copy(String text) {
            ((ClipboardManager) getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("colour", text));
            return "Copied " + text;
        }
    }
}
