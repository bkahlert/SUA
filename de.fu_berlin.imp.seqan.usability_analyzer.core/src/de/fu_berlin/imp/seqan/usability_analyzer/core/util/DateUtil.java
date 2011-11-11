package de.fu_berlin.imp.seqan.usability_analyzer.core.util;

import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateUtil {
	public static Date getDate(int year, int month, int date, int hourOfDay,
			int minute, int second, int milliseconds) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(year, month, date, hourOfDay, minute, second);
		calendar.set(Calendar.MILLISECOND, milliseconds);
		return calendar.getTime();
	}

	public static Date getDate(int year, int month, int date, int hourOfDay,
			int minute, int second) {
		return getDate(year, month, date, hourOfDay, minute, second, 0);
	}

	/**
	 * Transforms a date representation of with a nanoseconds portion to one
	 * with a milliseconds portion.
	 * 
	 * @param dateString
	 * @return returns null if the parameter is not parseable
	 */
	public static String nanoDateStringToMilliDateString(String dateString) {
		Pattern pattern = Pattern.compile(".(\\d{9}) ");
		Matcher matcher = pattern.matcher(dateString);
		if (!matcher.find()) {
			return null;
		}
		String nanoseconds = matcher.group(1);
		String fractionalMilliseconds = nanoseconds.substring(0, 4);
		int milliseconds = (int) Math.round(Double.parseDouble(fractionalMilliseconds) / 10);
		return matcher.replaceFirst("." + milliseconds + " ");
	}
}
