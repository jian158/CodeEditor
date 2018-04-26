package wei.editor.Editor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;


public class Editor extends View implements OnScrollListener, View.OnKeyListener, InputConnectionListener, SytaxHighLight.SytaxListener {
    protected InputMethodManager inputMethodManager;
    protected EditViewInputConnection inputConnection;
    protected Context context;
    protected Paint paint = new Paint();

    SytaxHighLight sytaxHighLight;

    ArrayList<SpanString> textList = new ArrayList<>();

    float x = 0;

    int lineHeight = 0;

    int startLine = 0;

    int visibleCount = 0;

    int preLineCountBit = 0;

    public final static int LINE_PADDING = 16;

    int selectionLine = 0;

    float cursorX = 0, cursorY = 0;
    int cursorIndex = 0;

    private Timer cursorTimer = new Timer();
    private boolean cursorVisibility = true;

    public Editor(Context context) {
        this(context, null);
    }

    public Editor(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Editor(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public Editor(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        setOnKeyListener(this);
        inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputConnection = new EditViewInputConnection(this, true);
        paint.setTextSize(48);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setAntiAlias(true);
        textList.add(new SpanString(""));
        cursorTimer.scheduleAtFixedRate(cursorTask, 0, 800);
        lineHeight = 54;
        visibleCount = 1920 / lineHeight;
    }

    public void setFontSize(float size) {
        if (size >= 48 && size <= 96)
            paint.setTextSize(size);
    }

    public void setSytaxHighLight(String fileName) {
        sytaxHighLight = new SytaxHighLight(fileName, this);
    }

    TimerTask cursorTask = new TimerTask() {
        @Override
        public void run() {
            cursorVisibility = !cursorVisibility;
            postInvalidate();
        }
    };


    Rect mRect = new Rect();

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = resolveSize(1080, widthMeasureSpec);
        int height = resolveSize(1920, widthMeasureSpec);

        String maxString = "";
        int max = 0;
        for (SpanString s : textList) {
            if (s.length() > max) {
                max = s.length();
                maxString = s.toString();
            }
        }

        if (TextUtils.isEmpty(maxString))
            maxString = "Hello";

        paint.getTextBounds(maxString, 0, maxString.length(), mRect);

        width = mRect.width() > width ? mRect.width() + getPaddingLeft() * 3 : width;
        height += textList.size() * lineHeight;
        setMeasuredDimension(width, height);
        cursorX = getPaddingLeft();
//        Log.i("lingHeight",String.valueOf(lineHeight));
//        Log.i("X",String.valueOf(cursorX));
//        Log.i("Y",String.valueOf(cursorY));

    }

    protected void updatePadding() {
        cursorX -= getPaddingLeft();
        Rect rect = new Rect();
        String s = String.valueOf(getLineCount());
        paint.getTextBounds(s, 0, s.length(), rect);
        x = rect.width() + 16;
        setPadding((int) x, 0, 0, 0);
        cursorX += getPaddingLeft();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        int lineCount = getLineCount();
        if (preLineCountBit != String.valueOf(lineCount).length()) {
            preLineCountBit = String.valueOf(lineCount).length();
            updatePadding();
        }

        paint.setStrokeWidth(0);
        paint.setColor(Color.rgb(150, 150, 150));
        float top = selectionLine * lineHeight + LINE_PADDING;
        canvas.drawRect(getPaddingLeft(), top, getWidth(), top + lineHeight, paint);

        synchronized (Editor.class) {
            paint.setStrokeWidth(0);
            int start = startLine > visibleCount ? startLine - visibleCount : 0;
            for (int i = start; i < textList.size() && i < startLine + visibleCount * 2; i++) {
                paint.setColor(Color.GREEN);
                canvas.drawText(String.valueOf(i + 1), 0, (i + 1) * lineHeight, paint);
                paint.setColor(Color.BLACK);
                SpanString spanString = textList.get(i);
                canvas.drawText(spanString.toString(), x, (i + 1) * lineHeight, paint);
                spanString.drawText(canvas, paint, x, (i + 1) * lineHeight);
            }
        }
        if (cursorVisibility) {
            paint.setStrokeWidth(6);
            paint.setColor(Color.RED);
            canvas.drawLine(cursorX, cursorY + LINE_PADDING, cursorX, cursorY + lineHeight + LINE_PADDING / 2, paint);
        }

        super.onDraw(canvas);
    }


    private void setCursorPos(MotionEvent event) {
        selectionLine = ((int) event.getY()) / lineHeight;
        if (selectionLine >= textList.size()) {
            selectionLine = textList.size() - 1;
        }
        cursorY = selectionLine * lineHeight;
        cursorX = event.getX() - getPaddingLeft();
        String selectText = textList.get(selectionLine).toString();
        int low = 0, high = selectText.length(), mid;
        while (low <= high) {
            mid = (low + high) >> 1;
            float width = paint.measureText(selectText, 0, mid);
            if (width < cursorX) {
                low = mid + 1;
            } else if (width > cursorX) {
                high = mid - 1;
            }
        }
        if (high > selectText.length())
            high = selectText.length();
        if (high < 0)
            high = 0;
        cursorIndex = high;
        cursorX = paint.measureText(selectText, 0, high) + getPaddingLeft();
        invalidate();
    }


    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        inputConnection.setInputConnectionListener(this);
        return inputConnection;
    }

