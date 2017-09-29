package com.ripple.lib.standreminder.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Carrie on 2017/9/29.
 * 时间工具
 */

public class DateUtil {

    /**
     * yyyy/mm/dd
     * @return str
     */
    public static String getTodayDate(){
        SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy/mm/dd",Locale.getDefault());
        return dateFormat.format(new Date());
    }

    /**
     * HH:mm
     */
    public String getCurrentTime(){
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * yyyy/mm/dd HH:mm
     */
    public static String getCurrentDateTime(){
        SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy/mm/dd HH:mm",Locale.getDefault());
        return dateFormat.format(new Date());
    }

}
