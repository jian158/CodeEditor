package wei.editor.Editor;


import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;


public class ScrollView extends android.widget.ScrollView {
    public ScrollView(Context context) {
        super(context);
    }

    public ScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


//    public static final int SCROLL_IDLE = 0,SCROLL_SCROLL = 1,SCROLL_FLING = 2;


    private static final int CHECK_SCROLL_STOP_DELAY_MILLIS = 80;
    private static final int MSG_SCROLL = 1, MSG_SINGLE_TAB = 2;

    private boolean mIsTouched = false;
    private OnScrollListener.STATE mScrollState = OnScrollListener.STATE.IDLE;

    private OnScrollListener mOnScrollListener;

    private final Handler mHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {

        private int mLastY = Integer.MIN_VALUE;

        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == MSG_SCROLL) {
                final int scrollY = getScrollY();
                if (!mIsTouched && mLastY == scrollY) {
                    mLastY = Integer.MIN_VALUE;
                    setScrollState(OnScrollListener.STATE.IDLE);
                } else {
                    mLastY = scrollY;
                    checkScrollTiming();
                }
                return true;
            } else if (msg.what == MSG_SINGLE_TAB && mOnScrollListener != null) {
//                mOnScrollListener.onSingleTab((MotionEvent) msg.obj);
            }
            return false;
        }
    });

    private void checkScrollTiming() {
        mHandler.removeMessages(MSG_SCROLL);
        mHandler.sendEmptyMessageDelayed(MSG_SCROLL, CHECK_SCROLL_STOP_DELAY_MILLIS);
    }

    private void checkSingleTabTiming(MotionEvent event) {
        mHandler.removeMessages(MSG_SINGLE_TAB);
        Message message = new Message();
        message.what = MSG_SINGLE_TAB;
        message.obj = event;
        mHandler.sendMessageDelayed(message, CHECK_SCROLL_STOP_DELAY_MILLIS * 2);
    }

    public void setOnScrollListener(OnScrollListener onScrollListener) {
        mOnScrollListener = onScrollListener;
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        handleDownEvent(ev);
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mHandler.removeMessages(MSG_SINGLE_TAB);
        handleUpEvent(ev);
        return super.onTouchEvent(ev);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (mIsTouched) {
            setScrollState(OnScrollListener.STATE.SCROLL);
        } else {
            setScrollState(OnScrollListener.STATE.FLING);
            checkScrollTiming();
        }
        if (mOnScrollListener != null) {
            mOnScrollListener.onScroll(mIsTouched, l, t, oldl, oldt);
        }
    }

    private void handleDownEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mIsTouched = true;
                checkSingleTabTiming(ev);
                break;
        }
    }

    private void handleUpEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsTouched = false;
                checkScrollTiming();
                break;
        }
    }

    private void setScrollState(OnScrollListener.STATE state) {
        if (mScrollState != state) {
            mScrollState = state;
            if (mOnScrollListener != null) {
                mOnScrollListener.onScrollStateChanged(state);
            }
        }
    }


}
