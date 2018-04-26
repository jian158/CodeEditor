package wei.editor.Editor;

import android.Manifest;
import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class TextEditor extends ScrollView {
    private HorizontalScrollView horizontalScrollView;
    private Editor editor;
    private File file;
    private Context context;

    public TextEditor(Context context) {
        super(context);
        onStart(context);
    }

    public TextEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
        onStart(context);
    }

    public TextEditor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onStart(context);
    }

    public TextEditor(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        onStart(context);
    }

    public String getName() {
        return file.getName();
    }

    public String getPath() {
        return file.getPath();
    }

    public void save() {
        if (file != null)
            new OpenFile(TYPE.SAVE).execute();
    }

    private enum TYPE {READ, SAVE}

    ;

    public void Open(final String Path) {
        file = new File(Path);
        new OpenFile(TYPE.READ).execute();
    }

    private class OpenFile extends AsyncTask<String, Void, Void> {
        private TYPE type;
        private String state = "";

        public OpenFile(TYPE type) {
            this.type = type;
            if (type == TYPE.READ) {
                editor.clear();
            }
        }

        @Override
        protected Void doInBackground(String... strings) {
            if (type == TYPE.READ) {
                read();
            } else {
                save();
            }
            return null;
        }

        private void save() {
            ArrayList<SpanString> list = editor.textList;
            synchronized (Editor.class) {
                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                    for (SpanString s : list) {
                        writer.write(s.toString());
                        writer.newLine();
                    }
                    writer.close();
                } catch (IOException e) {
                    state = e.toString();
                    e.printStackTrace();
                }

            }
        }

        private void read() {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = reader.readLine()) != null) {
                    editor.appendPost(line);
                }
                reader.close();
            } catch (IOException e) {
                state = e.toString();
                e.printStackTrace();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (type == TYPE.READ && state.equals("")) {
                flush(file.getName());
            } else if (state.equals("")) {
                Toast.makeText(context, "保存完毕", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "操作失败" + state, Toast.LENGTH_SHORT).show();
            }
            super.onPostExecute(aVoid);
        }

    }

    private void onStart(Context context) {
        this.context = context;
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        setLayoutParams(params);
        horizontalScrollView = new HorizontalScrollView(context);
        horizontalScrollView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        addView(horizontalScrollView);
        editor = new Editor(context);
        editor.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        horizontalScrollView.addView(editor);
        setOnScrollListener(editor);
        horizontalScrollView.setOnScrollListener(editor);
    }


    public int size() {
        return editor.getLineCount();
    }

    public void test() {
        invalidate();
    }

    public void appendPost(String text) {
        editor.appendPost(text);
    }

    public void setText(String text) {
        editor.setText(text);
    }

    public void flush(String fileName) {
        editor.setSytaxHighLight(fileName);
        editor.notifyDataAll();
    }

    public void clear() {
        editor.clear();
    }


}
