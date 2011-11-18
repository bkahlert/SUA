package de.fu_berlin.imp.seqan.usability_analyzer.core.model;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import de.fu_berlin.imp.seqan.usability_analyzer.core.util.DateUtil;

/**
 * In contrast to {@link Date} instances of this class:
 * <ol>
 * <li>contain a {@link TimeZone}
 * <li>contain the milliseconds passed since 1.1.1970 00:00:00 +time zone
 * </ol>
 * 
 * @author bkahlert
 * 
 */
public class LocalDate {

	private Calendar calendar;

	/**
	 * Constructs a new instance based on a ISO8601 date representation.
	 * <p>
	 * e.g. 1984-05-15T14:30:00+01:00
	 * 
	 * @param lexicalRepresentation
	 */
	public LocalDate(String lexicalRepresentation) {
		this.calendar = DateUtil.fromISO8601(lexicalRepresentation);
	}

	/**
	 * Formats this {@link LocalDate} while considering the time zone
	 * information.
	 * 
	 * @param pattern
	 * @return
	 */
	public String format(String pattern) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
		dateFormat.setTimeZone(calendar.getTimeZone());
		return dateFormat.format(this.calendar.getTime());
	}
}
