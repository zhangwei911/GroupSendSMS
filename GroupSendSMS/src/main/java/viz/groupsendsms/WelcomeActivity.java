package viz.groupsendsms;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;

import viz.groupsendsms.Adpter.ContactsAdpter;

/**
 * Created by vi on 14-2-5.
 */
public class WelcomeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);
        new Thread(new Runnable() {
            @Override
            public void run() {
                getContactsNamesAndPhoneNo();

                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra("contactsNames", contactsNames);
                intent.putExtra("contactsNamesAndPhoneNo", contactsNamesAndPhoneNo);
                startActivity(intent);
                finish();
            }
        }).start();

    }


    public void getContactsNamesAndPhoneNo() {
        // 获得所有的联系人
        Cursor cur = getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI,
                null,
                null,
                null,
                ContactsContract.Contacts.DISPLAY_NAME
                        + " COLLATE LOCALIZED ASC");// 获得所有的联系人
        contactsCount = cur.getCount();
        if (contactsCount != 0) {
            // 循环遍历
            if (cur.moveToFirst()) {
                contactsNames = new String[contactsCount];
                contactsNamesAndPhoneNo = new String[contactsCount];
                int idColumn = cur.getColumnIndex(ContactsContract.Contacts._ID);
                int displayNameColumn = cur
                        .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);

                do {
                    // 获得联系人的ID号
                    String contactId = cur.getString(idColumn);
                    // 获得联系人姓名
                    String disPlayName = cur.getString(displayNameColumn);
                    // 查看该联系人有多少个电话号码。如果没有这返回值为0
                    int phoneCount = cur
                            .getInt(cur
                                    .getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
//                Log.i("phoneCount", phoneCount + "");
                    //Log.i("username", disPlayName);
                    if (phoneCount > 0) {
                        // 获得联系人的电话号码
                        Cursor phones = getContentResolver().query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                                        + " = " + contactId, null, null);
                        if (phones.moveToFirst()) {
                            String phoneNoTemp = "";
                            do {
                                // 遍历所有的电话号码
                                String phoneNumber = phones
                                        .getString(phones
                                                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
//                            Log.i("phoneNumber", phoneNumber);
//                            Log.i("phoneType", phoneType);
                                if (phoneNoTemp.equals("")) {
                                    phoneNoTemp = phoneNumber;
                                } else {
                                    phoneNoTemp = phoneNoTemp + "@" + phoneNumber;
                                }
                            } while (phones.moveToNext());
                            contactsNamesAndPhoneNo[count] = disPlayName + "#" + phoneNoTemp;
                        }
                        phones.close();
                    }
                    contactsNames[count] = disPlayName;
                    count++;
                } while (cur.moveToNext());
            }
        }else {
            contactsNames = new String[1];
            contactsNamesAndPhoneNo = new String[1];
            contactsNames[0]="nodata";
            contactsNamesAndPhoneNo[0]="nodata";
        }
        cur.close();
    }

    int count = 0;
    //获取联系人数量
    int contactsCount = 0;
    Context context = WelcomeActivity.this;
    String[] contactsNames, contactsNamesAndPhoneNo;
}
