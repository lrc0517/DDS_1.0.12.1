package com.ai.xiaocai.manager;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import com.ai.xiaocai.utils.LogUtils;
import org.json.JSONObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.content.Context.ALARM_SERVICE;

/**
 * Created by Lucien on 2018/5/16.
 */

public class ReminderManager {

    private static final String TAG = "XC/Remin";
    private final AlarmManager mAlarmManager;

    private Context mContext;
    private ReminderManagerListener mReminderManagerListener;

    private static final int INTERVAL = 1000 * 60 * 60 * 24;// 24h

    public ReminderManager(Context context) {
        this.mContext = context;
        // this.mReminderManagerListener = reminderManagerListener;
        mAlarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);

        Calendar instance = Calendar.getInstance();
        instance.set(Calendar.HOUR_OF_DAY, 8);
        instance.set(Calendar.MINUTE, 0);
        instance.set(Calendar.SECOND, 0);
        //Intent alarmIntent = new Intent(mContext, AlarmclockReceive.class);
        // PendingIntent broadcast = PendingIntent.getBroadcast(mContext, 0, alarmIntent, 0);
        // alarmService.setRepeating(AlarmManager.RTC_WAKEUP, instance.getTimeInMillis(),INTERVAL , broadcast);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //    alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //    alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), pendingIntent);
        } else {
            //    alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), TIME_INTERVAL, pendingIntent);
        }
    }

    private static final int FLAG_DELETE_ALARM_ALL = 0;
    private static final int FLAG_DELETE_ALARM_BY_DAY = 1;
    private static final int FLAG_DELETE_ALARM_BY_HOUR = 2;
    private static final int FLAG_DELETE_ALARM_BY_MINUTES = 3;

    /**
     * "nlu": {
     * "operation": "取消",
     * "time": "20180516",
     * "object": "闹钟",
     * "date": "08:00:00"
     *
     * @param clockJson
     */
    public void conmmandClock(JSONObject clockJson) {

        Intent intent = new Intent("action_speech_reminder");
        intent.putExtra("speech_reminder_delete", true); // delete Alerm need id
        try {
            String date = clockJson.getString("time");
            if (date.matches("[0-9]+")) {
                int[] days = new int[1];

                Date yyyyMMdd = stringToDate(date, "yyyyMMdd");
                days[0] = DateToWeek(yyyyMMdd);
                intent.putExtra("speech_reminder_days", days);
                intent.putExtra("speech_reminder_delete_falg", FLAG_DELETE_ALARM_BY_DAY); // delete Alerm need id
                LogUtils.i("conmmandClock days = " + days);
            } else if (date.contains("<")) {
                String str1 = date.substring(0, date.indexOf("<"));
                String str2 = date.substring(date.indexOf("<") + 1, date.length());
                LogUtils.e(str1 + "++" + str2);

                int i1 = Integer.parseInt(str1);
                int i2 = Integer.parseInt(str2);

                int i = i2 - i1;
                int[] days = new int[i + 1];
                try {
                    for (int j = 0; j < i + 1; j++) {
                        Date yyyyMMdd = stringToDate("" + (i1 + j), "yyyyMMdd");
                        days[j] = DateToWeek(yyyyMMdd);
                    }
                    intent.putExtra("speech_reminder_days", days);
                    intent.putExtra("speech_reminder_delete_falg", FLAG_DELETE_ALARM_BY_DAY); // delete Alerm need id
                } catch (Exception e) {
                    intent.putExtra("speech_reminder_delete_falg", FLAG_DELETE_ALARM_ALL);
                    LogUtils.e(TAG, " getDate Error ", e);
                    e.printStackTrace();
                }
            } else {
                intent.putExtra("speech_reminder_delete_falg", FLAG_DELETE_ALARM_ALL); // delete Alerm need id
            }
        } catch (Exception e) {
            intent.putExtra("speech_reminder_delete_falg", FLAG_DELETE_ALARM_ALL); // delete Alerm need id
            LogUtils.e(TAG, "Delete Clock Error ", e);
            e.printStackTrace();
        }

        try {
            String time = clockJson.getString("date");
            int[] timeInt = formatTurnHourandMinutes(time);
            intent.putExtra("speech_reminder_hour", timeInt[0]); // by hour
            intent.putExtra("speech_reminder_minutes", timeInt[1]);// by minutes
            intent.putExtra("speech_reminder_delete_falg", FLAG_DELETE_ALARM_BY_HOUR); // delete Alerm need id
        } catch (Exception e) {
            LogUtils.e(TAG, "Delete Clock time Error", e);
            e.printStackTrace();
        }
        //int[] days = { Calendar.TUESDAY, Calendar.WEDNESDAY }; // remind days
        intent.setClassName("com.android.deskclock", "com.android.deskclock.DeskClock");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }


    /**
     * object : 闹钟
     * interval : 00:05:00
     * period : 早上
     * date : EVERYDAY W7 M1 20180519<20180520 20180521<20180525
     * repeat : EVERYDAY W7
     * event : 闹钟
     * time : 08:00:00
     */
    public void reminderData() {
        String object = null;//dbdataBean.getObject();
        String repeat = null;//dbdataBean.getRepeat();
        String interval = null;//dbdataBean.getInterval();

        String date = null;//dbdataBean.getDate();
        String time = null;//dbdataBean.getTime();
        String period = null;//dbdataBean.getPeriod();
        LogUtils.i("reminderData object = " + object);
        LogUtils.i("reminderData interval = " + interval);
        LogUtils.i("reminderData repeat = " + repeat);
        LogUtils.i("reminderData date = " + date);
        LogUtils.i("reminderData time = " + time);
        LogUtils.i("reminderData period = " + period);

        boolean rep = repeat != null;

        int inter = formatTurnSecond(interval);
        int[] timeInt = formatTurnHourandMinutes(time);
        int[] days = null;
        if (date.matches("[0-9]+")) {
            days = new int[1];
            try {
                Date yyyyMMdd = stringToDate(date, "yyyyMMdd");
                days[0] = DateToWeek(yyyyMMdd);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            LogUtils.i("reminderData days = " + days);
        } else if (date.contains("<")) {
            String str1 = date.substring(0, date.indexOf("<"));
            String str2 = date.substring(date.indexOf("<") + 1, date.length());
            LogUtils.e(str1 + "++" + str2);

            int i1 = Integer.parseInt(str1);
            int i2 = Integer.parseInt(str2);

            int i = i2 - i1;
            days = new int[i + 1];
            try {
                for (int j = 0; j < i + 1; j++) {
                    Date yyyyMMdd = stringToDate("" + (i1 + j), "yyyyMMdd");
                    days[j] = DateToWeek(yyyyMMdd);
                }
            } catch (Exception e) {
                LogUtils.e(TAG, "getDate Error ",e);
                e.printStackTrace();
            }
        } else if (date.startsWith("W")) {
            if (date.contains("<")) {
                String str = date.replace("W", "");
                String[] s = str.split("<");
                days = new int[]{Integer.parseInt(s[0]), Integer.parseInt(s[1])};
            } else {
                String str = date.replace("W", "");
                days = new int[]{Integer.parseInt(str)};
            }

        } else if (date.startsWith("M")) {

        } else if (date.equals("EVERYDAY")) {
            days = new int[]{1, 2, 3, 4, 5, 6, 7};
        } else {

        }
        LogUtils.i("reminderData: inter = " + inter + ",timeInt = " + timeInt + ",rep = " + rep + ",days " + days);
        setReminder(inter, timeInt[0], timeInt[1], rep, days);
    }

    /**
     * @param date 20180701 20180702<20180706
     * @param time 09:00:00
     */
    public void setSingleAlarm(String date, String time) {
        int[] days = null;
        if (date.matches("[0-9]+")) {
            days = new int[1];
            try {
                Date yyyyMMdd = stringToDate(date, "yyyyMMdd");
                days[0] = DateToWeek(yyyyMMdd);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else if (date.contains("<")) {
            String str1 = date.substring(0, date.indexOf("<"));
            String str2 = date.substring(date.indexOf("<") + 1, date.length());
            LogUtils.e(str1 + "++" + str2);

            int i1 = Integer.parseInt(str1);
            int i2 = Integer.parseInt(str2);

            int i = i2 - i1;
            days = new int[i + 1];
            try {
                for (int j = 0; j < i + 1; j++) {
                    Date yyyyMMdd = stringToDate("" + (i1 + j), "yyyyMMdd");
                    days[j] = DateToWeek(yyyyMMdd);
                }
            } catch (Exception e) {
                LogUtils.w(TAG, "getDate Error ",e);
                e.printStackTrace();
            }
        }
        int[] timeInt = formatTurnHourandMinutes(time);
        setReminder(0, timeInt[0], timeInt[1], false, days);

    }

    /**
     * @param relativeTime 00:05:00
     */
    public void setInterval(String relativeTime) {
        int inter = formatTurnSecond(relativeTime);
        setReminder(inter, 0, 0, false, new int[]{});
    }

    public void setRepeatAlarm(String dayOfWeek, String timeOfDay) {
        int[] days = new int[]{Integer.parseInt(dayOfWeek)};
        int[] timeInt = formatTurnHourandMinutes(timeOfDay);
        setReminder(0, timeInt[0], timeInt[1], true, days);
    }


    private void setReminder(int interval, int hour, int minutes, boolean repeat, int... days) {

        Intent intent = new Intent("action_speech_reminder");
        intent.putExtra("speech_reminder_interval", interval);
        intent.putExtra("speech_reminder_hour", hour);
        intent.putExtra("speech_reminder_minutes", minutes);
        intent.putExtra("speech_reminder_repeat", repeat);
        //int[] days = {Calendar.SUNDAY, Calendar.SATURDAY}; // remind days
        intent.putExtra("speech_reminder_days", days);
        intent.setClassName("com.android.deskclock", "com.android.deskclock.DeskClock");

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);

    }


    /**
     * @param initerval
     * @return minutes
     */
    public int formatTurnSecond(String initerval) {
        if (initerval == null) return 0;
        String s = initerval;
        int index1 = s.indexOf(":");
        int index2 = s.indexOf(":", index1 + 1);
        int hh = Integer.parseInt(s.substring(0, index1));
        int mi = Integer.parseInt(s.substring(index1 + 1, index2));
        int ss = Integer.parseInt(s.substring(index2 + 1));
        int sec = hh * 3600 + mi * 60 + ss;
        return sec;
    }

    /**
     * @param time
     * @return hour minutes seconds
     */
    public int[] formatTurnHourandMinutes(String time) {
        String s = time;
        int index1 = s.indexOf(":");
        int index2 = s.indexOf(":", index1 + 1);
        int hh = Integer.parseInt(s.substring(0, index1));
        int mi = Integer.parseInt(s.substring(index1 + 1, index2));
        int ss = Integer.parseInt(s.substring(index2 + 1));
        return new int[]{hh, mi, ss};
    }

    /**
     * @param strTime
     * @param formatType
     * @return
     * @throws ParseException
     */
    public static Date stringToDate(String strTime, String formatType)
            throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(formatType);
        Date date = null;
        date = formatter.parse(strTime);
        return date;
    }

    public static int DateToWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int dayIndex = calendar.get(Calendar.DAY_OF_WEEK);
        if (dayIndex < 1 || dayIndex > 7) {
            return -1;
        }

        return dayIndex;
    }

    public void stop() {

    }


    public interface ReminderManagerListener {

    }


}
