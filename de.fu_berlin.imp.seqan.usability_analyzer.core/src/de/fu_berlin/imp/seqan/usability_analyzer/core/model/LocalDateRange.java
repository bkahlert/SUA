package de.fu_berlin.imp.seqan.usability_analyzer.core.model;


/**
 * This class describes a range defined by two {@link LocalDate}s. To define a
 * unbounded range, simply pass <code>null</code> as one argument.
 * 
 * @author bkahlert
 */
public class LocalDateRange {

	public static LocalDateRange calculateOuterDateRange(
			LocalDateRange... dateRanges) {
		LocalDate earliestDate = null;
		LocalDate latestDate = null;

		for (LocalDateRange dateRange : dateRanges) {
			if (earliestDate == null
					|| earliestDate.compareTo(dateRange.getStartDate()) > 0)
				earliestDate = dateRange.getStartDate();
			if (latestDate == null
					|| latestDate.compareTo(dateRange.getEndDate()) < 0)
				latestDate = dateRange.getEndDate();
		}

		return new LocalDateRange(earliestDate, latestDate);
	}

	private LocalDate startDate;
	private LocalDate endDate;

	public LocalDateRange(LocalDate startDate, LocalDate endDate) {
		super();
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public LocalDate getEndDate() {
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

	public boolean isInRange(LocalDate date) {
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

	public boolean isBeforeRange(LocalDate date) {
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

	public boolean isAfterRange(LocalDate date) {
		if (date == null)
			return false;
		return this.isAfterRange(date.getTime());
	}

	/**
	 * Returns true if the given {@link LocalDateRange} intersects the current
	 * {@link LocalDateRange}.
	 * 
	 * @param dateRange
	 * @return
	 */
	public boolean isIntersected(LocalDateRange dateRange) {
		if (dateRange == null)
			return true;

		boolean startAndEndBeforeRange = this.isBeforeRange(dateRange
				.getStartDate()) && this.isBeforeRange(dateRange.getEndDate());
		boolean startAndEndAfterRange = this.isAfterRange(dateRange
				.getStartDate()) && this.isAfterRange(dateRange.getEndDate());
		return !(startAndEndBeforeRange || startAndEndAfterRange);
	}

}
