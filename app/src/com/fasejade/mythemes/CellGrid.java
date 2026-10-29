package com.fasejade.mythemes;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

// A fixed cols x rows grid; child i sits in slot i. Without an exact height, cells are 5:4 tall.
class CellGrid extends ViewGroup {
    final int cols, rows;

    CellGrid(Context c, int cols, int rows) { super(c); this.cols = cols; this.rows = rows; }

    @Override protected void onMeasure(int w, int h) {
        int width = MeasureSpec.getSize(w);
        int height = MeasureSpec.getMode(h) == MeasureSpec.EXACTLY ? MeasureSpec.getSize(h) : width / cols * 5 / 4 * rows;
        int cw = width / cols, ch = height / rows;
        for (int i = 0; i < getChildCount(); i++)
            getChildAt(i).measure(MeasureSpec.makeMeasureSpec(cw, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(ch, MeasureSpec.EXACTLY));
        setMeasuredDimension(width, height);
    }

    @Override protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int cw = (r - l) / cols, ch = (b - t) / rows;
        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            int x = i % cols * cw, y = i / cols * ch;
            v.layout(x, y, x + cw, y + ch);
        }
    }
}
