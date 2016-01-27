package sune.ssp.util;

public class UnitHelper {
	
	public static String byteToCorrectUnit(long bytes, int decimals) {
		double factor = 1024;
		double value  = bytes;
		int unitType  = 0;
		
		if(value >= 1024.0) {
			while((++unitType < 4) &&
				  (value = bytes / factor) >= 1024) {
				factor *= 1024.0;
			}
		}
		
		return Utils.round(value, decimals) +
			(unitType == 1 ? " kB" :
			 unitType == 2 ? " MB" :
			 unitType == 3 ? " GB" :
			 unitType == 4 ? " TB" : " B");
	}
	
	public static String millisecondsToCorrectFormat(long milliseconds) {
		double _second = 1000;
		double _minute = 60 * _second;
		double _hour   = 60 * _minute;
		double _day    = 24 * _hour;
		
		StringBuilder builder = new StringBuilder();
		double days 	= (milliseconds / _day);
		double hours 	= (milliseconds % _day)    / _hour;
		double minutes 	= (milliseconds % _hour)   / _minute;
		double seconds 	= (milliseconds % _minute) / _second;
		
		if(days 	>= 1) builder.append((int) Utils.round(days, 	0)).append("d ");
		if(hours 	>= 1) builder.append((int) Utils.round(hours, 	0)).append("h ");
		if(minutes 	>= 1) builder.append((int) Utils.round(minutes, 0)).append("m ");
		if(seconds 	>= 1) builder.append((int) Utils.round(seconds, 0)).append("s ");
		builder.append((int) (milliseconds % _second)).append("ms");
		return builder.toString().trim();
	}
}