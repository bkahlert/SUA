package de.fu_berlin.imp.seqan.usability_analyzer.core.model;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.bkahlert.devel.nebula.utils.CalendarUtils;

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

	private final Calendar calendar;

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
		if (calendar == null) {
			throw new IllegalArgumentException("calendar must not be null");
		}
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
		return CalendarUtils.format(this.getCalendar(), pattern);
	}

	/**
	 * Formats this {@link TimeZoneDate} while considering the time zone
	 * information.
	 * 
	 * @param dateFormat2
	 * @return
	 */
	public String format(DateFormat dateFormat) {
		return CalendarUtils.format(this.getCalendar(), dateFormat);
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
		return CalendarUtils.getLocalTime(this.calendar);
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
		CalendarUtils.addMilliseconds(this.calendar, amount);
		return this;
	}

	public int compareToTimeZoneLess(TimeZoneDate date) {
		return CalendarUtils
				.compareToTimeZoneLess(this.calendar, date.calendar);
	}

	public TimeZoneDate addMilliseconds(int amount) {
		this.calendar.add(Calendar.MILLISECOND, amount);
		return this;
	}

	public boolean before(TimeZoneDate date) {
		return CalendarUtils.before(this.calendar, date.calendar);
	}

	public boolean after(TimeZoneDate date) {
		return CalendarUtils.after(this.calendar, date.calendar);
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
		return CalendarUtils.toShort(this.calendar);
	}

	@Override
	public String toString() {
		return this.toISO8601();
	}

	@Override
	public TimeZoneDate clone() {
		return new TimeZoneDate(new Date(this.calendar.getTime().getTime()),
				TimeZone.getTimeZone(this.calendar.getTimeZone().getID()));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.calendar == null) ? 0 : this.calendar.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		TimeZoneDate other = (TimeZoneDate) obj;
		if (this.calendar == null) {
			if (other.calendar != null) {
				return false;
			}
		} else if (this.compareTo(other) != 0) {
			return false;
		}
		return true;
	}
}
