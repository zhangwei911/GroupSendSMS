/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package viz.groupsendsms;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import viz.groupsendsms.Adpter.SMSAdpter;
import viz.groupsendsms.Adpter.SelectedAdpter;
import viz.groupsendsms.db.DataBaseOpenHelper;

/**
 * Simple one-activity app that takes a search term via the Action Bar
 * and uses it as a query to search the contacts database via the Contactables
 * table.
 */
public class GroupSendActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gs);


        final ActionBar actionBar = this.getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
            menu = getLayoutInflater().inflate(R.layout.actionbar_overflow, null);
            actionBar.setCustomView(menu,
                    new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.MATCH_PARENT));
            ActionBar.LayoutParams lp = (ActionBar.LayoutParams) menu.getLayoutParams();
            lp.gravity = lp.gravity & ~Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK | Gravity.END;
            actionBar.setCustomView(menu, lp);
            int change = actionBar.getDisplayOptions() ^ ActionBar.DISPLAY_SHOW_CUSTOM;
            actionBar.setDisplayOptions(change, ActionBar.DISPLAY_SHOW_CUSTOM);
        }
        if (getIntent() != null) {
            Intent intent = getIntent();
            selectedContacts = intent.getStringArrayExtra("selectedContacts");
            contactsNames = intent.getStringArrayExtra("contactsNames");
            contactsNamesAndPhoneNo = intent.getStringArrayExtra("contactsNamesAndPhoneNo");
            smsSelect = new int[selectedContacts.length];
            selectedPhoneNo = new String[selectedContacts.length];
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < smsSelect.length; i++) {
                    smsSelect[i] = -1;
                }
            }
        }).start();
        selectedContactsCount = selectedContacts.length;
        selectedAdpter = new SelectedAdpter(context, selectedContactsCount);
        selectedAdpter.selectedContacts = new String[selectedContactsCount];
        for (int i = 0; i < selectedContactsCount; i++) {
            selectedAdpter.selectedContacts[i] = selectedContacts[i].substring(0, selectedContacts[i].indexOf("#"));
//            Log.i("sms",sms[i]);
        }
        selectedAdpter.notifyDataSetChanged();

        sms_ListView = (ListView) findViewById(R.id.sms_listView);
//        String[] sms = getResources().getStringArray(R.array.sms);
        updateSMS();
        sms_ListView.setEnabled(false);
        contacts_ListView = (ListView) findViewById(R.id.contacts_listView);
        contacts_ListView.setAdapter(selectedAdpter);
        contacts_ListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        contacts_ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                sms_ListView.setEnabled(true);
                if (selectedContacts[position].substring(selectedContacts[position].indexOf("#") + 1).contains("@")) {
                    selectedPhoneNos = selectedContacts[position].substring(selectedContacts[position].indexOf("#") + 1).split("@");
                } else {
                    selectedPhoneNos = new String[1];
                    selectedPhoneNo[position] = selectedContacts[position].substring(selectedContacts[position].indexOf("#") + 1);
                    selectedPhoneNos[0] = selectedPhoneNo[0];
                }
                new AlertDialog.Builder(GroupSendActivity.this).setTitle(selectedContacts[position].substring(0, selectedContacts[position].indexOf("#")))
                        .setItems(selectedPhoneNos, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (selectedPhoneNos.length != 1) {
                                    selectedPhoneNo[position] = selectedPhoneNos[which];
                                }
                            }
                        })
                        .create().show();
                selectIndex = position;
                selectedAdpter.setSelectedPosition(position);
                selectedAdpter.notifyDataSetInvalidated();
                smsAdpter.setSelectedPosition(smsSelect[position]);
                smsAdpter.notifyDataSetInvalidated();
                sms_ListView.setSelection(smsSelect[position]);
            }
        });
        contacts_ListView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                menu.add(0, 0, 0, getString(R.string.exchange));
                menu.add(0, 1, 1, getString(R.string.delete));
            }
        });
        contacts_ListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                itemid = position;
                return false;
            }
        });
        contacts_ListView.setSelector(R.drawable.nocolor);


        sms_ListView.setAdapter(smsAdpter);
        sms_ListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        sms_ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                smsAdpter.setSelectedPosition(position);
                smsAdpter.notifyDataSetInvalidated();
                smsSelect[selectIndex] = position;
                itemid2 = position;
                for (int i = 0; i < smsSelect.length; i++) {
                    if (smsSelect[i] == -1 || TextUtils.isEmpty(selectedPhoneNo[i])) {
                        showSendButton = false;
                    }
                }
                if (showSendButton) {
                    send_button.setEnabled(true);
                } else {
                    send_button.setEnabled(false);
                }
                showSendButton = true;
            }
        });
        sms_ListView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                menu.add(0, 2, 2, getString(R.string.delete));
            }
        });
        sms_ListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                itemid1 = position;
