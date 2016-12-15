package viz.groupsendsms;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import viz.groupsendsms.Adpter.ContactsAdpter;

/**
 * Created by vi on 14-2-4.
 */
public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.c);
        Intent intent = getIntent();
        contactsNames = intent.getStringArrayExtra("contactsNames");
        contactsNamesAndPhoneNo = intent.getStringArrayExtra("contactsNamesAndPhoneNo");
        if (!contactsNames[0].equals("nodata")) {
            contactsCount = contactsNames.length;
            contactsAdpter = new ContactsAdpter(context, contactsCount);
            contactsAdpter.contactsNames = new String[contactsCount];
            for (int i = 0; i < contactsCount; i++) {
                contactsAdpter.contactsNames[i] = contactsNames[i];
            }
        } else {
            contactsAdpter = new ContactsAdpter(context, 0);
        }
        c_listView = (ListView) findViewById(R.id.c_listView);
        c_listView.setItemsCanFocus(false);
        c_listView.setAdapter(contactsAdpter);
        c_listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        c_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /*对于由position指定的项目，返回其是否被选中。
     * 只有当选择模式已被设置为CHOICE_MODE_SINGLE或CHOICE_MODE_MULTIPLE时 ，结果才有效。
     */
                boolean checked = c_listView.isItemChecked(position);
                contactsAdpter.checkedMap.put(position, checked);
                contactsAdpter.notifyDataSetChanged();
                if (!selectedItemId.equals("")) {
                    if (selectedItemId.contains("@")) {
                        String[] itemid = selectedItemId.split("@");
                        for (int i = 0; i < itemid.length; i++) {
                            if (itemid[i].equals(position + "")) {
                                if (i == 0) {
                                    selectedItemId = selectedItemId.replace(position + "@", "");
                                } else {
                                    selectedItemId = selectedItemId.replace("@" + position, "");
                                }
                                selected = true;
                            }
                        }
                    } else {
                        if (selectedItemId.equals(position + "")) {
                            selectedItemId = "";
                            selected = true;
                        }
                    }
                    if (!selected) {
                        selectedItemId = selectedItemId + "@" + position;
                    }
                    selected = false;
                } else {
                    selectedItemId = position + "";
                }
                if (TextUtils.isEmpty(selectedItemId)) {
                    selectSMS_Button.setEnabled(false);
                } else {
                    selectSMS_Button.setEnabled(true);
                }
            }
        });
        selectSMS_Button = (Button) findViewById(R.id.selectSMS_button);
        selectSMS_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!selectedItemId.equals("") && selectedItemId != "" && selectedItemId != null) {
                    Intent intent = new Intent(context, GroupSendActivity.class);
                    if (selectedItemId.contains("@")) {
                        String[] itemid = selectedItemId.split("@");
                        selectedContacts = new String[itemid.length];
                        for (int i = 0; i < itemid.length; i++) {
                            selectedContacts[i] = contactsNamesAndPhoneNo[Integer.parseInt(itemid[i])];
                        }
                    } else {
                        selectedContacts = new String[1];
                        selectedContacts[0] = contactsNamesAndPhoneNo[Integer.parseInt(selectedItemId)];
                    }
                    intent.putExtra("selectedContacts", selectedContacts);
                    intent.putExtra("contactsNames", contactsNames);
                    intent.putExtra("contactsNamesAndPhoneNo", contactsNamesAndPhoneNo);
                    startActivity(intent);
                    windowManager.removeView(overlay);
                    finish();
                }
            }
        });

        overlay = (TextView) View.inflate(this,
                R.layout.overlay, null);
        windowManager = getWindowManager();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT);
        lp.gravity = Gravity.TOP;
        lp.verticalMargin = 0.1f;
        lp.horizontalMargin = 0.1f;
        windowManager.addView(overlay, lp);
        overlay.setVisibility(View.INVISIBLE);

        c_listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            boolean visible;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                visible = true;
                if (scrollState == ListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    overlay.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (visible && contactsNames[firstVisibleItem] != null) {
                    String name = contactsNames[firstVisibleItem]
                            .substring(0, 1);
                    overlay.setText(name);
                    overlay.setVisibility(View.VISIBLE);
                }
            }
        });

        try {
            Field f = AbsListView.class.getDeclaredField("mFastScroller");
            f.setAccessible(true);
            Object o = f.get(c_listView);
            f = f.getType().getDeclaredField("mThumbDrawable");
            f.setAccessible(true);
            Drawable drawable = (Drawable) f.get(o);
            drawable = getResources().getDrawable(R.drawable.scrollbar);
            f.set(o, drawable);
//            Toast.makeText(this, f.getType().getName(), 1000).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        sharedPreferences = context.getSharedPreferences("vsms", 0);
        if (sharedPreferences.getBoolean("showdesc", true)) {
            try {
                new AlertDialog.Builder(context).setTitle(getString(R.string.desc)).setMessage(getString(R.string.desc1) + "\n版本号：" + Tools.getVersionName(context)).setPositiveButton(R.string.dontshowagain, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("showdesc", false);
                        editor.apply();
                    }
                }).setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        windowManager.removeView(overlay);
                        finish();
                    }
                }).create().show();
            } catch (Exception e) {
                Log.e("desc", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    ListView c_listView;
    TextView overlay;
    ContactsAdpter contactsAdpter;
    Context context = MainActivity.this;
    Button selectSMS_Button;
    String[] contactsNames, selectedContacts, contactsNamesAndPhoneNo;
    String selectedItemId = "";
    boolean selected = false;
    //获取联系人数量
    int contactsCount = 0;
    WindowManager windowManager;
    SharedPreferences sharedPreferences;
}
