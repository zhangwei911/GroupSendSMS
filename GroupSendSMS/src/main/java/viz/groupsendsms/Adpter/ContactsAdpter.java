package viz.groupsendsms.Adpter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.HashMap;

import viz.groupsendsms.R;

/**
 * Created by vi on 14-2-4.
 */
public class ContactsAdpter extends BaseAdapter {
    public ContactsAdpter(Context context, int count) {
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHodler vh;
        mInflater = LayoutInflater.from(context);
        convertView = mInflater.inflate(R.layout.cl_items, null);
        if (convertView != null) {
            vh = new ViewHodler();
            vh.contacts_checkBox = (CheckBox) convertView.findViewById(R.id.contacts_checkBox);
            convertView.setTag(vh);
        } else {
            vh = (ViewHodler) convertView.getTag();
        }
        if (contactsNames[position] != null) vh.contacts_checkBox.setText("    "+contactsNames[position]);
        //根据checkMap中position的状态设置是否被选中
        if (checkedMap.get(position) != null && checkedMap.get(position) == true) {
            vh.contacts_checkBox.setChecked(true);
        }else{
            vh.contacts_checkBox.setChecked(false);
        }
        return convertView;
    }

    class ViewHodler {
        CheckBox contacts_checkBox;
    }

    private LayoutInflater mInflater;
    Context context;
    public String[] contactsNames;
    int count = 0;
    public HashMap<Integer, Boolean> checkedMap = new HashMap<Integer, Boolean>();
}