//                Log.i("test", position + "");
                return false;
            }
        });
        sms_ListView.setSelector(R.drawable.nocolor);
        send_button = (Button) findViewById(R.id.send_button);
        send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_enter.setVisibility(View.VISIBLE);
                contacts_ListView.setEnabled(false);
                sms_ListView.setEnabled(false);
                send_button.setEnabled(false);
            }
        });
        send_enter = findViewById(R.id.send_enter);
        send_enter.setVisibility(View.GONE);
        send_ok_button = (Button) send_enter.findViewById(R.id.send_ok_button);
        send_cancel_button = (Button) send_enter.findViewById(R.id.send_cancel_button);
        send_ok_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String[] nameAndPhoneNo = new String[selectedContacts.length];
//                for (int i = 0; i < selectedContacts.length; i++) {
//                    nameAndPhoneNo[i] = selectedAdpter.selectedContacts[i] + " " + selectedPhoneNo[i] + "\n" + smsAdpter.smsContent[smsSelect[i]];
//                }
//                new AlertDialog.Builder(context).setTitle("test Group Send SMS").setItems(nameAndPhoneNo, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                    }
//                }).create().show();
                send_enter.setVisibility(View.GONE);
                sending.setVisibility(View.VISIBLE);

                //Log.i("test",selectedContacts.length+"");
                //Log.i("test",selectedPhoneNo.length+"");
                //Log.i("test",smsSelect.length+"");
                //Log.i("test",smsAdpter.smsContent.length+"");
                for (int i = 0; i < selectedContacts.length; i++) {
                    groupSendSMSService = new GroupSendSMSService(context);
                    //Log.i("test",selectedPhoneNo[i]);
                    //Log.i("test",smsSelect[i]+"");
                    //Log.i("test",smsAdpter.smsContent[smsSelect[i]]);
                    groupSendSMSService.send(selectedPhoneNo[i], smsAdpter.smsContent[smsSelect[i]]);
                    GroupSendSMSService.writeSMSIntoSystemDB(context, selectedPhoneNo[i], smsAdpter.smsContent[smsSelect[i]]);
                }
                sending.setVisibility(View.GONE);
                contacts_ListView.setEnabled(true);
                sms_ListView.setEnabled(true);
                send_button.setEnabled(true);
                //showOverlay();
            }
        });
        send_cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contacts_ListView.setEnabled(true);
                sms_ListView.setEnabled(true);
                send_button.setEnabled(true);
                send_enter.setVisibility(View.GONE);
            }
        });
        sending = findViewById(R.id.sending);
        sending.setVisibility(View.GONE);
        sending_textView = (TextView) sending.findViewById(R.id.sending_textView);

        addmessage_include = findViewById(R.id.addmessage_include);
        addmessage_include.setVisibility(View.GONE);
        addmessage_EditText = (EditText) addmessage_include.findViewById(R.id.addmessage_editText);
        addmessage_button = (Button) addmessage_include.findViewById(R.id.addmessage_button);
        cancelmessage_button = (Button) addmessage_include.findViewById(R.id.cancelmessage_button);
        addmessage_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(addmessage_EditText.getText().toString())) {
                    if (ae == 1) {
                        dboh.insertData(db, addmessage_EditText.getText().toString() + splitstr + (smsCount1 != 0 ? (smsid[smsCount - 1] + 1) : 1), (smsCount1 != 0 ? (smsid[smsCount - 1] + 1) : 1) + "");
                        updateSMS();
//                        Log.i("insert", "insert");
                    } else if (ae == 2) {
                        dboh.updateData(db, addmessage_EditText.getText().toString() + splitstr + smsid[itemid2], smsid[itemid2]);
//                        Log.i("update", "update");
                        sms_ListView.setEnabled(true);
                        updateSMS();
                    }
                    showOrHide(0);
                    addmessage_include.setVisibility(View.GONE);
                }
            }
        });
        cancelmessage_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOrHide(0);
                addmessage_include.setVisibility(View.GONE);
            }
        });

        add_contacts_button = (Button) findViewById(R.id.add_contacts_button);
        add_contacts_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToSelectContacts();
            }
        });
    }

    public void ToSelectContacts() {
        Intent intent = new Intent();
        intent.setClass(context, MainActivity.class);
        intent.putExtra("contactsNames", contactsNames);
        intent.putExtra("contactsNamesAndPhoneNo", contactsNamesAndPhoneNo);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();
        MID = (int) info.id;
        switch (item.getItemId()) {
            case 0:
                new AlertDialog.Builder(GroupSendActivity.this).setTitle(getString(R.string.exchange))
                        .setItems(contactsNames, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                selectedContacts[itemid] = contactsNamesAndPhoneNo[which];
                                selectedPhoneNo[itemid] = "";
                                smsSelect[itemid] = 0;
                                sms_ListView.setEnabled(false);
                                selectedAdpter.selectedContacts[itemid] = contactsNames[which];
                                selectedAdpter.notifyDataSetChanged();
                            }
                        })
                        .create().show();
                break;
            case 1:
                selectedContactsCount = selectedContacts.length - 1;
                if (selectedContactsCount == 0) {
                    add_contacts_button.setVisibility(View.VISIBLE);
                    contacts_ListView.setVisibility(View.GONE);
                    send_button.setEnabled(false);
                } else {
                    selectedContactsTemp = new String[selectedContactsCount];
                    selectedPhoneNoTemp = new String[selectedContactsCount];
                    smsSelectTemp = new int[selectedContactsCount];
                    for (int i = 0; i < selectedContacts.length - 1; i++) {
                        if (i >= itemid) {
                            selectedContactsTemp[i] = selectedContacts[i + 1];
                            selectedPhoneNoTemp[i] = selectedPhoneNo[i + 1];
                            smsSelectTemp[i] = smsSelect[i + 1];
                        } else {
                            selectedContactsTemp[i] = selectedContacts[i];
                            selectedPhoneNoTemp[i] = selectedPhoneNo[i];
                            smsSelectTemp[i] = smsSelect[i];
                        }
                    }
                    selectedContacts = new String[selectedContactsCount];
                    selectedPhoneNo = new String[selectedContactsCount];
                    smsSelect = new int[selectedContactsCount];
                    selectedAdpter = new SelectedAdpter(context, selectedContactsCount);
                    selectedAdpter.selectedContacts = new String[selectedContactsCount];
                    for (int i = 0; i < selectedContactsCount; i++) {
                        selectedContacts[i] = selectedContactsTemp[i];
                        selectedPhoneNo[i] = selectedPhoneNoTemp[i];
                        smsSelect[i] = smsSelectTemp[i];
                        selectedAdpter.selectedContacts[i] = selectedContacts[i].substring(0, selectedContacts[i].indexOf("#"));
                    }
                    contacts_ListView.setAdapter(selectedAdpter);
                    selectedAdpter.notifyDataSetChanged();
                }
                break;
            case 2:
                dboh.deleteData(db, smsid[itemid1]);
                if (smsAdpter.getCount() == 0) {
                    send_button.setEnabled(false);
                }
                updateSMS();
                break;
        }
        return super.onContextItemSelected(item);
    }

    private void showOverlay() {
        // Generate a toast view with a special layout that will position itself right
        // on top of this view's interesting widgets.  Sneaky huh?
        SecureViewOverlay overlay = (SecureViewOverlay)
                getLayoutInflater().inflate(R.layout.view_overlay, null);
        overlay.setActivityToSpoof(this);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.FILL, 0, 0);
        toast.setView(overlay);
        toast.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        actionItem = menu.add(0, 1, 1, getString(R.string.menu));
