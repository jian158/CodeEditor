package wei.editor.Editor;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;

public class EditViewInputConnection extends BaseInputConnection {

    InputConnectionListener inputConnectionListener;

    public EditViewInputConnection(View targetView, boolean fullEditor) {
        super(targetView, fullEditor);
    }

    @Override
    public boolean sendKeyEvent(KeyEvent event) {
        Log.i("KEY", event.toString());
        return super.sendKeyEvent(event);
    }

    @Override
    public boolean deleteSurroundingText(int beforeLength, int afterLength) {
        Log.i("Connect", "YES");
        return super.deleteSurroundingText(beforeLength, afterLength);
    }

    @Override
    public boolean commitText(CharSequence text, int newCursorPosition) {
        Log.i("Text", text.toString());
        Log.i("Content", getEditable().toString());
        if (inputConnectionListener != null) {
            inputConnectionListener.commitText(text, newCursorPosition);
        }
        return super.commitText(text, newCursorPosition);
    }

    public void setInputConnectionListener(InputConnectionListener listener) {
        inputConnectionListener = listener;
    }


}
