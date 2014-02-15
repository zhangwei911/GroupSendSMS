package viz.groupsendsms.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wei on 13-9-23.
 */

public class DataBaseOpenHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DICTIONARY_TABLE_NAME = "sms_info";
    private static final String SMS_MESSAGE = "sms_message";
    private static final String SMS_ID = "sms_id";
    private static final String SMS_VARCHAR = " varchar(280) not null";
    private static final String VARCHAR = " varchar(30) not null";
    private static final String ID = "id INTEGER PRIMARY KEY AUTOINCREMENT";
    private static final String COMMA = ",";
    private static final String DICTIONARY_TABLE_CREATE = "CREATE TABLE "
            + DICTIONARY_TABLE_NAME + " (" + ID + COMMA
            + SMS_MESSAGE + SMS_VARCHAR + COMMA + SMS_ID + VARCHAR
            + ");";

    public DataBaseOpenHelper(Context context, String name,
                              SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public DataBaseOpenHelper(Context context, String name, int version) {
        this(context, name, null, version);
    }

    public DataBaseOpenHelper(Context context, String name) {
        this(context, name, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DICTIONARY_TABLE_CREATE);
        // sqLiteDatabase.execSQL(insertData("123","456","789"));
//        Log.i("TestSQLite", "create database success!");
    }

    public void insertData(SQLiteDatabase db, String sms_message, String sms_id) {
//        Log.e("viz.child.errors", "insert");
        String result = "nodata";
        if (!TextUtils.isEmpty(sms_message)) {
            result = "insert into " + DICTIONARY_TABLE_NAME
                    + "(sms_message,sms_id) values('" + sms_message + "','" + sms_id + "');";
        }
        db.execSQL(result);
    }

    public int deleteData(SQLiteDatabase db, int sms_id) {
        return db.delete("sms_info", "sms_id ='" + sms_id + "'",
                null);
    }

    public int updateData(SQLiteDatabase db, String sms_message, int sms_id) {
        ContentValues cv = new ContentValues();
        cv.put("sms_message", sms_message);
        return db.update("sms_info", cv, "sms_id='"
                + sms_id + "'", null);
    }

    public String[] query(SQLiteDatabase db) {
        String[] datas = null;
        Cursor cursor = db.query("sms_info", new String[]{"sms_message"}, null, null, null, null, null);
        int countData = 0;
        while (cursor.moveToNext()) {
            countData++;
        }
        if (countData != 0) {
            datas = new String[countData];
            int gettempcount = 0;
            Cursor cursor1 = db.query("sms_info", new String[]{"sms_message"}, null, null, null, null, null);
            while (cursor1.moveToNext()) {
                datas[gettempcount] = cursor1.getString(cursor
                        .getColumnIndex("sms_message"));
                gettempcount++;
            }
        } else {
            datas = new String[1];
            datas[0] = "nodata";
        }
        return datas;
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

    }
}
