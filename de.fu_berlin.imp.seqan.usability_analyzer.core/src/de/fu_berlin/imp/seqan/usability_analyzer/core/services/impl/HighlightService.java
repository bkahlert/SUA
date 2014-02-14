package de.fu_berlin.imp.seqan.usability_analyzer.core.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;

import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;
import com.bkahlert.nebula.datetime.CalendarRange;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.HasIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IHighlightService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IHighlightServiceListener;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;

public class HighlightService implements IHighlightService {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(HighlightService.class);

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

		List<HasIdentifier> identifiables = SelectionUtils.getAdaptableObjects(
				selection, HasIdentifier.class);
		Map<IIdentifier, List<CalendarRange>> groupedRanges = new HashMap<IIdentifier, List<CalendarRange>>();
		for (HasIdentifier identifiable : identifiables) {
			HasDateRange range = (HasDateRange) Platform.getAdapterManager()
					.getAdapter(identifiable, HasDateRange.class);
			if (range != null) {
				IIdentifier identifier = identifiable.getIdentifier();
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
