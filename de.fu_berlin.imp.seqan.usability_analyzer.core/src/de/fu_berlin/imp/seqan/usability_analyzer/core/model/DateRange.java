package de.fu_berlin.imp.seqan.usability_analyzer.core.model;

import java.util.Date;

/**
 * This class describes a range defined by two {@link Date}s. To define a
 * unbounded range, simply pass <code>null</code> as one argument.
 * 
 * @author bkahlert
 */
public class DateRange {
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

}
