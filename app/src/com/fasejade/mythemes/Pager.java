package com.fasejade.mythemes;

import android.content.Context;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

// Sideways pages that snap, like One UI's home and drawer. Vertical swipes are reported instead of scrolled.
class Pager extends ViewGroup {
    interface Listener { void onPage(int page); void onSwipeUp(); void onSwipeDown(); }

    private final Scroller scroller;
    private final int slop, flingVelocity;
    private VelocityTracker vt;
    private float downX, downY, lastX;
    private boolean sideways, vertical;
    int page;
    Listener listener;

    Pager(Context c) {
        super(c);
        scroller = new Scroller(c);
        ViewConfiguration vc = ViewConfiguration.get(c);
        slop = vc.getScaledTouchSlop();
        flingVelocity = vc.getScaledMinimumFlingVelocity() * 4;
    }

    @Override protected void onMeasure(int w, int h) {
        super.onMeasure(w, h);
        for (int i = 0; i < getChildCount(); i++)
            getChildAt(i).measure(MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY));
    }

    @Override protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int w = r - l;
        for (int i = 0; i < getChildCount(); i++) getChildAt(i).layout(i * w, 0, (i + 1) * w, b - t);
        if (changed) scrollTo(Math.min(page, Math.max(0, getChildCount() - 1)) * w, 0);
    }

    @Override public boolean onInterceptTouchEvent(MotionEvent e) {
        track(e);
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX = lastX = e.getX(); downY = e.getY(); sideways = vertical = false;
                if (!scroller.isFinished()) { scroller.abortAnimation(); sideways = true; }
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = Math.abs(e.getX() - downX), dy = Math.abs(e.getY() - downY);
                if (dx > slop && dx > dy) sideways = true;
                else if (dy > slop * 2 && dy > dx) vertical = true;
                break;
        }
        return sideways || vertical;
    }

    @Override public boolean onTouchEvent(MotionEvent e) {
        track(e);
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: return true;
            case MotionEvent.ACTION_MOVE:
                if (sideways) {
                    int max = Math.max(0, getChildCount() - 1) * getWidth();
                    scrollTo((int) Math.max(-getWidth() * .2f, Math.min(max + getWidth() * .2f, getScrollX() + lastX - e.getX())), 0);
                }
                lastX = e.getX();
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (sideways) {
                    vt.computeCurrentVelocity(1000);
                    float v = vt.getXVelocity();
                    int target = Math.round((float) getScrollX() / getWidth());
                    if (v < -flingVelocity) target = (int) Math.floor((float) getScrollX() / getWidth()) + 1;
                    else if (v > flingVelocity) target = (int) Math.floor((float) getScrollX() / getWidth());
                    snapTo(target);
                } else if (vertical && listener != null && e.getActionMasked() == MotionEvent.ACTION_UP) {
                    if (e.getY() < downY) listener.onSwipeUp(); else listener.onSwipeDown();
                }
                sideways = vertical = false;
                if (vt != null) { vt.recycle(); vt = null; }
                return true;
        }
        return true;
    }

    void snapTo(int target) {
        page = Math.max(0, Math.min(getChildCount() - 1, target));
        int dx = page * getWidth() - getScrollX();
        scroller.startScroll(getScrollX(), 0, dx, 0, Math.min(380, 120 + Math.abs(dx) / 3));
        invalidate();
        if (listener != null) listener.onPage(page);
    }

    @Override public void computeScroll() {
        if (scroller.computeScrollOffset()) { scrollTo(scroller.getCurrX(), 0); postInvalidateOnAnimation(); }
    }

    private void track(MotionEvent e) {
        if (vt == null) vt = VelocityTracker.obtain();
        vt.addMovement(e);
    }
}
