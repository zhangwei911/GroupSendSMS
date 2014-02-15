package viz.groupsendsms;

import android.content.AsyncQueryHandler;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.widget.CursorTreeAdapter;

/**
 * Created by vi on 14-2-7.
 */
public class GetContacts {
    private static final String[] CONTACTS_PROJECTION = new String[] {
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME
    };
    private static final int GROUP_ID_COLUMN_INDEX = 0;

    private static final String[] PHONE_NUMBER_PROJECTION = new String[] {
            ContactsContract.CommonDataKinds.Phone._ID,
            ContactsContract.CommonDataKinds.Phone.NUMBER
    };

    private static final int TOKEN_GROUP = 0;
    private static final int TOKEN_CHILD = 1;

}
