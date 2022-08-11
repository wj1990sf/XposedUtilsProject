package net.dalu2048.wechatgenius.util;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * 时间工具类.
 */

public class TimeUtils {
    private static final String TAG = TimeUtils.class.getName();
    public static final SimpleDateFormat SDF_ALL = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final SimpleDateFormat SDF_YMD = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat SDF_YMD2 = new SimpleDateFormat("yyyy/MM/dd");
    public static final SimpleDateFormat SDF_YMDHM2 = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    public static final SimpleDateFormat SDF_YMHM = new SimpleDateFormat("MM/dd HH:mm");
    public static final SimpleDateFormat SDF_YMDHMS = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    public static final SimpleDateFormat SDF_MDHM = new SimpleDateFormat("MM-dd HH:mm");
    public static final SimpleDateFormat SDF_HMS = new SimpleDateFormat("HH:mm:ss");
    public static final SimpleDateFormat SDF_HMSSSS = new SimpleDateFormat("HH:mm:ss:SSS");
    public static final SimpleDateFormat SDF_YM = new SimpleDateFormat("yyyy-MM");
    public static final SimpleDateFormat SDF_HM = new SimpleDateFormat("HH:mm");
    public static final SimpleDateFormat SDF_YMDHM = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    public static final SimpleDateFormat SDF_YMDHM_CN = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
    public static final SimpleDateFormat SDF_MDHM_CN = new SimpleDateFormat("MM月dd日 HH:mm");
    public static final SimpleDateFormat SDF_MD_CN = new SimpleDateFormat("MM月dd日");

    public static final String TIME_STAMP_PARSE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final SimpleDateFormat TIME_STAMP_PARSE_FORMAT_DOT = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

    public static final String TIMEZONE_ASIA_SHANGHAI = "Asia/Shanghai";
    public static final long MILLION_SEC = 1000L;
    public static final long ONE_DAY_IN_MILSEC = 24 * 60 * 60 * MILLION_SEC;

    @SuppressLint("SimpleDateFormat")
    public static String getTime() {
        return getTime("yyyy-MM-dd HH:mm:ss", null);
    }

    public static String getTime(Date date) {
        return getTime("yyyy-MM-dd HH:mm:ss", date);
    }


    public static String getTimeStampParseFormatDot(long timeMilsec) {
        Calendar calender = Calendar.getInstance();
        calender.setTimeInMillis(timeMilsec);
        return TIME_STAMP_PARSE_FORMAT_DOT.format(calender.getTime());
    }

    public static String getTime(long timeMilsec) {
        Calendar calender = Calendar.getInstance();
        calender.setTimeInMillis(timeMilsec);
        return getTime(calender);
    }

    public static String getTime(String formatter, Date data) {
        SimpleDateFormat yymmdd = new SimpleDateFormat(formatter);
        Calendar calender = Calendar.getInstance();
        if (data != null) {
            calender.setTime(data);
        }
        //服务器传的时间没有时区，约定为北京时间，这里把本地时间转为北京时间
        yymmdd.setTimeZone(TimeZone.getTimeZone(TIMEZONE_ASIA_SHANGHAI));
        String yyMmDd = yymmdd.format(calender.getTime());
        return yyMmDd;
    }

    public static long str2Long(String time) {
        return str2Long(SDF_ALL, time);
    }

    public static long str2Long(SimpleDateFormat sdf, String time) {
        if (TextUtils.isEmpty(time)) {
            return 0;
        }
        try {
            return sdf.parse(time).getTime();
        } catch (ParseException e) {
            //LogUtils.e(TAG, e.toString());
        }
        return 0;
    }

    public static Date str2Date(String time) {
        return new Date(str2Long(time));
    }

    public static int getMonth(String time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(str2Long(time));
        return calendar.get(Calendar.MONTH);
    }

    public static int getYear(String time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(str2Long(time));
        return calendar.get(Calendar.YEAR);
    }

    public static int getMonth(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return calendar.get(Calendar.MONTH);
    }

    public static int getMinutes(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return calendar.get(Calendar.MINUTE);
    }

    public static int getYear(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return calendar.get(Calendar.YEAR);
    }

    public static int getHour(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    public static String getDate(SimpleDateFormat format, long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return format.format(calendar.getTime());
    }

    public static String getDate(SimpleDateFormat format, String time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(str2Long(time));
        return format.format(calendar.getTime());
    }

    public static boolean isValidDate(String str) {
        return str2Long(str) <= 0;
    }

    public static boolean isValidDate(String str, SimpleDateFormat formatter) {
        return str2Long(formatter, str) > 0;
    }

    public static String getTime(Calendar calendar) {
        String yyMmDd = SDF_ALL.format(calendar.getTime());
        return yyMmDd;
    }

    /**
     * 将yyyy-MM-dd HH:mm:ss格式转化成指定格式
     *
     * @param format
     * @param time
     * @return
     */
    public static String getTime(SimpleDateFormat format, String time) {
        long timeLong = str2Long(time);
        if (timeLong > 1) {
            return format.format(str2Long(time));
        } else {
            return "";
        }
    }

    /**
     * 返回准确到秒的时间
     *
     * @param time
     * @return
     */
    public static long getTimeToSeconds(long time) {
        String date = getDate(SDF_ALL, time);
        return str2Long(date) / 1000;
    }

    public static String getTime(SimpleDateFormat format, long time) {
        return format.format(time);
    }

    private static int getTotalHours(long time) {
        return (int) (getTotalMinutes(time) / 60);
    }

    private static int getTotalMinutes(long time) {
        return (int) (time / 1000 / 60);
    }

    public static String changeFormatter(SimpleDateFormat oldFormat, SimpleDateFormat newFormat, String date) {
        return getDate(newFormat, str2Long(oldFormat, date));
    }
    /**
     * 当天凌晨时间戳
     *
     * @return
     */
    public static long getCurEndTimestamp() {
        long current=System.currentTimeMillis();//当前时间毫秒数
        long zero=current/(1000*3600*24)*(1000*3600*24)- TimeZone.getDefault().getRawOffset();//今天零点零分零秒的毫秒数
        return zero;
    }
}
