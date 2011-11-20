package de.fu_berlin.imp.seqan.usability_analyzer.core.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

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
		SimpleDateFormat iso8601 = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ssZ");
		iso8601.setTimeZone(calendar.getTimeZone());
		String missingDots = iso8601.format(calendar.getTime()).replace("GMT",
				"");
		return missingDots.substring(0, missingDots.length() - 2) + ":"
				+ missingDots.substring(missingDots.length() - 2);
	}

	public static Calendar fromISO8601(String lexicalRepresentation) {
		try {
			return DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(lexicalRepresentation)
					.toGregorianCalendar();
		} catch (DatatypeConfigurationException e) {
			return null;
		}
	}
}