    @Override
    public boolean onCheckIsTextEditor() {
        return true;
    }


    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN)
            return false;
        Log.i("Keycode", String.valueOf(keyCode));
        if (KeyEvent.KEYCODE_DEL == keyCode) {
            if (cursorIndex == 0 && textList.get(selectionLine).length() == 0 && textList.size() > 1) {
                textList.remove(selectionLine);
                String Text = textList.get(selectionLine - 1).toString();
                cursorIndex = Text.length();
                cursorX = paint.measureText(Text) + getPaddingLeft();
                cursorY -= lineHeight;
                selectionLine--;
            } else if (cursorIndex == 0 && textList.get(selectionLine).length() > 0) {
                SpanString spanString = textList.get(selectionLine - 1);
                cursorIndex = spanString.toString().length();
                cursorX = paint.measureText(spanString.toString()) + getPaddingLeft();
                cursorY -= lineHeight;
                String Text = spanString.toString() + textList.get(selectionLine).toString();
                spanString.setString(Text);
                spanString.setModify(true);
                textList.remove(selectionLine);
                selectionLine--;
            } else if (textList.get(selectionLine).length() > 0) {
                SpanString spanString = textList.get(selectionLine);
                StringBuilder builder = new StringBuilder(spanString.toString());
                char p = builder.charAt(cursorIndex - 1);
                builder.delete(cursorIndex - 1, cursorIndex);
                spanString.setString(builder.toString());
                spanString.setModify(true);
                cursorX -= paint.measureText(new char[]{p}, 0, 1);
                cursorIndex--;
            }
            invalidate();
            highLight();

        } else if (keyCode == KeyEvent.KEYCODE_ENTER) {
            String s = "";
            SpanString spanString = textList.get(selectionLine);
            if (cursorIndex < spanString.length()) {
                s = spanString.toString().substring(cursorIndex);
                spanString.setString(spanString.toString().substring(0, cursorIndex));
                spanString.setModify(true);
            }

            textList.add(++selectionLine, new SpanString(s));
            cursorX = getPaddingLeft();
            cursorY += lineHeight;
            cursorIndex = 0;
            invalidate();
            highLight();
        }
        return true;
    }

    @Override
    public void commitText(CharSequence text, int newCursorPosition) {
        SpanString spanString = textList.get(selectionLine);
        StringBuilder builder = new StringBuilder(spanString.toString());
        builder.insert(cursorIndex, text);
        spanString.setString(builder.toString());
        spanString.setModify(true);
        cursorIndex += text.length();
        cursorX += paint.measureText(text.toString());
        invalidate();
        highLight();
    }


    public int getLineHeight() {
        return lineHeight;
    }

    public void setText(final String text) {
        selectionLine = 0;
        startLine = 0;
        cursorX = getPaddingLeft();
        cursorY = 0;
        if (text == null || TextUtils.isEmpty(text)) {
            synchronized (textList) {
                textList.clear();
            }
            textList.add(new SpanString(""));
            notifyDataAll();
            return;
        }
        String[] texts = text.split("\n");
        textList.clear();
        for (int i = 0; i < texts.length; i++) {
            textList.add(new SpanString(texts[i]));
        }
        notifyDataAll();
    }

    public void notifyDataAll() {
        requestLayout();
        invalidate();
        if (sytaxHighLight != null && !sytaxHighLight.isNull()) {
            sytaxHighLight.startHighLight(textList, 0, visibleCount * 2, paint);
            Log.i("Editor", "NOT NULL");
        } else Log.i("Editor", "NULL");
    }

    public void append(String text) {
        textList.add(new SpanString(text));
        notifyDataAll();
    }

    public void appendPost(String text) {
        textList.add(new SpanString(text));
    }

    public void clear() {
        synchronized (Editor.class) {
            textList.clear();
            cursorX = getPaddingLeft();
            cursorY = 0;
            startLine = 0;
            selectionLine = 0;
            cursorIndex = 0;
        }
        invalidate();
    }

    public int getLineCount() {
        return textList.size();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        setCursorPos(event);

        return false;
    }

    @Override
    public void onSingleTab(MotionEvent event) {
        Log.i("Event", String.valueOf(event.toString()));
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        inputMethodManager.showSoftInput(this, InputMethodManager.HIDE_NOT_ALWAYS);
    }


    @Override
    public void onScrollStateChanged(OnScrollListener.STATE scrollState) {
        if (scrollState == OnScrollListener.STATE.IDLE && sytaxHighLight != null && !sytaxHighLight.isNull()) {
            highLight();
        }
    }

    @Override
    public void onScroll(boolean isTouchScroll, int l, int t, int oldl, int oldt) {
        int first = t / lineHeight;
        if (first > startLine + visibleCount || first < startLine - visibleCount / 2) {
            startLine = first;
            invalidate();
        }
    }

    private void highLight() {
        if (sytaxHighLight != null && !sytaxHighLight.isNull()) {
            sytaxHighLight.startHighLight(textList, startLine - visibleCount, startLine + visibleCount * 2, paint);
        }
    }

    @Override
    public void onComplete(int start) {
        if (start > startLine - visibleCount && start < startLine + visibleCount * 2) {
            invalidate();
            Log.i("Eedior", "FLUSH");
        } else {
            Log.i("Eedior", start + "\t" + startLine);
        }
    }

}
