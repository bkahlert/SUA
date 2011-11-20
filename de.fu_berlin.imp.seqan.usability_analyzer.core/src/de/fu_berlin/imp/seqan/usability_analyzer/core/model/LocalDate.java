package de.fu_berlin.imp.seqan.usability_analyzer.core.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import de.fu_berlin.imp.seqan.usability_analyzer.core.util.DateUtil;

/**
 * In contrast to {@link Date} instances this class...
 * <ol>
 * <li>contains a {@link TimeZone}
 * <li>contains a format method that properly considers the {@link TimeZone}
 * portion
 * </ol>
 * 
 * @author bkahlert
 * 
 */
public class LocalDate implements Comparable<LocalDate> {

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
	 * Constructs a new instance based on a {@link Date}Êand a {@link TimeZone}.
	 * 
	 * @param date
	 *            milliseconds passed since 1.1.1970 00:00:00.000 GMT
	 * @param timeZone
	 */
	public LocalDate(Date date, TimeZone timeZone) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.setTimeZone(timeZone);
		this.calendar = calendar;
	}

	/**
	 * Formats this {@link LocalDate} while considering the time zone
	 * information.
	 * 
	 * @param pattern
	 * @return
	 */
	public String format(String pattern) {
		return this.format(new SimpleDateFormat(pattern));
	}

	/**
	 * Formats this {@link LocalDate} while considering the time zone
	 * information.
	 * 
	 * @param dateFormat2
	 * @return
	 */
	public String format(DateFormat dateFormat) {
		dateFormat.setTimeZone(calendar.getTimeZone());
		return dateFormat.format(this.calendar.getTime());
	}

	/**
	 * Returns the {@link Date) portion without any {@link TimeZone}
	 * information.
	 * 
	 * @return milliseconds passed since 1.1.1970 00:00:00.000 GMT
	 */
	public Date getDate() {
		return this.calendar.getTime();
	}

	/**
	 * Returns the milliseconds passed since 1.1.1970 00:00:00.000 GMT
	 * 
	 * @return
	 */
	public long getTime() {
		return this.getDate().getTime();
	}

	public LocalDate addMilliseconds(Long amount) {
		if (amount != null)
			this.calendar.add(Calendar.MILLISECOND, (int) amount.floatValue());
		return this;
	}

	public LocalDate addMilliseconds(int amount) {
		this.calendar.add(Calendar.MILLISECOND, amount);
		return this;
	}

	@Override
	public int compareTo(LocalDate localDate) {
		return new Long(this.getTime()).compareTo(localDate.getTime());
	}

	public boolean before(LocalDate date) {
		return date.compareTo(this) > 0;
	}

	public boolean after(LocalDate date) {
		return date.compareTo(this) < 0;
	}

	/**
	 * Returns the ISO 8601 representation
	 * <p>
	 * e.g. 1984-05-15T14:30:00+01:00
	 * 
	 * @return
	 */
	public String toISO8601() {
		return DateUtil.toISO8601(this.calendar);
	}

	@Override
	public String toString() {
		return this.toISO8601();
	}

	@Override
	public LocalDate clone() {
		return new LocalDate(new Date(this.calendar.getTime().getTime()),
				TimeZone.getTimeZone(calendar.getTimeZone().getID()));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((calendar == null) ? 0 : calendar.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LocalDate other = (LocalDate) obj;
		if (calendar == null) {
			if (other.calendar != null)
				return false;
		} else if (!calendar.equals(other.calendar))
			return false;
		return true;
	}
}
