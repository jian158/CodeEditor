package wei.editor.Editor;

import android.view.MotionEvent;

public interface OnScrollListener {
    enum STATE {IDLE, SCROLL, FLING, TAB}

    ;

    void onSingleTab(MotionEvent event);

    void onScrollStateChanged(STATE scrollState);

    void onScroll(boolean isTouchScroll, int l, int t, int oldl, int oldt);
}
