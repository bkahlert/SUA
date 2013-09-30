package de.fu_berlin.imp.seqan.usability_analyzer.core.util;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.bkahlert.devel.nebula.utils.CalendarUtils;

public class DateUtil {

	public static Date getDate(int year, int month, int date, int hourOfDay,
			int minute, int second, int milliseconds) {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		calendar.set(year, month, date, hourOfDay, minute, second);
		calendar.set(Calendar.MILLISECOND, milliseconds);
		return calendar.getTime();
	}

	public static Date getDate(int year, int month, int date, int hourOfDay,
			int minute, int second) {
		return getDate(year, month, date, hourOfDay, minute, second, 0);
	}

	/**
	 * Checks wether a given date is the start of the Unix time. The epoch date
	 * is the 1st January 1970 00h 00m 00s 000 milliseconds.
	 * 
	 * @param date
	 * @return true if the date is <code>null</code> or the start of the Unix
	 *         time
	 */
	public static boolean isUnixTimeStart(Date date) {
		return (date == null || date.getTime() == 0l);
	}

	public static String toISO8601(Calendar calendar) {
		return CalendarUtils.toISO8601(calendar);
	}

	public static Calendar fromISO8601(String lexicalRepresentation)
			throws IllegalArgumentException {
		return CalendarUtils.fromISO8601(lexicalRepresentation);
	}
}
