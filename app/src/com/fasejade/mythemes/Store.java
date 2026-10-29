package com.fasejade.mythemes;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

// The launcher layout: drawer order with folders, home pages and the dock. Saved as JSON in preferences.
class Store {
    static final int HOME_SLOTS = 20, DOCK_SLOTS = 4;

    // One drawer or folder entry: an app ("package/activity") or a folder.
    static class Item {
        String app, name;
        ArrayList<String> apps;
        static Item app(String c) { Item i = new Item(); i.app = c; return i; }
        static Item folder(String n, ArrayList<String> a) { Item i = new Item(); i.name = n; i.apps = a; return i; }
    }

    final ArrayList<Item> drawer = new ArrayList<>();
    final ArrayList<String[]> home = new ArrayList<>();
    String[] dock = new String[DOCK_SLOTS];
    private final SharedPreferences sp;

    Store(Context c) { sp = c.getSharedPreferences("layout", Context.MODE_PRIVATE); }

    // The owner's One UI drawer at the time of the switch, by package, so a first run looks familiar.
    private static final String[][] FOLDERS = {
        {"Samsung", "com.samsung.ecomm.global.gbr", "com.samsung.android.oneconnect", "com.sec.android.app.voicenote", "com.sec.android.app.myfiles",
            "com.sec.android.app.sbrowser", "com.sec.android.app.shealth", "com.samsung.android.app.watchmanager", "com.samsung.android.spay",
            "com.samsung.android.app.find", "com.rsupport.rs.activity.rsupport.aas2", "com.sec.android.easyMover"},
        {"Google", "com.google.android.googlequicksearchbox", "com.android.chrome", "com.google.android.gm", "com.google.android.apps.maps",
            "com.google.android.youtube", "com.google.android.apps.tachyon", "com.google.android.apps.photos"},
        {"Blote", "com.samsung.android.voc", "com.samsung.sree", "com.samsung.android.game.gamehome", "com.samsung.android.app.contacts", "com.samsung.android.app.spage"},
    };
    private static final String[] DRAWER = {"com.microsoft.skydrive", "com.android.vending", "com.google.android.apps.bard", "com.sec.android.app.samsungapps",
        "com.facebook.katana", "com.instagram.android", "com.sec.android.app.camera", "com.sec.android.gallery3d", "com.sec.android.app.clockpackage",
        "com.android.settings", "com.samsung.android.calendar", "com.sec.android.app.popupcalculator", "com.samsung.android.app.notes",
        "com.samsung.android.dialer", "com.google.android.apps.messaging", "com.whatsapp", "com.truecaller", "com.azure.authenticator",
        "com.google.android.apps.authenticator2", "na.com.green.tap_a_meal_android", "com.discord", "com.supercell.clashroyale", "app.teli",
        "com.vibeshort.visualnovel.android", "com.openai.chatgpt", "com.anthropic.claude"};
    private static final String[] DOCK = {"com.samsung.android.dialer", "com.google.android.apps.messaging", "com.android.chrome", "com.sec.android.app.camera"};
    private static final String[] HOME_ROW = {"com.instagram.android", "com.whatsapp", "com.google.android.youtube", "com.discord"};

    // installed: "package/activity" -> label. Loads the saved layout (or the defaults), drops removed apps, appends new ones.
    void load(Map<String, String> installed) {
        drawer.clear(); home.clear(); dock = new String[DOCK_SLOTS];
        String saved = sp.getString("v1", null);
        try {
            if (saved == null) throw new Exception("first run");
            JSONObject o = new JSONObject(saved);
            JSONArray d = o.getJSONArray("drawer");
            for (int i = 0; i < d.length(); i++) {
                Object x = d.get(i);
                if (x instanceof JSONObject) drawer.add(Item.folder(((JSONObject) x).getString("n"), strings(((JSONObject) x).getJSONArray("a"))));
                else drawer.add(Item.app((String) x));
            }
            JSONArray h = o.getJSONArray("home");
            for (int i = 0; i < h.length(); i++) home.add(slots(h.getJSONArray(i), HOME_SLOTS));
            dock = slots(o.getJSONArray("dock"), DOCK_SLOTS);
        } catch (Exception e) {
            drawer.clear(); home.clear();
            for (String[] f : FOLDERS) {
                ArrayList<String> apps = new ArrayList<>();
                for (int i = 1; i < f.length; i++) { String c = byPkg(installed, f[i]); if (c != null) apps.add(c); }
                drawer.add(Item.folder(f[0], apps));
            }
            for (String p : DRAWER) { String c = byPkg(installed, p); if (c != null) drawer.add(Item.app(c)); }
            String[] page = new String[HOME_SLOTS];
            for (int i = 0; i < HOME_ROW.length; i++) page[HOME_SLOTS - HOME_ROW.length + i] = byPkg(installed, HOME_ROW[i]);
            home.add(page);
            for (int i = 0; i < DOCK.length; i++) dock[i] = byPkg(installed, DOCK[i]);
        }
        reconcile(installed);
    }

