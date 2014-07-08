package de.fu_berlin.imp.apiua.core.ui.viewer.filters;

import de.fu_berlin.imp.apiua.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.services.location.ILocatorService;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.PlatformUI;

/**
 * This class works in conjunction with {@link IRange} and keeps only those
 * objects (of Type {@link IRange}) that are within the time range each object
 * 
 * @author bkahlert
 * 
 */
public class DateRangeFilter extends ViewerFilter {
	private final Logger logger = Logger.getLogger(DateRangeFilter.class);
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
				element = this.locatorService.resolve((URI) element, null)
						.get();
			} catch (Exception e) {
				this.logger.error("Could not resolve " + element);
			}
		}

		if (element instanceof HasDateRange) {
			TimeZoneDateRange dateRange = ((HasDateRange) element)
					.getDateRange();
			return this.dateRange.isIntersected(dateRange);
		}

		this.logger.warn("The object " + element + " does not implement "
				+ HasDateRange.class.getSimpleName()
				+ " and had been filtered from viewer " + viewer + ".");
		return false;
	}
}
