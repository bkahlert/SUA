package de.fu_berlin.imp.seqan.usability_analyzer.core.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.bkahlert.devel.nebula.utils.ExecUtils;
import com.bkahlert.nebula.datetime.CalendarRange;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IHighlightServiceListener;

public class HighlightServiceListenerNotifier {
	private final List<IHighlightServiceListener> highlightServiceListeners = new ArrayList<IHighlightServiceListener>();

	public void addHighlightServiceListener(
			IHighlightServiceListener highlightServiceListener) {
		this.highlightServiceListeners.add(highlightServiceListener);
	}

	public void removeHighlightServiceListeners(
			IHighlightServiceListener highlightServiceListener) {
		this.highlightServiceListeners.remove(highlightServiceListener);
	}

	public void highlight(final Object sender, final CalendarRange[] ranges,
			final boolean moveInsideViewport) {
		ExecUtils
				.nonUIAsyncExec(
						DataServiceListenerNotifier.class,
						"Highlight Notification",
						this.highlightServiceListeners,
						new ExecUtils.ParametrizedCallable<IHighlightServiceListener, Void>() {
							@Override
							public Void call(
									IHighlightServiceListener highlightServiceListener)
									throws Exception {
								highlightServiceListener.highlight(sender,
										ranges, moveInsideViewport);
								return null;
							}
						});
	}

	public void highlight(final Object sender,
			final Map<IIdentifier, CalendarRange[]> groupedRanges,
			final boolean moveInsideViewport) {
		ExecUtils
				.nonUIAsyncExec(
						DataServiceListenerNotifier.class,
						"Highlight Notification",
						this.highlightServiceListeners,
						new ExecUtils.ParametrizedCallable<IHighlightServiceListener, Void>() {
							@Override
							public Void call(
									IHighlightServiceListener highlightServiceListener)
									throws Exception {
								highlightServiceListener.highlight(sender,
										groupedRanges, moveInsideViewport);
								return null;
							}
						});
	}

}