    private void reconcile(Map<String, String> installed) {
        Set<String> seen = new HashSet<>();
        for (int i = drawer.size() - 1; i >= 0; i--) {
            Item it = drawer.get(i);
            if (it.app != null) { if (installed.containsKey(it.app)) seen.add(it.app); else drawer.remove(i); }
            else { it.apps.retainAll(installed.keySet()); seen.addAll(it.apps); if (it.apps.isEmpty()) drawer.remove(i); }
        }
        ArrayList<String> fresh = new ArrayList<>();
        for (String c : installed.keySet()) if (!seen.contains(c)) fresh.add(c);
        fresh.sort((a, b) -> installed.get(a).compareToIgnoreCase(installed.get(b)));
        for (String c : fresh) drawer.add(Item.app(c));
        for (String[] page : home) for (int i = 0; i < page.length; i++) if (page[i] != null && !installed.containsKey(page[i])) page[i] = null;
        for (int i = 0; i < dock.length; i++) if (dock[i] != null && !installed.containsKey(dock[i])) dock[i] = null;
        if (home.isEmpty()) home.add(new String[HOME_SLOTS]);
        save();
    }

    void save() {
        try {
            JSONArray d = new JSONArray();
            for (Item it : drawer) d.put(it.app != null ? it.app : new JSONObject().put("n", it.name).put("a", new JSONArray(it.apps)));
            JSONArray h = new JSONArray();
            for (String[] page : home) h.put(json(page));
            sp.edit().putString("v1", new JSONObject().put("drawer", d).put("home", h).put("dock", json(dock)).toString()).apply();
        } catch (Exception ignored) { } // JSONException only on NaN numbers, which we never store
    }

    // Puts an app in the first free home slot, adding a page when all are full.
    void addToHome(String comp) {
        for (String[] page : home) for (int i = 0; i < page.length; i++) if (page[i] == null) { page[i] = comp; save(); return; }
        String[] page = new String[HOME_SLOTS]; page[0] = comp; home.add(page); save();
    }

    boolean addToDock(String comp) {
        for (int i = 0; i < dock.length; i++) if (dock[i] == null) { dock[i] = comp; save(); return true; }
        return false;
    }

    void remove(String[] slots, int i) {
        slots[i] = null;
        // Drop empty extra home pages so the dots stay honest.
        for (int p = home.size() - 1; p > 0; p--) if (Arrays.stream(home.get(p)).allMatch(x -> x == null)) home.remove(p);
        save();
    }

    private static String byPkg(Map<String, String> installed, String pkg) {
        for (String c : installed.keySet()) if (c.startsWith(pkg + "/")) return c;
        return null;
    }

    private static ArrayList<String> strings(JSONArray a) throws Exception {
        ArrayList<String> out = new ArrayList<>();
        for (int i = 0; i < a.length(); i++) out.add(a.getString(i));
        return out;
    }

    private static String[] slots(JSONArray a, int n) throws Exception {
        String[] out = new String[n];
        for (int i = 0; i < Math.min(n, a.length()); i++) out[i] = a.isNull(i) || a.getString(i).isEmpty() ? null : a.getString(i);
        return out;
    }

    private static JSONArray json(String[] slots) {
        JSONArray a = new JSONArray();
        for (String s : slots) a.put(s == null ? "" : s);
        return a;
    }

}
