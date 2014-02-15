package viz.groupsendsms.Adpter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.HashMap;

import viz.groupsendsms.R;

/**
 * Created by vi on 14-2-4.
 */
public class SelectedAdpter extends BaseAdapter {
    public SelectedAdpter(Context context, int count) {
        this.context = context;
        this.count = count;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setSelectedPosition(int position) {
        selectedPosition = position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHodler vh;
        mInflater = LayoutInflater.from(context);
        convertView = mInflater.inflate(R.layout.selectedcontacts_items, null);
        if (convertView != null) {
            vh = new ViewHodler();
            vh.selectedContacts_TextView = (TextView) convertView.findViewById(R.id.selectedContacts_TextView);
            vh.selectedContacts_RelativeLayout = (RelativeLayout) convertView.findViewById(R.id.selectedContacts_RelativeLayout);
            convertView.setTag(vh);
        } else {
            vh = (ViewHodler) convertView.getTag();
        }
        if (selectedContacts[position] != null)
            vh.selectedContacts_TextView.setText(selectedContacts[position]);
        if (selectedPosition == position) {
            vh.selectedContacts_TextView.setSelected(true);
            vh.selectedContacts_TextView.setPressed(true);
            vh.selectedContacts_RelativeLayout.setBackgroundColor(Color.GREEN);
        } else {
            vh.selectedContacts_TextView.setSelected(false);
            vh.selectedContacts_TextView.setPressed(false);
            vh.selectedContacts_RelativeLayout.setBackgroundColor(Color.TRANSPARENT);
        }
        return convertView;
    }

    class ViewHodler {
        TextView selectedContacts_TextView;
        RelativeLayout selectedContacts_RelativeLayout;
    }

    private LayoutInflater mInflater;
    Context context;
    int count = 0;
    public String[] selectedContacts;
    private int selectedPosition = -1;// 选中的位置
}
