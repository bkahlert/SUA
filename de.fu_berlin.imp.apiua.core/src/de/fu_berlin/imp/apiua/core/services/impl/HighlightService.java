package de.fu_berlin.imp.apiua.core.services.impl;

import de.fu_berlin.imp.apiua.core.model.HasIdentifier;
import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.apiua.core.services.IHighlightService;
import de.fu_berlin.imp.apiua.core.services.IHighlightServiceListener;
import de.fu_berlin.imp.apiua.core.services.location.ILocatorService;
import de.fu_berlin.imp.apiua.core.ui.viewer.filters.HasDateRange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.datetime.CalendarRange;
import com.bkahlert.nebula.utils.selection.SelectionUtils;

public class HighlightService implements IHighlightService {

	private static final Logger LOGGER = Logger
			.getLogger(HighlightService.class);

	private final ILocatorService locatorService = (ILocatorService) PlatformUI
			.getWorkbench().getService(ILocatorService.class);

	private final HighlightServiceListenerNotifier notifier = new HighlightServiceListenerNotifier();

	public HighlightService() {
	}

	@Override
	public void addHighlightServiceListener(
			IHighlightServiceListener highlightServiceListener) {
		this.notifier.addHighlightServiceListener(highlightServiceListener);
	}

	@Override
	public void removeHighlightServiceListener(
			IHighlightServiceListener highlightServiceListener) {
		this.notifier.removeHighlightServiceListeners(highlightServiceListener);
	}

	@Override
	public void highlight(Object sender, CalendarRange range,
			boolean moveInsideViewport) {
		Assert.isNotNull(sender);
		Assert.isNotNull(range);
		this.highlight(sender, new CalendarRange[] { range },
				moveInsideViewport);
	}

	@Override
	public void highlight(Object sender, CalendarRange[] ranges,
			boolean moveInsideViewport) {
		Assert.isNotNull(sender);
		Assert.isNotNull(ranges);
		Assert.isTrue(ranges.length > 0);
		this.notifier.highlight(sender, ranges, moveInsideViewport);
	}

	@Override
	public void highlight(Object sender,
			Map<IIdentifier, CalendarRange[]> groupedRanges,
			boolean moveInsideViewport) {
		Assert.isNotNull(sender);
		Assert.isNotNull(groupedRanges);
		Assert.isTrue(!groupedRanges.isEmpty());
		this.notifier.highlight(sender, groupedRanges, moveInsideViewport);
	}

	@Override
	public void highlight(Object sender, ISelection selection,
			boolean moveInsideViewport) {
		Assert.isNotNull(sender);
		Assert.isNotNull(selection);

		List<URI> uris = SelectionUtils.getAdaptableObjects(selection,
				URI.class);

		List<ILocatable> locatables;
		try {
			locatables = this.locatorService.resolve(uris, null).get();
		} catch (Exception e) {
			LOGGER.error("Error resolving " + StringUtils.join(uris, ", "), e);
			return;
		}
		Map<IIdentifier, List<CalendarRange>> groupedRanges = new HashMap<IIdentifier, List<CalendarRange>>();
		for (ILocatable locatable : locatables) {
			if (!(locatable instanceof HasIdentifier)
					|| !(locatable instanceof HasDateRange)) {
				continue;
			}
			HasDateRange range = (HasDateRange) locatable;
			if (range != null) {
				IIdentifier identifier = ((HasIdentifier) locatable)
						.getIdentifier();
				if (!groupedRanges.containsKey(identifier)) {
					groupedRanges.put(identifier,
							new ArrayList<CalendarRange>());
				}
				groupedRanges.get(identifier).add(
						range.getDateRange().getCalendarRange());
			}
		}

		Map<IIdentifier, CalendarRange[]> groupedRangesArr = new HashMap<IIdentifier, CalendarRange[]>();
		for (IIdentifier identifier : groupedRanges.keySet()) {
			groupedRangesArr.put(identifier, groupedRanges.get(identifier)
					.toArray(new CalendarRange[0]));
		}

		if (!groupedRangesArr.isEmpty()) {
			this.highlight(sender, groupedRangesArr, moveInsideViewport);
		}
	}
}
