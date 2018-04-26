package wei.editor.Editor;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.ArrayList;
import java.util.regex.Matcher;

public class SpanString {
    private String text;
    private ArrayList<SplitString> spans = new ArrayList<>();
    private boolean isModified = true;

    public SpanString(String text) {
        this.text = text;
    }

    public void setString(String s) {
        isModified = true;
        reset();
        text = s;
    }

    public void setModify(boolean modify) {
        isModified = modify;
    }

    public boolean isModify() {
        return isModified;
    }


    public void drawText(Canvas canvas, Paint paint, float baseOffsetX, float baseOffsetY) {
        synchronized (SpanString.class) {
            for (SplitString split : spans) {
                paint.setColor(split.color);
                canvas.drawText(split.string, baseOffsetX + split.offset, baseOffsetY, paint);
            }
        }
    }

    public void reset() {
        spans.clear();
    }

    public synchronized void startHighLight(SytaxHighLight sytaxHighLight, Paint paint) {
        synchronized (SpanString.class) {
            reset();

            Matcher matcher = sytaxHighLight.getLineComment().matcher(text);
            int indexOfComment = text.length();
            if (matcher.find()) {
                indexOfComment = matcher.start();
                String s = text.substring(matcher.start(), matcher.end());
                spans.add(new SplitString(s, paint.measureText(text, 0, matcher.start()), Color.GRAY));
            }

            String mText = text.substring(0, indexOfComment);

            matcher = sytaxHighLight.getNumber().matcher(mText);
            while (matcher.find()) {
                String s = mText.substring(matcher.start(), matcher.end());
                spans.add(new SplitString(s, paint.measureText(mText, 0, matcher.start()), Color.GREEN));
            }

            matcher = sytaxHighLight.getString().matcher(mText);
            while (matcher.find()) {
                String s = mText.substring(matcher.start(), matcher.end());
                spans.add(new SplitString(s, paint.measureText(mText, 0, matcher.start()), 0xFF00FFaa));
            }

            matcher = sytaxHighLight.getStateMent().matcher(mText);
            while (matcher.find()) {
                String s = mText.substring(matcher.start(), matcher.end());
                spans.add(new SplitString(s, paint.measureText(mText, 0, matcher.start()), Color.BLUE));
            }

            matcher = sytaxHighLight.getType().matcher(mText);
            while (matcher.find()) {
                String s = mText.substring(matcher.start(), matcher.end());
                spans.add(new SplitString(s, paint.measureText(mText, 0, matcher.start()), Color.DKGRAY));
            }

            isModified = false;
        }

    }

    @Override
    public String toString() {
        return text;
    }

    public int length() {
        return text.length();
    }

    class SplitString {
        public String string;
        public float offset = 0;
        public int color = Color.BLACK;

        public SplitString(String s, float offset, int color) {
            this.offset = offset;
            this.color = color;
            string = s;
        }
    }
}
