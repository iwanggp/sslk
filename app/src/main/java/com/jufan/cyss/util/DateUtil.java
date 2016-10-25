package com.jufan.cyss.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by cyjss on 2014/12/23.
 */
public class DateUtil {

    private static final SimpleDateFormat DETAIL_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat DEFAULT_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private static String[] weekDays = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};

    public static String detailDateStr(Date date) {
        return DETAIL_FORMAT.format(date);
    }

    public static Date detailFormat(String time) {
        Date d = null;
        try {
            d = DETAIL_FORMAT.parse(time);
        } catch (Exception e) {

        }
        return d;
    }

    public static Date defaultFormat(String time) {
        Date d = null;
        try {
            d = DEFAULT_FORMAT.parse(time);
        } catch (Exception e) {

        }
        return d;
    }

    public static String getWeekOfDate(Date dt) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0)
            w = 0;
        return weekDays[w];
    }

    public static String getShortTimeDesc(Date date) {
        Date now = new Date();
        long nowTime = now.getTime();
        long time = date.getTime();
        long gap = nowTime - time;

        long minute = 1000 * 60;
        long hour = minute * 60;
        long oneDayTime = 24 * 3600 * 1000;
        long oneWeekTime = oneDayTime * 7;
        long oneMonthTime = oneDayTime * 30;
        long oneYearTime = oneDayTime * 365;

        long yearC = gap / oneYearTime;
        long monthC = gap / oneMonthTime;
        long weekC = gap / (oneWeekTime);
        long dayC = gap / oneDayTime;
        long hourC = gap / hour;
        long minC = gap / minute;
        String result = "";
        if (yearC >= 1) {
            result = (yearC) + "年前";
        } else if (monthC >= 1) {
            result = (monthC) + "个月前";
        } else if (weekC >= 1) {
            result = (weekC) + "周前";
        } else if (dayC >= 1) {
            result = (dayC) + "天前";
        } else if (hourC >= 1) {
            result = (hourC) + "个小时前";
        } else if (minC >= 1) {
            result = (minC) + "分钟前";
        } else
            result = "刚刚";
        return result;
//
//        if (time <= nowTime && time > nowTime - oneDayTime) {
//            return "今天 " + date.getHours() + ":" + date.getMinutes();
//        } else if (time <= nowTime - oneDayTime && time > nowTime - 2 * oneDayTime) {
//            return "昨天 " + date.getHours() + ":" + date.getMinutes();
//        } else if (time <= nowTime - 2 * oneDayTime && time > nowTime - 3 * oneDayTime) {
//            return "前天 " + date.getHours() + ":" + date.getMinutes();
//        } else if (time <= nowTime - 3 * oneDayTime && time > nowTime - 7 * oneDayTime) {
//            long day = (nowTime - time) / oneDayTime;
//            return day + "天前";
//        } else if (time <= nowTime - oneWeekTime && time > nowTime - 2 * oneWeekTime) {
//            return "1周前";
//        } else if (time <= nowTime - 2 * oneWeekTime && time > nowTime - 3 * oneWeekTime) {
//            return "2周前";
//        } else if (time <= nowTime - 3 * oneWeekTime && time > nowTime - 4 * oneWeekTime) {
//            return "3周前";
//        } else if (time <= nowTime - oneMonthTime && time > nowTime - 2 * oneMonthTime) {
//            return "上个月";
//        } else if (time <= nowTime - 2 * oneMonthTime && time > nowTime - 12 * oneMonthTime) {
//            long month = (nowTime - time) / oneMonthTime;
//            return month + "月前";
//        } else if (time <= nowTime - oneYearTime && time > nowTime - 2 * oneYearTime) {
//            return "去年";
//        } else {
//            long year = (nowTime - time) / oneYearTime;
//            return year + "年前";
//        }

//        int nowYear = now.getYear();
//        int nowMonth = now.getMonth();
//        int nowDay = now.getDate();
//        int year = date.getYear();
//        int month = date.getMonth();
//        int day = date.getDate();
//        if (nowYear == year) {
//            if (nowMonth == month) {
//                if (nowDay == day) {
//                    return "今天" + date.getHours() + ":" + date.getMinutes();
//                } else if (nowDay - 1 == day) {
//                    return "昨天" + date.getHours() + ":" + date.getMinutes();
//                } else if (nowDay - 2 == day) {
//                    return "前天" + date.getHours() + ":" + date.getMinutes();
//                } else if (nowDay - 3 >= day && nowDay - day < 8) {
//                    return (nowDay - day) + "天前" + date.getHours() + ":" + date.getMinutes();
//                } else if (nowDay - day >= 8 && nowDay - day < 14) {
//                    return "1周前";
//                } else if (nowDay - day >= 14 && nowDay - day < 21) {
//                    return "2周前";
//                } else if (nowDay - day >= 21 && nowDay - day < 30) {
//                    return "3周前";
//                }
//            } else if (nowMonth - 1 == month) {
//                return "上个月";
//            } else if (nowMonth - 2 >= month && nowMonth - month <= 6) {
//                return (nowMonth - month) + "个月前";
//            } else {
//                return "半年前";
//            }
//        } else {
//            return year - nowYear + "年前";
//        }
//        return "";
    }
}
