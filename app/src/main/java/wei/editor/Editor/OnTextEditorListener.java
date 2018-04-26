package wei.editor.Editor;

import android.view.MotionEvent;

/**
 * Created by wei on 2018/2/10.
 */

public interface OnTextEditorListener {
    void onSingleTab(MotionEvent event);

    void onScrollChanged(int l, int t, int oldl, int oldt);
}
