package com.fasejade.mythemes;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Insets;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// The home screen, laid out like One UI Home: sideways home pages, a dock, a paged app drawer with folders
// and a search pill. Everything uses the current theme's icons.
public class HomeActivity extends Activity {
    private static final int TEXT = 0xFFFFFFFF, SUB = 0xFFFFD2A6, SHEET = 0xF2231A14;
    private static final int DRAWER_COLS = 4, DRAWER_ROWS = 6, FOLDER_COLS = 3;
    private float dp;
    private Store store;
    private Icons icons;
    private final Map<String, String> labels = new HashMap<>();
    private FrameLayout root, drawer, folderView;
    private Pager homePager, drawerPager;
    private Dots homeDots, drawerDots;
    private CellGrid dockGrid;
    private EditText search;
    private ScrollView results;
    private PopupWindow menu;
    private Insets insets = Insets.NONE;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        dp = getResources().getDisplayMetrics().density;
        store = new Store(this);
        icons = new Icons(this, "amber-pocket");
        root = new FrameLayout(this);
        root.setOnApplyWindowInsetsListener((v, in) -> {
            // Keyboard included, so the drawer's search pill rides above it like One UI's.
            insets = in.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.ime());
            for (int i = 0; i < root.getChildCount(); i++) pad(root.getChildAt(i));
            return WindowInsets.CONSUMED;
        });
        buildHome();
        buildDrawer();
        setContentView(root);
    }

    @Override protected void onResume() {
        super.onResume();
        refresh();
    }

    // Home pressed while already home: close whatever is open and go to the first page, like One UI.
    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        boolean wasHome = hasWindowFocus() && drawer.getVisibility() != View.VISIBLE && folderView == null;
        closeAll();
        if (wasHome) homePager.snapTo(0);
    }

    // While only being tried out (not the default home), Back with nothing open leaves.
    @Override public void onBackPressed() {
        boolean open = menu != null && menu.isShowing() || folderView != null || drawer.getVisibility() == View.VISIBLE;
        if (open) closeAll();
        else if (!isDefaultHome()) finish();
    }

    private boolean isDefaultHome() {
        ResolveInfo r = getPackageManager().resolveActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), 0);
        return r != null && getPackageName().equals(r.activityInfo.packageName);
    }

    // ---- data ----

    private void refresh() {
        labels.clear();
        Intent i = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        for (ResolveInfo r : getPackageManager().queryIntentActivities(i, 0)) {
            if (r.activityInfo.packageName.equals(getPackageName()) && r.activityInfo.name.endsWith("HomeActivity")) continue;
            labels.put(r.activityInfo.packageName + "/" + r.activityInfo.name, r.loadLabel(getPackageManager()).toString());
        }
        store.load(labels);
        fillHome();
        fillDrawer();
    }

    // ---- home ----

    private void buildHome() {
        LinearLayout home = new LinearLayout(this);
        home.setOrientation(LinearLayout.VERTICAL);
        homePager = new Pager(this);
        homePager.listener = new Pager.Listener() {
            public void onPage(int p) { homeDots.set(homePager.getChildCount(), p); }
            public void onSwipeUp() { openDrawer(); }
            public void onSwipeDown() { expandNotifications(); }
        };
        homeDots = new Dots(this, Icons.AMBER);
        dockGrid = new CellGrid(this, Store.DOCK_SLOTS, 1);
        home.addView(homePager, new LinearLayout.LayoutParams(-1, 0, 1));
        home.addView(homeDots, new LinearLayout.LayoutParams(-1, -2));
        LinearLayout.LayoutParams dl = new LinearLayout.LayoutParams(-1, (int) (96 * dp));
        dl.setMargins((int) (8 * dp), (int) (6 * dp), (int) (8 * dp), (int) (8 * dp));
        home.addView(dockGrid, dl);
        root.addView(home, new FrameLayout.LayoutParams(-1, -1));
    }

    private void fillHome() {
        homePager.removeAllViews();
        for (String[] page : store.home) {
            CellGrid g = new CellGrid(this, 4, 5);
            g.setPadding((int) (8 * dp), (int) (8 * dp), (int) (8 * dp), 0);
            for (int i = 0; i < page.length; i++) g.addView(page[i] == null ? new View(this) : appCell(page[i], true, page, i));
            homePager.addView(g);
        }
        homePager.page = Math.min(homePager.page, store.home.size() - 1);
        homeDots.set(store.home.size(), homePager.page);
        dockGrid.removeAllViews();
        for (int i = 0; i < store.dock.length; i++) dockGrid.addView(store.dock[i] == null ? new View(this) : appCell(store.dock[i], false, store.dock, i));
    }

    // ---- drawer ----

    private void buildDrawer() {
        drawer = new FrameLayout(this);
        drawer.setBackgroundColor(0xF2080604);
        drawer.setVisibility(View.GONE);
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);

        TextView more = text("⋮", 24, TEXT);
        more.setGravity(Gravity.CENTER);
        more.setContentDescription("My Themes settings");
        more.setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.END);
        top.addView(more, new LinearLayout.LayoutParams((int) (56 * dp), (int) (56 * dp)));
        col.addView(top);

        FrameLayout middle = new FrameLayout(this);
        drawerPager = new Pager(this);
        drawerPager.listener = new Pager.Listener() {
            public void onPage(int p) { drawerDots.set(drawerPager.getChildCount(), p); }
            public void onSwipeUp() { }
            public void onSwipeDown() { closeDrawer(); }
        };
        middle.addView(drawerPager, new FrameLayout.LayoutParams(-1, -1));
        results = new ScrollView(this);
        results.setVisibility(View.GONE);
        middle.addView(results, new FrameLayout.LayoutParams(-1, -1));
        col.addView(middle, new LinearLayout.LayoutParams(-1, 0, 1));

        drawerDots = new Dots(this, Icons.AMBER);
        col.addView(drawerDots, new LinearLayout.LayoutParams(-1, -2));

        search = new EditText(this);
        search.setHint("Search");
        search.setHintTextColor(0xB3FFFFFF);
        search.setTextColor(TEXT);
        search.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        search.setSingleLine();
        search.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        search.setBackground(round(0x33FFFFFF, 26));
        search.setPadding((int) (22 * dp), 0, (int) (22 * dp), 0);
        search.setCompoundDrawablesRelativeWithIntrinsicBounds(android.R.drawable.ic_menu_search, 0, 0, 0);
        search.setCompoundDrawablePadding((int) (10 * dp));
        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            public void onTextChanged(CharSequence s, int a, int b, int c) { }
            public void afterTextChanged(Editable s) { showResults(s.toString().trim()); }
        });
        LinearLayout.LayoutParams sl = new LinearLayout.LayoutParams(-1, (int) (52 * dp));
        sl.setMargins((int) (28 * dp), (int) (12 * dp), (int) (28 * dp), (int) (16 * dp));
        col.addView(search, sl);

        drawer.addView(col, new FrameLayout.LayoutParams(-1, -1));
        root.addView(drawer, new FrameLayout.LayoutParams(-1, -1));
    }

    private void fillDrawer() {
        drawerPager.removeAllViews();
        int per = DRAWER_COLS * DRAWER_ROWS;
        for (int start = 0; start < store.drawer.size(); start += per) {
            CellGrid g = new CellGrid(this, DRAWER_COLS, DRAWER_ROWS);
            g.setPadding((int) (8 * dp), 0, (int) (8 * dp), 0);
            for (int i = start; i < Math.min(start + per, store.drawer.size()); i++) {
                Store.Item it = store.drawer.get(i);
                g.addView(it.app != null ? appCell(it.app, true, null, -1) : folderCell(it));
            }
            drawerPager.addView(g);
        }
        drawerPager.page = Math.min(drawerPager.page, Math.max(0, drawerPager.getChildCount() - 1));
        drawerDots.set(drawerPager.getChildCount(), drawerPager.page);
        if (search.length() > 0) showResults(search.getText().toString().trim());
    }

    private void showResults(String q) {
        boolean on = !q.isEmpty();
        results.setVisibility(on ? View.VISIBLE : View.GONE);
        drawerPager.setVisibility(on ? View.INVISIBLE : View.VISIBLE);
        drawerDots.setVisibility(on ? View.INVISIBLE : View.VISIBLE);
        if (!on) return;
        List<String> hits = new ArrayList<>();
        for (Map.Entry<String, String> e : labels.entrySet()) if (e.getValue().toLowerCase().contains(q.toLowerCase())) hits.add(e.getKey());
        hits.sort((a, b) -> labels.get(a).compareToIgnoreCase(labels.get(b)));
        results.removeAllViews();
        if (hits.isEmpty()) {
            TextView none = text("No apps found", 15, SUB);
            none.setGravity(Gravity.CENTER);
            none.setPadding(0, (int) (48 * dp), 0, 0);
            results.addView(none);
            return;
        }
        CellGrid g = new CellGrid(this, DRAWER_COLS, (hits.size() + DRAWER_COLS - 1) / DRAWER_COLS);
        g.setPadding((int) (8 * dp), 0, (int) (8 * dp), 0);
        for (String c : hits) g.addView(appCell(c, true, null, -1));
        results.addView(g);
    }

    private void openDrawer() {
        if (drawer.getVisibility() == View.VISIBLE) return;
        drawer.setVisibility(View.VISIBLE);
        drawer.setAlpha(0); drawer.setTranslationY(root.getHeight() * .12f);
        drawer.animate().alpha(1).translationY(0).setDuration(220).start();
    }

    private void closeDrawer() {
        if (drawer.getVisibility() != View.VISIBLE) return;
        hideKeyboard();
        drawer.animate().alpha(0).translationY(root.getHeight() * .12f).setDuration(180).withEndAction(() -> {
            drawer.setVisibility(View.GONE);
            search.setText("");
            drawerPager.page = 0; drawerPager.scrollTo(0, 0); drawerDots.set(drawerPager.getChildCount(), 0);
        }).start();
    }

    // ---- folders ----

    private View folderCell(Store.Item it) {
        View v = cell(icons.folder(it.apps, labels), it.name, true);
        v.setOnClickListener(x -> openFolder(it));
        return v;
    }

    private void openFolder(Store.Item it) {
        closeFolder();
        folderView = new FrameLayout(this);
        folderView.setBackgroundColor(0xB3000000);
        folderView.setOnClickListener(v -> closeFolder());
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setGravity(Gravity.CENTER_HORIZONTAL);
        TextView title = text(it.name, 32, TEXT);
        title.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        title.setPadding(0, (int) (120 * dp), 0, (int) (28 * dp));
        col.addView(title);
        CellGrid g = new CellGrid(this, FOLDER_COLS, (it.apps.size() + FOLDER_COLS - 1) / FOLDER_COLS);
        g.setPadding((int) (10 * dp), (int) (16 * dp), (int) (10 * dp), (int) (8 * dp));
        g.setBackground(round(SHEET, 34));
        g.setClickable(true); // taps on the panel's gaps must not close the folder
        for (String c : it.apps) g.addView(appCell(c, true, null, -1));
        LinearLayout.LayoutParams gl = new LinearLayout.LayoutParams(-1, -2);
        gl.setMargins((int) (28 * dp), 0, (int) (28 * dp), 0);
        col.addView(g, gl);
        folderView.addView(col, new FrameLayout.LayoutParams(-1, -1));
        pad(folderView);
        root.addView(folderView, new FrameLayout.LayoutParams(-1, -1));
        g.setScaleX(.92f); g.setScaleY(.92f); folderView.setAlpha(0);
        folderView.animate().alpha(1).setDuration(160).start();
        g.animate().scaleX(1).scaleY(1).setDuration(200).start();
    }

    private void closeFolder() {
        if (folderView == null) return;
        View v = folderView;
        folderView = null;
        v.animate().alpha(0).setDuration(140).withEndAction(() -> root.removeView(v)).start();
    }

    private void closeAll() {
        if (menu != null) menu.dismiss();
        if (folderView != null) closeFolder(); else closeDrawer();
    }

    // ---- cells ----

    // slots/index say where the app sits on home or the dock (null when it is in the drawer or a folder).
    private View appCell(String comp, boolean label, String[] slots, int index) {
        String name = labels.getOrDefault(comp, "");
        View v = cell(icons.app(comp, name), name, label);
        v.setOnClickListener(x -> launch(comp, x.findViewWithTag("icon")));
        v.setOnLongClickListener(x -> { showMenu(x, comp, slots, index); return true; });
        return v;
    }

    private View cell(android.graphics.drawable.Drawable d, String name, boolean label) {
        LinearLayout v = new LinearLayout(this);
        v.setOrientation(LinearLayout.VERTICAL);
        v.setGravity(Gravity.CENTER);
        v.setContentDescription(name);
        ImageView iv = new ImageView(this);
        iv.setTag("icon");
        iv.setImageDrawable(d);
        int s = (int) (60 * dp);
        v.addView(iv, new LinearLayout.LayoutParams(s, s));
        if (label) {
            TextView t = text(name, 12.5f, TEXT);
            t.setSingleLine(); t.setEllipsize(TextUtils.TruncateAt.END);
            t.setGravity(Gravity.CENTER);
            t.setShadowLayer(4 * dp, 0, dp, 0xCC000000);
            t.setPadding((int) (2 * dp), (int) (5 * dp), (int) (2 * dp), 0);
            v.addView(t, new LinearLayout.LayoutParams(-1, -2));
        }
        return v;
    }

    private void launch(String comp, View from) {
        ComponentName c = ComponentName.unflattenFromString(comp);
        if (c == null) return;
        Intent i = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setComponent(c)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        try {
            // The app grows out of its icon, like One UI.
            startActivity(i, ActivityOptions.makeScaleUpAnimation(from, 0, 0, from.getWidth(), from.getHeight()).toBundle());
        } catch (Exception e) {
            Toast.makeText(this, "Could not open the app", Toast.LENGTH_SHORT).show();
        }
    }

    // ---- hold menu ----

    private void showMenu(View anchor, String comp, String[] slots, int index) {
        if (menu != null) menu.dismiss();
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackground(round(SHEET, 22));
        box.setPadding(0, (int) (6 * dp), 0, (int) (6 * dp));
        String pkg = comp.split("/")[0];
        if (slots == null) {
            item(box, "Add to Home", () -> { store.addToHome(comp); fillHome(); Toast.makeText(this, "Added to Home", Toast.LENGTH_SHORT).show(); });
            item(box, "Add to dock", () -> { if (store.addToDock(comp)) fillHome(); else Toast.makeText(this, "The dock is full. Remove an app from it first.", Toast.LENGTH_LONG).show(); });
        } else {
            item(box, slots == store.dock ? "Remove from dock" : "Remove from Home", () -> { store.remove(slots, index); fillHome(); });
        }
        item(box, "App info", () -> startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + pkg))));
        item(box, "Uninstall", () -> startActivity(new Intent(Intent.ACTION_DELETE, Uri.parse("package:" + pkg))));
        menu = new PopupWindow(box, (int) (210 * dp), ViewGroup.LayoutParams.WRAP_CONTENT, true);
        menu.setElevation(12 * dp);
        box.measure(View.MeasureSpec.makeMeasureSpec((int) (210 * dp), View.MeasureSpec.EXACTLY), View.MeasureSpec.UNSPECIFIED);
        int[] at = new int[2];
        anchor.getLocationInWindow(at);
        int x = Math.max((int) (8 * dp), Math.min(root.getWidth() - (int) (218 * dp), at[0] + anchor.getWidth() / 2 - (int) (105 * dp)));
        int y = at[1] - box.getMeasuredHeight() - (int) (4 * dp);
        if (y < insets.top) y = at[1] + anchor.getHeight();
        menu.showAtLocation(root, Gravity.NO_GRAVITY, x, y);
    }

    private void item(LinearLayout box, String label, Runnable action) {
        TextView t = text(label, 16, TEXT);
        t.setGravity(Gravity.CENTER_VERTICAL);
        t.setPadding((int) (20 * dp), 0, (int) (20 * dp), 0);
        t.setOnClickListener(v -> { menu.dismiss(); action.run(); });
        box.addView(t, new LinearLayout.LayoutParams(-1, (int) (48 * dp)));
    }

    // ---- helpers ----

    // Opens the notification panel. Hidden API that launchers commonly use; does nothing if the phone refuses it.
    private void expandNotifications() {
        try {
            Object sb = getSystemService("statusbar");
            Class.forName("android.app.StatusBarManager").getMethod("expandNotificationsPanel").invoke(sb);
        } catch (Exception ignored) { }
    }

    private void hideKeyboard() {
        ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(search.getWindowToken(), 0);
        search.clearFocus();
    }

    private void pad(View v) { v.setPadding(insets.left, insets.top, insets.right, insets.bottom); }

    private TextView text(String s, float sp, int color) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        t.setTextColor(color);
        return t;
    }

    private GradientDrawable round(int color, float radiusDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(radiusDp * dp);
        return g;
    }
}
