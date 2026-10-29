package com.fasejade.mythemes;

import android.content.Intent;
import android.os.Bundle;

// The launcher: the same WebView host as the dashboard, showing home.html over the phone's wallpaper.
public class HomeActivity extends MainActivity {
    @Override
    protected String page() { return "home.html"; }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        web.setBackgroundColor(0);
        web.setLongClickable(false);
        web.setOnLongClickListener(v -> true); // our page handles long-press; stop text selection
    }

    @Override
    protected void onResume() {
        super.onResume();
        web.evaluateJavascript("window.refresh && refresh()", null); // apps may have been installed or removed
    }

    // Pressing Home while already home closes the drawer, like other launchers.
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        web.evaluateJavascript("window.goHome && goHome()", null);
    }

    @Override
    public void onBackPressed() {
        web.evaluateJavascript("window.goHome && goHome()", null);
    }
}
