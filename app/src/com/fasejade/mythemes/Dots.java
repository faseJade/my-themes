package com.fasejade.mythemes;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

// Page indicator dots.
class Dots extends View {
    int count, current;
    private final Paint on = new Paint(Paint.ANTI_ALIAS_FLAG), off = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float r, gap;

    Dots(Context c, int onColor) {
        super(c);
        float d = c.getResources().getDisplayMetrics().density;
        r = 3.2f * d; gap = 12 * d;
        on.setColor(onColor); off.setColor(0x80FFFFFF);
    }

    void set(int count, int current) { this.count = count; this.current = current; invalidate(); }

    @Override protected void onMeasure(int w, int h) { setMeasuredDimension(MeasureSpec.getSize(w), (int) (r * 6)); }

    @Override protected void onDraw(Canvas c) {
        if (count < 2) return;
        float x0 = getWidth() / 2f - (count - 1) * gap / 2;
        for (int i = 0; i < count; i++) c.drawCircle(x0 + i * gap, getHeight() / 2f, r, i == current ? on : off);
    }
}
