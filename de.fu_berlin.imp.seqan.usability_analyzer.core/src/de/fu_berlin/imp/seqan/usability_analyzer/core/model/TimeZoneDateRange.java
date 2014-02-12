package de.fu_berlin.imp.seqan.usability_analyzer.core.model;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.bkahlert.nebula.datetime.CalendarRange;

import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;

/**
 * This class describes a range defined by two {@link TimeZoneDate}s. To define
 * a unbounded range, simply pass <code>null</code> as one argument.
 * 
 * @author bkahlert
 */
public class TimeZoneDateRange implements Comparable<TimeZoneDateRange> {

	public static Pattern DURATION_SHORTENER = Pattern
			.compile("0+\\s*[A-Za-z]*");

	public static TimeZoneDateRange calculateOuterDateRange(
			TimeZoneDateRange... dateRanges) {
		TimeZoneDate earliestDate = null;
		TimeZoneDate latestDate = null;

		for (TimeZoneDateRange dateRange : dateRanges) {
			if (dateRange == null) {
				continue;
			}
			if (dateRange.getStartDate() != null
					&& (earliestDate == null || earliestDate
							.compareTo(dateRange.getStartDate()) > 0)) {
				earliestDate = dateRange.getStartDate();
			}
			if (dateRange.getEndDate() != null
					&& (latestDate == null || latestDate.compareTo(dateRange
							.getEndDate()) < 0)) {
				latestDate = dateRange.getEndDate();
			}
		}

		return new TimeZoneDateRange(earliestDate, latestDate);
	}

	public static TimeZoneDateRange calculateOuterDateRange(
			List<? extends HasDateRange> hasDateRanges) {
		List<TimeZoneDateRange> dateRanges = new LinkedList<TimeZoneDateRange>();
		for (HasDateRange hasDateRange : hasDateRanges) {
			dateRanges.add(hasDateRange.getDateRange());
		}
		return calculateOuterDateRange(dateRanges
				.toArray(new TimeZoneDateRange[0]));
	}

	public static TimeZoneDateRange calculateOuterDateRange(
			HasDateRange... hasDateRanges) {
		List<TimeZoneDateRange> dateRanges = new LinkedList<TimeZoneDateRange>();
		for (HasDateRange hasDateRange : hasDateRanges) {
			dateRanges.add(hasDateRange.getDateRange());
		}
		return calculateOuterDateRange(dateRanges
				.toArray(new TimeZoneDateRange[0]));
	}

	private final TimeZoneDate startDate;
	private final TimeZoneDate endDate;

