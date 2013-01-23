package de.fu_berlin.imp.seqan.usability_analyzer.core.model;

import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.List;

import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;

/**
 * This class describes a range defined by two {@link TimeZoneDate}s. To define
 * a unbounded range, simply pass <code>null</code> as one argument.
 * 
 * @author bkahlert
 */
public class TimeZoneDateRange {

	public static TimeZoneDateRange calculateOuterDateRange(
			TimeZoneDateRange... dateRanges) {
		TimeZoneDate earliestDate = null;
		TimeZoneDate latestDate = null;

		for (TimeZoneDateRange dateRange : dateRanges) {
			if (dateRange == null)
				continue;
			if (dateRange.getStartDate() != null
					&& (earliestDate == null || earliestDate
							.compareTo(dateRange.getStartDate()) > 0))
				earliestDate = dateRange.getStartDate();
			if (dateRange.getEndDate() != null
					&& (latestDate == null || latestDate.compareTo(dateRange
							.getEndDate()) < 0))
				latestDate = dateRange.getEndDate();
		}

		return new TimeZoneDateRange(earliestDate, latestDate);
	}

	public static TimeZoneDateRange calculateOuterDateRange(
			List<? extends HasDateRange> hasDateRanges) {
		List<TimeZoneDateRange> dateRanges = new LinkedList<TimeZoneDateRange>();
		for (HasDateRange hasDateRange : hasDateRanges)
			dateRanges.add(hasDateRange.getDateRange());
		return calculateOuterDateRange(dateRanges
				.toArray(new TimeZoneDateRange[0]));
	}

	public static TimeZoneDateRange calculateOuterDateRange(
			HasDateRange... hasDateRanges) {
		List<TimeZoneDateRange> dateRanges = new LinkedList<TimeZoneDateRange>();
		for (HasDateRange hasDateRange : hasDateRanges)
			dateRanges.add(hasDateRange.getDateRange());
		return calculateOuterDateRange(dateRanges
				.toArray(new TimeZoneDateRange[0]));
	}

	private TimeZoneDate startDate;
	private TimeZoneDate endDate;

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

	public TimeZoneDate getStartDate() {
		return startDate;
	}

	public TimeZoneDate getEndDate() {
		return endDate;
	}

	public Long getDifference() {
		if (this.startDate == null)
			return null;
		if (this.endDate == null)
			return null;
		return this.endDate.getTime() - this.startDate.getTime();
	}

	public boolean isInRange(long time) {
		return (this.startDate == null || this.startDate.getTime() <= time)
				&& (this.endDate == null || time <= this.endDate.getTime());
	}

	public boolean isInRange(TimeZoneDate date) {
		if (date == null)
			return false;
		return this.isInRange(date.getTime());
	}

	public boolean isBeforeRange(long time) {
		if (this.startDate == null)
			return false;
		else
			return time < this.startDate.getTime();
	}

	public boolean isBeforeRange(TimeZoneDate date) {
		if (date == null)
			return false;
		return this.isBeforeRange(date.getTime());
	}

	public boolean isAfterRange(long time) {
		if (this.endDate == null)
			return false;
		else
			return time > this.endDate.getTime();
	}

	public boolean isAfterRange(TimeZoneDate date) {
		if (date == null)
			return false;
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
		if (dateRange == null)
			return true;

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
		if (!this.isIntersected(dateRange))
			return false;
		boolean areNeighbors1 = this.endDate != null
				&& dateRange.getStartDate() != null
				&& this.endDate.equals(dateRange.getStartDate());
		boolean areNeighbors2 = this.startDate != null
				&& dateRange.getEndDate() != null
				&& this.startDate.equals(dateRange.endDate);
		return !areNeighbors1 && !areNeighbors2;
	}

	@Override
	public String toString() {
		return ((startDate != null) ? startDate.toISO8601() : "-inf") + " - "
				+ ((endDate != null) ? endDate.toISO8601() : "+inf");
	}

}
