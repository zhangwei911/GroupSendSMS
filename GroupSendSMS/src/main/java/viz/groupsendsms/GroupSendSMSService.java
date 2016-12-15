package viz.groupsendsms;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.widget.Toast;

import java.util.List;

/**
 * Created by vi on 14-2-5.
 */
public class GroupSendSMSService {
    public GroupSendSMSService() {
    }

    public GroupSendSMSService(Context context) {
        mContext = context;
        smsManager = SmsManager.getDefault();
        // create the sentIntent parameter
        Intent sentIntent = new Intent(SMS_SEND_ACTIOIN);
        sentPI = PendingIntent.getBroadcast(mContext, 0, sentIntent, 0);

        // create the deilverIntent parameter
        Intent deliverIntent = new Intent(SMS_DELIVERED_ACTION);
        deliverPI = PendingIntent.getBroadcast(mContext, 0,
                deliverIntent, 0);
    }

    /**
     * 发送短信
     *
     * @param phoneNo 手机号
     * @param SMS     短信内容
     */
    public static void send(String phoneNo, String SMS) {
        try {
            smsManager.sendTextMessage(phoneNo, null, SMS, sentPI,
                    deliverPI);
        }catch (Exception e){
            Toast.makeText(mContext,mContext.getString(R.string.wrongNo),Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    /**
     * 群发短信
     *
     * @param phoneNos 手机号
     * @param SMSs     短信内容
     */
    public void groupsend(String[] phoneNos, String[] SMSs) {
        for (int i = 0; i < phoneNos.length; i++) {
            smsManager.sendTextMessage(phoneNos[i], null, SMSs[i], sentPI,
                    deliverPI);
        }
    }

    public static void sendsms(Context context, String phoneNo, String smsMessage) {
        SmsManager sms = SmsManager.getDefault();

        List<String> messages = sms.divideMessage(smsMessage);

        for (String message : messages) {
            sms.sendTextMessage(phoneNo, null, message, PendingIntent.getBroadcast(
                    context, 0, new Intent(ACTION_SMS_SENT), 0), null);
        }
    }

    public static void writeSMSIntoSystemDB(Context context, String phoneNo, String smsMessage) {
        /** 手机号码 与输入内容 必需不为空 **/
        if (!TextUtils.isEmpty(phoneNo) && !TextUtils.isEmpty(smsMessage)) {
            /**将发送的短信插入数据库**/
            ContentValues values = new ContentValues();
            //发送时间
            values.put("date", System.currentTimeMillis());
            //阅读状态
            values.put("read", 0);
            //1为收 2为发
            values.put("type", 2);
            //送达号码
            values.put("address", phoneNo);
            //送达内容
            values.put("body", smsMessage);
            //插入短信库
            context.getContentResolver().insert(Uri.parse("content://sms"), values);

        }
    }

    static Context mContext;
    static PendingIntent sentPI, deliverPI;
    static SmsManager smsManager;
    /* 自定义ACTION常数，作为广播的Intent Filter识别常数 */
    protected String SMS_SEND_ACTIOIN = "SMS_SEND_ACTIOIN";
    protected String SMS_DELIVERED_ACTION = "SMS_DELIVERED_ACTION";
    public static final String ACTION_SMS_SENT = "viz.groupsendsms.SMS_SENT_ACTION";

}
