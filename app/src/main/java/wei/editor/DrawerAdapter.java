package wei.editor;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;
import java.util.List;

public class DrawerAdapter extends BaseAdapter {
    private List<String> list;
    private Context context;
    CloseListener closeListener;

    public DrawerAdapter(Context context, List<String> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.drawer_item, null);
            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.title);
            holder.button = (ImageButton) convertView.findViewById(R.id.btn_close);
            holder.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (closeListener != null) {
                        closeListener.onCloseListener((Integer) holder.button.getTag());
                    }
                }
            });
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.title.setText(new File(list.get(position)).getName());
        holder.button.setTag(position);
        return convertView;
    }

    private static class ViewHolder {
        public TextView title;
        public ImageButton button;
    }

    public void setCloseListener(CloseListener closeListener) {
        this.closeListener = closeListener;
    }

    interface CloseListener {
        void onCloseListener(int position);
    }
}
