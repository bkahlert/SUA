package de.fu_berlin.imp.seqan.usability_analyzer.core.model;

import java.util.Date;

/**
 * This class describes a range defined by two {@link Date}s. To define a
 * unbounded range, simply pass <code>null</code> as one argument.
 * 
 * @author bkahlert
 */
public class DateRange {

	public static DateRange calculateOuterDateRange(DateRange... dateRanges) {
		Date earliestDate = null;
		Date latestDate = null;

		for (DateRange dateRange : dateRanges) {
			if (earliestDate == null
					|| earliestDate.compareTo(dateRange.getStartDate()) > 0)
				earliestDate = dateRange.getStartDate();
			if (latestDate == null
					|| latestDate.compareTo(dateRange.getEndDate()) < 0)
				latestDate = dateRange.getEndDate();
		}

		return new DateRange(earliestDate, latestDate);
	}

	private Date startDate;
	private Date endDate;

	public DateRange(Date startDate, Date endDate) {
		super();
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public DateRange(long startDate, long endDate) {
		super();
		this.startDate = new Date(startDate);
		this.endDate = new Date(endDate);
	}

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
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

	public boolean isInRange(Date date) {
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

	public boolean isBeforeRange(Date date) {
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

	public boolean isAfterRange(Date date) {
		if (date == null)
			return false;
		return this.isAfterRange(date.getTime());
	}

	/**
	 * Returns true if the given {@link DateRange} intersects the current
	 * {@link DateRange}.
	 * 
	 * @param dateRange
	 * @return
	 */
	public boolean isIntersected(DateRange dateRange) {
		if (dateRange == null)
			return true;

		boolean startAndEndBeforeRange = this.isBeforeRange(dateRange
				.getStartDate()) && this.isBeforeRange(dateRange.getEndDate());
		boolean startAndEndAfterRange = this.isAfterRange(dateRange
				.getStartDate()) && this.isAfterRange(dateRange.getEndDate());
		return !(startAndEndBeforeRange || startAndEndAfterRange);
	}

}
