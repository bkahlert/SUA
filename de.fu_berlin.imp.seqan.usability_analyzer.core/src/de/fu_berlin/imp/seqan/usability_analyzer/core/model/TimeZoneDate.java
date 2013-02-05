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
 * TODO extends Calendar
 * 
 * @author bkahlert
 * 
 */
public class TimeZoneDate implements Comparable<TimeZoneDate> {

	private Calendar calendar;

	/**
	 * Constructs a new instance that describes this very moment (now).
	 */
	public TimeZoneDate() throws IllegalArgumentException {
		this(new Date(), TimeZone.getDefault());
	}

	/**
	 * Constructs a new instance based on a ISO8601 date representation.
	 * <p>
	 * e.g. 1984-05-15T14:30:00+01:00
	 * 
	 * @param lexicalRepresentation
	 */
	public TimeZoneDate(String lexicalRepresentation)
			throws IllegalArgumentException {
		this.calendar = DateUtil.fromISO8601(lexicalRepresentation);
	}

	/**
	 * Constructs a new instance based on a {@link Date} and a {@link TimeZone}.
	 * 
	 * @param date
	 *            milliseconds passed since 1.1.1970 00:00:00.000 GMT
	 * @param timeZone
	 */
	public TimeZoneDate(Date date, TimeZone timeZone) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.setTimeZone(timeZone);
		this.calendar = calendar;
	}

	/**
	 * Constructs a new instance based on a {@link Calendar}.
	 * 
	 * @param calendar
	 * @param timeZone
	 */
	public TimeZoneDate(Calendar calendar) {
		if (calendar == null)
			throw new IllegalArgumentException("calendar must not be null");
		this.calendar = calendar;
	}

	/**
	 * Formats this {@link TimeZoneDate} while considering the time zone
	 * information.
	 * 
	 * @param pattern
	 * @return
	 */
	public String format(String pattern) {
		return this.format(new SimpleDateFormat(pattern));
	}

	/**
	 * Formats this {@link TimeZoneDate} while considering the time zone
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
	 * Returns an equivalent {@link Calendar} describing the same moment in
	 * time.
	 * 
	 * @return
	 */
	public Calendar getCalendar() {
		return (Calendar) this.calendar.clone();
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
		return this.calendar.getTimeInMillis();
	}

	public void setTime(long millis) {
		this.calendar.setTimeInMillis(millis);
	}

	@Override
	public int compareTo(TimeZoneDate date) {
		assert date != null;
		long time = this.getTime();
		long otherTime = date.getTime();
		return new Long(time).compareTo(otherTime);
	}

	/**
	 * Return the milliseconds passed since 1.1.1970 00:00:00.000 [TimeZone]
	 * 
	 * @return
	 */
	public long getLocalTime() {
		return calendar.getTimeInMillis()
				+ calendar.getTimeZone().getOffset(calendar.getTimeInMillis());
	}

	/**
	 * Returns the {@link TimeZone} of the associated date.
	 * 
	 * @return
	 */
	public TimeZone getTimeZone() {
		return this.calendar.getTimeZone();
	}

	public TimeZoneDate addMilliseconds(Long amount) {
		if (amount != null)
			this.calendar.add(Calendar.MILLISECOND, (int) amount.floatValue());
		return this;
	}

	public int compareToTimeZoneLess(TimeZoneDate date) {
		long localTime = this.getLocalTime();
		long otherLocalTime = date.getLocalTime();
		return new Long(localTime).compareTo(otherLocalTime);
	}

	public TimeZoneDate addMilliseconds(int amount) {
		this.calendar.add(Calendar.MILLISECOND, amount);
		return this;
	}

	public boolean before(TimeZoneDate date) {
		return date.compareTo(this) > 0;
	}

	public boolean after(TimeZoneDate date) {
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

	public String toShort() {
		return DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
				DateFormat.SHORT).format(this.calendar.getTime());
	}

	@Override
	public String toString() {
		return this.toISO8601();
	}

	@Override
	public TimeZoneDate clone() {
		return new TimeZoneDate(new Date(this.calendar.getTime().getTime()),
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
		TimeZoneDate other = (TimeZoneDate) obj;
		if (calendar == null) {
			if (other.calendar != null)
				return false;
		} else if (this.compareTo(other) != 0)
			return false;
		return true;
	}
}
