package com.fasejade.mythemes;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Theme icons for apps, amber letter tiles for apps the theme has no icon for, and folder icons.
class Icons {
    static final int BG = 0xFF080604, AMBER = 0xFFFF8A24, PANEL = 0xFF140D07;
    private final Context c;
    private final Map<String, String> links = new HashMap<>(), byPkg = new HashMap<>();

    Icons(Context c, String theme) {
        this.c = c;
        try (BufferedReader r = new BufferedReader(new InputStreamReader(c.getAssets().open("themes/" + theme + "/links.txt")))) {
            for (String line; (line = r.readLine()) != null; ) {
                String[] p = line.split("=", 2);
                if (p.length < 2) continue;
                links.put(p[0], p[1]);
                byPkg.put(p[0].split("/")[0], p[1]);
            }
        } catch (Exception ignored) { } // no links file: every app gets a letter tile
    }

    // Exact activity first, then the package, since apps sometimes rename their start activity in updates.
    Drawable app(String comp, String label) {
        String res = links.containsKey(comp) ? links.get(comp) : byPkg.get(comp.split("/")[0]);
        int id = res == null ? 0 : c.getResources().getIdentifier(res, "drawable", c.getPackageName());
        return id != 0 ? c.getDrawable(id) : new Tile(label);
    }

    Drawable folder(List<String> comps, Map<String, String> labels) {
        Drawable[] minis = new Drawable[Math.min(9, comps.size())];
        for (int i = 0; i < minis.length; i++) minis[i] = app(comps.get(i), labels.getOrDefault(comps.get(i), "?"));
        return new Folder(minis);
    }

    // The same frame as the PNG icons: inset 1/32, corner 9/32, stroke 1.4/32.
    private abstract static class Framed extends Drawable {
        final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG), line = new Paint(Paint.ANTI_ALIAS_FLAG);
        Framed(int fillColor) { fill.setColor(fillColor); line.setColor(AMBER); line.setStyle(Paint.Style.STROKE); }
        void frame(Canvas c) {
            float s = getBounds().width() / 32f;
            RectF r = new RectF(getBounds()); r.inset(s * 1.7f, s * 1.7f);
            line.setStrokeWidth(s * 1.4f);
            c.drawRoundRect(r, s * 9, s * 9, fill); c.drawRoundRect(r, s * 9, s * 9, line);
        }
        @Override public void setAlpha(int a) { }
        @Override public void setColorFilter(ColorFilter f) { }
        @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
    }

    private static class Tile extends Framed {
        private final String letter;
        private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
        Tile(String label) {
            super(BG);
            String l = "?";
            for (char ch : label.toCharArray()) if (Character.isLetterOrDigit(ch)) { l = String.valueOf(ch).toUpperCase(); break; }
            letter = l;
            text.setColor(AMBER); text.setTypeface(Typeface.DEFAULT_BOLD); text.setTextAlign(Paint.Align.CENTER);
        }
        @Override public void draw(Canvas c) {
            frame(c);
            text.setTextSize(getBounds().height() * .42f);
            c.drawText(letter, getBounds().exactCenterX(), getBounds().exactCenterY() - (text.ascent() + text.descent()) / 2, text);
        }
    }

    private static class Folder extends Framed {
        private final Drawable[] minis;
        Folder(Drawable[] minis) { super(PANEL); this.minis = minis; }
        @Override public void draw(Canvas c) {
            frame(c);
            float s = getBounds().width() / 32f, cell = s * 7.6f, x0 = getBounds().left + s * 4.6f, y0 = getBounds().top + s * 4.6f;
            for (int i = 0; i < minis.length; i++) {
                int x = (int) (x0 + i % 3 * cell), y = (int) (y0 + i / 3 * cell);
                minis[i].setBounds(x, y, (int) (x + cell), (int) (y + cell));
                minis[i].draw(c);
            }
        }
    }
}
