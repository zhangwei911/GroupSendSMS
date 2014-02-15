package viz.groupsendsms.Adpter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import viz.groupsendsms.R;

/**
 * Created by vi on 14-2-4.
 */
public class SMSAdpter extends BaseAdapter {
    public SMSAdpter(Context context, int count) {
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
        convertView = mInflater.inflate(R.layout.sms_items, null);
        if (convertView != null) {
            vh = new ViewHodler();
            vh.sms_TextView = (TextView) convertView.findViewById(R.id.sms_TextView);
            vh.sms_RelativeLayout = (RelativeLayout) convertView.findViewById(R.id.sms_RelativeLayout);
            convertView.setTag(vh);
        } else {
            vh = (ViewHodler) convertView.getTag();
        }
        if (smsContent[position] != null) vh.sms_TextView.setText(smsContent[position]);
        if (selectedPosition == position) {
            vh.sms_TextView.setSelected(true);
            vh.sms_TextView.setPressed(true);
            vh.sms_RelativeLayout.setBackgroundColor(Color.CYAN);
        } else {
            vh.sms_TextView.setSelected(false);
            vh.sms_TextView.setPressed(false);
            vh.sms_RelativeLayout.setBackgroundColor(Color.TRANSPARENT);
        }
        return convertView;
    }

    class ViewHodler {
        TextView sms_TextView;
        RelativeLayout sms_RelativeLayout;
    }

    private LayoutInflater mInflater;
    Context context;
    int count = 0;
    public String[] smsContent;
    private int selectedPosition = -1;// 选中的位置
}
