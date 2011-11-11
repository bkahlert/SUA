package de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DateRange;

/**
 * This class works in conjunction with {@link IRange} and keeps only those
 * objects (of Type {@link IRange}) that are within the time range each object
 * 
 * @author bkahlert
 * 
 */
public class DateRangeFilter extends ViewerFilter {
	private Logger logger = Logger.getLogger(DateRange.class);

	private DateRange dateRange;

	public DateRangeFilter(DateRange dateRange) {
		this.dateRange = dateRange;
	}

	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (this.dateRange == null)
			return true;

		if (element instanceof HasDateRange) {
			DateRange dateRange = ((HasDateRange) element).getDateRange();
			return this.dateRange.isIntersected(dateRange);
		}

		logger.warn("The object " + element + " does not implement "
				+ HasDateRange.class.getSimpleName()
				+ " and had been filtered from viewer " + viewer + ".");
		return false;
	}
}
