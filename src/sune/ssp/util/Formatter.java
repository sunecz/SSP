package sune.ssp.util;

import sune.ssp.data.Message;

public class Formatter {
	
	private static String FORMAT_MESSAGE = "%s [%s]: %s";
	public static void setMessageFormat(String format) {
		FORMAT_MESSAGE = format;
	}
	
	public static String formatMessage(Message message) {
		String time 	= message.getDateTime();
		String username = message.getUsername();
		String content  = message.getMessage();
		String dateTime = time == null ?
			DateHelper.getFormattedDate() :
			DateHelper.formatDate(time, DateHelper.PATTERN_DEFAULT);
		return String.format(FORMAT_MESSAGE, dateTime, username, content);
	}
}