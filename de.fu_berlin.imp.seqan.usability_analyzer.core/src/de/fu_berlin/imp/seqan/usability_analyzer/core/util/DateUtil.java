package de.fu_berlin.imp.seqan.usability_analyzer.core.util;

import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.apache.commons.lang.StringUtils;

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

	/**
	 * Transforms a date representation of with a nanoseconds portion to one
	 * with a milliseconds portion.
	 * 
	 * @param dateString
	 * @return an array of two elements
	 *         <ol>
	 *         <li>converted date string</li>
	 *         <li>a boolean that is true iff an overflow occurred; you have to
	 *         add a millisecond to the converted date</li>
	 *         </ol>
	 */
	public static Object[] nanoDateStringToMilliDateString(String dateString) {
		Pattern pattern = Pattern.compile("\\.(\\d{9}) ");
		Matcher matcher = pattern.matcher(dateString);
		if (!matcher.find()) {
			throw new InvalidParameterException(
					"The nano portion of the date string could not be founded. "
							+ "Please make sure that is consists of exactly 9 digits.");
		}
		String nanoseconds = matcher.group(1);
		String fractionalMilliseconds = nanoseconds.substring(0, 4);
		Double milliseconds = Double.parseDouble(fractionalMilliseconds) / 10;
		Long roundedMilliseconds = Math.round(milliseconds);
		String millisecondsString = roundedMilliseconds.toString();
		boolean overflow = millisecondsString.length() > 3;
		String filledMillisecondsString = StringUtils.repeat("0",
				3 - millisecondsString.length())
				+ ((overflow) ? millisecondsString.substring(millisecondsString
						.length() - 3) : millisecondsString);
		return new Object[] {
				matcher.replaceFirst("." + filledMillisecondsString + " "),
				overflow };
	}

	public static String toISO8601(Calendar calendar) {
		SimpleDateFormat iso8601 = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ssz");
		iso8601.setTimeZone(calendar.getTimeZone());
		return iso8601.format(calendar.getTime()).replace("GMT", "");
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
