package sune.ssp.util;

import java.time.LocalDate;
import java.time.LocalTime;

public class DateHelper {
	
	public static final String PATTERN_DEFAULT = "D.M.Y h:m:s";
	
	public static String getCurrentDate() {
		LocalDate date = LocalDate.now();
		LocalTime time = LocalTime.now();
		StringBuilder builder = new StringBuilder();
		builder.append(formatNumber(date.getYear(), 4)).append("-");
		builder.append(formatNumber(date.getMonthValue(), 2)).append("-");
		builder.append(formatNumber(date.getDayOfMonth(), 2)).append("-");
		builder.append(formatNumber(time.getHour(), 2)).append("-");
		builder.append(formatNumber(time.getMinute(), 2)).append("-");
		builder.append(formatNumber(time.getSecond(), 2));
		return builder.toString();
	}
	
	public static String getFormattedDate() {
		return formatDate(getCurrentDate(), PATTERN_DEFAULT);
	}

	/*
	 * Format patterns:
	 *   Y - for year
	 *   M - for month
	 *   D - for day
	 *   h - for hours
	 *   m - for minutes
	 *   s - for seconds*/
	public static String formatDate(String unformattedDate, String format) {
		// Format: year-month-day-hours-minutes-seconds
		String[] splitUFDate = unformattedDate.split("-");
		if(splitUFDate.length != 6) return "";
		
		String formattedDate = format;
		String[] patterns 	 = { "Y", "M", "D", "h", "m", "s" };
		
		int l = Math.min(patterns.length, splitUFDate.length);
		for(int i = 0; i < l; ++i)
			formattedDate = formattedDate.replaceAll(
				patterns[i], splitUFDate[i]);
		return formattedDate;
	}
	
	private static String formatNumber(int number, int zeros) {
		String strZeros  = repeatString("0", zeros);
		String strNumber = Integer.toString(number);
		return new StringBuilder().append(
			strZeros.substring(0, zeros-strNumber.length()))
			.append(strNumber).toString();
	}
	
	private static String repeatString(String string, int times) {
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < times; ++i)
			builder.append(string);
		return builder.toString();
	}
}