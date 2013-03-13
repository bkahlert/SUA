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

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.HasIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IHighlightService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IHighlightServiceListener;

public class HighlightService implements IHighlightService {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(HighlightService.class);

	private HighlightServiceListenerNotifier notifier = new HighlightServiceListenerNotifier();

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
	public void highlight(Object sender, TimeZoneDateRange range) {
		Assert.isNotNull(sender);
		Assert.isNotNull(range);
		this.highlight(sender, new TimeZoneDateRange[] { range });
	}

	@Override
	public void highlight(Object sender, TimeZoneDateRange[] ranges) {
		Assert.isNotNull(sender);
		Assert.isNotNull(ranges);
		Assert.isTrue(ranges.length > 0);
		this.notifier.highlight(sender, ranges);
	}

	@Override
	public void highlight(Object sender,
			Map<IIdentifier, TimeZoneDateRange[]> groupedRanges) {
		Assert.isNotNull(sender);
		Assert.isNotNull(groupedRanges);
		Assert.isTrue(!groupedRanges.isEmpty());
		this.notifier.highlight(sender, groupedRanges);
	}

	@Override
	public void highlight(Object sender, ISelection selection) {
		Assert.isNotNull(sender);
		Assert.isNotNull(selection);

		List<HasIdentifier> identifiables = SelectionUtils.getAdaptableObjects(
				selection, HasIdentifier.class);
		Map<IIdentifier, List<TimeZoneDateRange>> groupedRanges = new HashMap<IIdentifier, List<TimeZoneDateRange>>();
		for (HasIdentifier identifiable : identifiables) {
			TimeZoneDateRange range = (TimeZoneDateRange) Platform
					.getAdapterManager().getAdapter(identifiable,
							TimeZoneDateRange.class);
			if (range != null) {
				IIdentifier identifier = identifiable.getIdentifier();
				if (!groupedRanges.containsKey(identifier)) {
					groupedRanges.put(identifier,
							new ArrayList<TimeZoneDateRange>());
				}
				groupedRanges.get(identifier).add(range);
			}
		}

		Map<IIdentifier, TimeZoneDateRange[]> groupedRangesArr = new HashMap<IIdentifier, TimeZoneDateRange[]>();
		for (IIdentifier identifier : groupedRanges.keySet()) {
			groupedRangesArr.put(identifier, groupedRanges.get(identifier)
					.toArray(new TimeZoneDateRange[0]));
		}

		if (!groupedRangesArr.isEmpty()) {
			this.highlight(sender, groupedRangesArr);
		}
	}
}
