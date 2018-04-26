package wei.editor.Editor;

import android.graphics.Paint;
import android.os.Handler;
import android.util.Log;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SytaxHighLight {
    private Pattern lineComment, stateMent, string, number, type, weak;
    String endName = "";
    private final Handler handler = new Handler();
    private ExecutorService pool;
    private boolean isNull = false;
    private SytaxListener sytaxListener;

    public SytaxHighLight(String FileName, SytaxListener sytaxListener) {
        this.sytaxListener = sytaxListener;
        int index;
        if ((index = FileName.lastIndexOf('.')) > -1) {
            endName = FileName.substring(index + 1).toLowerCase();
        }
        switch (endName) {
            case "java":
                setJavaPattern();
                break;
            case "c":
            case "cpp":
            case "h":
                setCppPattern();
                break;
            case "py":
                setPyPattern();
                break;
            case "xml":
            case "html":
            case "htm":
                setHtmlPattern();
                break;
            default:
                endName = "";
                isNull = true;
        }
        if (!endName.equals("")) {
            pool = Executors.newSingleThreadExecutor();
        }
    }

    public boolean isNull() {
        return isNull;
    }

    public void startHighLight(List<SpanString> list, int start, int end, Paint paint) {
        if (start < 0)
            start = 0;
        if (end > list.size())
            end = list.size();
        pool.execute(new HighLightTask(list, start, end, paint));
    }

    private void setJavaPattern() {
        lineComment = Pattern.compile("//[^\\n]*");
        stateMent = Pattern.compile("\\b(public|protected|private|final|static|break|case|catch|continue|default|do|else|finally|for|goto|if|return|switch|synchronized|throw|try|while|abstract|assert|class|const|enum|extends|implements|import|instanceof|interface|native|package|strictfp|super|this|throws|transient|volatile)\\b");
        string = Pattern.compile("\"(\\\\\\\"|.)*?\"");
        number = Pattern.compile("\\b((0[Xx][0-9a-fA-F]*)|([-]?[0-9]*))\\b");
        type = Pattern.compile("\\b(void|boolean|byte|short|int|long|float|double|char|new|String)\\b");
        weak = Pattern.compile("^.*\\*/|/\\*.*$");
    }

    private void setCppPattern() {
        lineComment = Pattern.compile("//[^\\n]*");
        stateMent = Pattern.compile("\\b(if|else|for|while|class|typedef|const|public|protected|private|static|virtual|break|case|this|return)\\b");
        string = Pattern.compile("\"(\"|.)*?\"");
        number = Pattern.compile("\\b((0[Xx][0-9a-fA-F]*)|([-]?[0-9]*))\\b");
        type = Pattern.compile("\\b(void|boolean|byte|short|int|long|float|double|char|new|String)\\b");
        weak = Pattern.compile("^.*\\*/|/\\*.*$");
    }

    private void setPyPattern() {
        lineComment = Pattern.compile("#[^\\n]*");
        stateMent = Pattern.compile("\\b(and|del|from|not|while|as|elif|global|or|with|assert|else|if|pass|yield|break|except|import|print|class|exec|in|raise|continue|finally|is|return|def|for|lambda|try|True|False|None|self|NotImplemented|Ellipsis|__debug__|__file__)\\b");
        string = Pattern.compile("(r|u|ur|R|U|UR|Ur|uR|b|B|br|Br|bR|BR)?(((\"\"\"((\\\\.)|[^\\\\])*?\"\"\")|(\'\'\'((\\\\.)|[^\\\\])*?\'\'\'))|((\"((\\\\.)|[^\\n\"\\\\])*\")|('((\\\\.)|[^\\n\'\\\\])*\')))");
        number = Pattern.compile("(?<=[\\s\\n\\+\\-\\*/%<>=&\\|^~\\(\\[\\{,\\:;])(?i)(((\\d+\\.?\\d*E(\\+|-)?\\d+)|(\\d*\\.\\d+)|(\\d+\\.))J?|(((([1-9]\\d*)|0)|(0O*[0-7]+)|(0X[\\dA-F]+)|(0B(0|1)+))L?)|\\d+J)(?=[\\s\\n\\+\\-\\*/%<>=&\\|^~!\\)\\]\\},\\:;#])");
        type = Pattern.compile("\\b(int|float|long|complex|str|unicode|list|tuple|bytearray|buffer|xrange|set|frozenset|dict|bool)\\b");
    }

    private void setHtmlPattern() {
        lineComment = Pattern.compile("<!--.*?-->|<!\\[CDATA\\[.*?\\]\\]>");
        stateMent = Pattern.compile("(<[^>\\s]*|(/|\\?)\\s*>|>)");
        string = Pattern.compile("\"(\\\\\\\"|.)*?\"|'(\\\\\\'|.)*?'");
        number = Pattern.compile("&([a-zA-Z]{2,10}?|#[a-zA-Z0-9]{2,10}?);");
        type = Pattern.compile("[^\\s]+(?==)");
    }

    public Pattern getLineComment() {
        return lineComment;
    }

    public Pattern getNumber() {
        return number;
    }

    public Pattern getStateMent() {
        return stateMent;
    }

    public Pattern getString() {
        return string;
    }

    public Pattern getType() {
        return type;
    }

    public interface SytaxListener {
        void onComplete(int startLine);
    }

    private class HighLightTask implements Runnable {
        private List<SpanString> list;
        private int start, end;
        private Paint paint;

        public HighLightTask(List<SpanString> list, int start, int end, Paint paint) {
            this.list = list;
            this.start = start;
            this.end = end;
            this.paint = paint;
        }

        @Override
        public void run() {
            boolean isFlush = false;
            synchronized (SytaxHighLight.class) {
                int n = list.size();
//                StringBuilder builder=new StringBuilder();
                for (int i = start; i < end && i < n; i++) {
                    SpanString s = list.get(i);
                    if (s.isModify()) {
                        isFlush = true;
                        s.startHighLight(SytaxHighLight.this, paint);
                    }
//                    builder.append(s.toString()).append("\n");
                }
//                if(weak!=null){
//                    Matcher matcher=weak.matcher(builder.toString());
//
//                }
            }
            if (isFlush && sytaxListener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        sytaxListener.onComplete(start);
                    }
                });
            }
        }
    }

}
