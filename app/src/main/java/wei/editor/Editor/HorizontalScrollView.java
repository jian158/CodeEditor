package wei.editor.Editor;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class HorizontalScrollView extends android.widget.HorizontalScrollView {
    public HorizontalScrollView(Context context) {
        super(context);
    }

    public HorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public HorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    OnScrollListener mOnScrollListener;

    public void setOnScrollListener(OnScrollListener onScrollListener) {
        mOnScrollListener = onScrollListener;
    }


    private long time = 0;
    private final static long INTERVAL = 160;

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                time = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_UP:
                long intern = System.currentTimeMillis() - time;
                if (intern < INTERVAL && mOnScrollListener != null) {
                    mOnScrollListener.onSingleTab(ev);
                }
                break;

        }
        return super.onTouchEvent(ev);
    }


}
