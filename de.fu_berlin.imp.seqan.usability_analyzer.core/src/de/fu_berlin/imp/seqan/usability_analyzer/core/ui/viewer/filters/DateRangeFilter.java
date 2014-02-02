package de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters;

import java.net.URI;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.ILocatorService;

/**
 * This class works in conjunction with {@link IRange} and keeps only those
 * objects (of Type {@link IRange}) that are within the time range each object
 * 
 * @author bkahlert
 * 
 */
public class DateRangeFilter extends ViewerFilter {
	private final Logger logger = Logger.getLogger(TimeZoneDateRange.class);
	private final ILocatorService locatorService = (ILocatorService) PlatformUI
			.getWorkbench().getService(ILocatorService.class);

	private final TimeZoneDateRange dateRange;

	public DateRangeFilter(TimeZoneDateRange dateRange) {
		this.dateRange = dateRange;
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		Assert.isNotNull(element);

		if (this.dateRange == null) {
			return true;
		}

		if (element instanceof URI) {
			try {
				element = locatorService.resolve((URI) element, null).get();
			} catch (Exception e) {
				logger.error("Could not resolve " + element);
			}
		}

		if (element instanceof HasDateRange) {
			TimeZoneDateRange dateRange = ((HasDateRange) element)
					.getDateRange();
			return this.dateRange.isIntersected(dateRange);
		}

		logger.warn("The object " + element + " does not implement "
				+ HasDateRange.class.getSimpleName()
				+ " and had been filtered from viewer " + viewer + ".");
		return false;
	}
}