	public TimeZoneDateRange(TimeZoneDate startDate, TimeZoneDate endDate) {
		super();
		if (startDate != null && endDate != null
				&& startDate.compareTo(endDate) > 0) {
			throw new InvalidParameterException(
					"start date must be before or on the end date");
		}
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public TimeZoneDateRange(CalendarRange calendarRange) {
		this(new TimeZoneDate(calendarRange.getStartDate()), new TimeZoneDate(
				calendarRange.getEndDate()));
	}

	public TimeZoneDate getStartDate() {
		return this.startDate;
	}

	public TimeZoneDate getEndDate() {
		return this.endDate;
	}

	public CalendarRange getCalendarRange() {
		return new CalendarRange(
				this.startDate != null ? this.startDate.getCalendar() : null,
				this.endDate != null ? this.endDate.getCalendar() : null);
	}

	public Long getDifference() {
		if (this.startDate == null) {
			return null;
		}
		if (this.endDate == null) {
			return null;
		}
		return this.endDate.getTime() - this.startDate.getTime();
	}

	public boolean isInRange(long time) {
		return (this.startDate == null || this.startDate.getTime() <= time)
				&& (this.endDate == null || time <= this.endDate.getTime());
	}

	public boolean isInRange(TimeZoneDate date) {
		if (date == null) {
			return false;
		}
		return this.isInRange(date.getTime());
	}

	public boolean isBeforeRange(long time) {
		if (this.startDate == null) {
			return false;
		} else {
			return time < this.startDate.getTime();
		}
	}

	public boolean isBeforeRange(TimeZoneDate date) {
		if (date == null) {
			return false;
		}
		return this.isBeforeRange(date.getTime());
	}

	public boolean isAfterRange(long time) {
		if (this.endDate == null) {
			return false;
		} else {
			return time > this.endDate.getTime();
		}
	}

	public boolean isAfterRange(TimeZoneDate date) {
		if (date == null) {
			return false;
		}
		return this.isAfterRange(date.getTime());
	}

	/**
	 * Returns true if the given {@link TimeZoneDateRange} intersects the
	 * current {@link TimeZoneDateRange}.
	 * 
	 * @param dateRange
	 * @return
	 */
	public boolean isIntersected(TimeZoneDateRange dateRange) {
		if (dateRange == null) {
			return true;
		}

		boolean startAndEndBeforeRange = this.isBeforeRange(dateRange
				.getStartDate()) && this.isBeforeRange(dateRange.getEndDate());
		boolean startAndEndAfterRange = this.isAfterRange(dateRange
				.getStartDate()) && this.isAfterRange(dateRange.getEndDate());
		return !(startAndEndBeforeRange || startAndEndAfterRange);
	}

	/**
	 * Returns true if the given {@link TimeZoneDateRange} intersects the
	 * current {@link TimeZoneDateRange}.
	 * <p>
	 * In contrast to {@link #isIntersected(TimeZoneDateRange)} this method does
	 * not count exact matches as intersected. This means the the case in which
	 * one {@link TimeZoneDateRange} ends at the very moment the second one
	 * starts is not considered intersected.
	 * 
	 * @param dateRange
	 * @return
	 */
	public boolean isIntersected2(TimeZoneDateRange dateRange) {
		if (!this.isIntersected(dateRange)) {
			return false;
		}
		boolean areNeighbors1 = this.endDate != null
				&& dateRange.getStartDate() != null
				&& this.endDate.equals(dateRange.getStartDate());
		boolean areNeighbors2 = this.startDate != null
				&& dateRange.getEndDate() != null
				&& this.startDate.equals(dateRange.endDate);
		return !areNeighbors1 && !areNeighbors2;
	}

	@Override
	public int compareTo(TimeZoneDateRange o) {
		TimeZoneDate t1 = this.getStartDate();
		if (t1 == null) {
			t1 = this.getEndDate();
		}

		TimeZoneDate t2 = o.getStartDate();
		if (t2 == null) {
			t2 = o.getEndDate();
		}

		if (t1 == null && t2 == null) {
			return 0;
		}
		if (t1 != null && t2 == null) {
			return +1;
		}
		if (t1 == null && t2 != null) {
			return -1;
		}
		return t1.compareTo(t2);
	}

	public String formatDuration() {
		String duration = this.getCalendarRange().formatDuration(
				new SUACorePreferenceUtil().getTimeDifferenceFormat());
		List<String> parts = new LinkedList<String>(Arrays.asList(duration
				.split(" ")));
		for (Iterator<String> it = parts.iterator(); it.hasNext();) {
			String part = it.next();
			// remove parts only consisting of zeros
			if (DURATION_SHORTENER.matcher(part).matches()) {
				it.remove();
			} else {
				break;
			}
		}
		// invariant: no leading parts that are only zero
		if (parts.size() > 0) {
			// remove leading zeros
			parts.set(0, parts.get(0).replaceAll("^0+", ""));
		}
		return StringUtils.join(parts, " ");
	}

	@Override
	public String toString() {
		return ((this.startDate != null) ? this.startDate.toISO8601() : "-inf")
				+ " - "
				+ ((this.endDate != null) ? this.endDate.toISO8601() : "+inf");
	}

}
