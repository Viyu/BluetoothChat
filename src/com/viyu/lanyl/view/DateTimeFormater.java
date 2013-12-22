package com.viyu.lanyl.view;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import android.text.format.Time;

/**
 * @author Viyu_Lu
 * 统一的时间管理类
 */

public class DateTimeFormater {
	
	private DateFormat formater = null;
	
	private DateTimeFormater() {
		formater = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.getDefault());
	}
	
	public static DateTimeFormater getInstance() {
		return InstanceHolder.instance;
	}
	
	private static class InstanceHolder {
		private static DateTimeFormater instance = new DateTimeFormater();
	}
	
	public String format(long millis) {
		String str = formater.format(new Date(millis));
		return str;
	}
	
	public String getFormatedNowTime() {
		Time time = new Time("GMT+8");    
        time.setToNow(); 
        return format(time.toMillis(true));
	}
}