//        actionItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
//        actionItem.setIcon(android.R.drawable.menu_frame);
//        menu.add(0,2,2,getString(R.string.popup_menu_add));
//        menu.add(0,3,3,getString(R.string.popup_menu_edit));
//        menu.add(0,4,4,getString(R.string.exit));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ToSelectContacts();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);// 注意
            intent.addCategory(Intent.CATEGORY_HOME);
            this.startActivity(intent);
            return true;
        } else if (event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
            menu.findViewById(R.id.menuBar).callOnClick();
        }
        return false;
    }

    public void onPopupButtonClick(View button) {
        PopupMenu popup = new PopupMenu(this, button);
        popup.getMenuInflater().inflate(R.menu.popup, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getTitle().equals(getString(R.string.add))) {
                    addmessage_button.setText(getString(R.string.add));
                    addmessage_EditText.setText("");
                    ae = 1;
                    showOrHide(1);
                    addmessage_include.setVisibility(View.VISIBLE);
                } else if (item.getTitle().equals(getString(R.string.edit))) {
                    if (smsSelect[selectIndex] != -1 && smsCount != 0) {
                        sms_ListView.setEnabled(false);
                        addmessage_button.setText(getString(R.string.edit));
                        ae = 2;
                        showOrHide(1);
                        addmessage_include.setVisibility(View.VISIBLE);
                        addmessage_EditText.setText(smsAdpter.smsContent[smsSelect[selectIndex]]);
                    }
                } else if (item.getTitle().equals(getString(R.string.exit))) {
                    finish();
                }
                return true;
            }
        });

        popup.show();
    }

    /**
     * 显示隐藏效果
     *
     * @param lr 0为隐藏，1为显示
     */
    public void showOrHide(int lr) {
        switch (lr) {
            case 0:
                scaleAnimation = new ScaleAnimation(1f, 1f, 1f, 0f);
                scaleAnimation.setDuration(300);
                addmessage_include.setAnimation(scaleAnimation);
                break;
            case 1:
                scaleAnimation = new ScaleAnimation(1f, 1f, 0f, 1f);
                scaleAnimation.setDuration(300);
                addmessage_include.setAnimation(scaleAnimation);
                break;
        }
    }

    public void updateSMS() {
        smsCount1 = 0;
        dboh = new DataBaseOpenHelper(context, "dbinfo");
        db = dboh.getWritableDatabase();
        final String[] sms = dboh.query(db);
        smsCount = sms.length;
        if (!"nodata".equals(sms[0])) {
            smsAdpter = new SMSAdpter(context, smsCount);
            smsAdpter.smsContent = new String[smsCount];
            smsid = new int[smsCount];
            String[] smssplit = new String[2];
            smsCount1 = smsCount;
            for (int i = 0; i < smsCount; i++) {
                smssplit = sms[i].split(splitstr);
                smsAdpter.smsContent[i] = smssplit[0];
                smsid[i] = Integer.parseInt(smssplit[1]);
//                Log.i("sms", smssplit[1]);
            }
        } else {
            smsAdpter = new SMSAdpter(context, 0);
            smsAdpter.smsContent = new String[smsCount];
        }
        sms_ListView.setAdapter(smsAdpter);
        smsAdpter.notifyDataSetChanged();

    }

    SelectedAdpter selectedAdpter;
    SMSAdpter smsAdpter;
    Context context = GroupSendActivity.this;
    ListView contacts_ListView, sms_ListView;
    String[] selectedContacts, selectedContactsTemp, selectedPhoneNo, selectedPhoneNoTemp, selectedPhoneNos, contactsNames, contactsNamesAndPhoneNo;
    int[] smsSelect, smsSelectTemp, smsid;
    Button send_button, send_ok_button, send_cancel_button, addmessage_button, cancelmessage_button, add_contacts_button;
    GroupSendSMSService groupSendSMSService;
    int selectIndex = 0;
    boolean showSendButton = true;
    View send_enter, sending, menu, addmessage_include;
    private MenuItem actionItem;
    ProgressBar sending_progressBar;
    TextView sending_textView;
    EditText addmessage_EditText;
    boolean flags = true;
    int smsCount1 = 0, smsCount = 0, ae = 0, MID = 0, itemid = 0, itemid1 = 0, itemid2 = 0, selectedContactsCount = 0;
    String splitstr = "@viz@";

    DataBaseOpenHelper dboh;
    SQLiteDatabase db;

    ScaleAnimation scaleAnimation;
}
