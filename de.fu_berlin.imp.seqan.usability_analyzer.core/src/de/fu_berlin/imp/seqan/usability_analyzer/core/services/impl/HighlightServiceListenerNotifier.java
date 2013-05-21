package de.fu_berlin.imp.seqan.usability_analyzer.core.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.bkahlert.devel.nebula.utils.ExecutorService.ParametrizedCallable;
import com.bkahlert.devel.nebula.utils.ExecutorUtil;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IHighlightServiceListener;

public class HighlightServiceListenerNotifier {
	private List<IHighlightServiceListener> highlightServiceListeners = new ArrayList<IHighlightServiceListener>();
	private static final ExecutorService POOL = com.bkahlert.devel.nebula.utils.ExecutorService
			.newFixedMultipleOfProcessorsThreadPool(1);

	public void addHighlightServiceListener(
			IHighlightServiceListener highlightServiceListener) {
		this.highlightServiceListeners.add(highlightServiceListener);
	}

	public void removeHighlightServiceListeners(
			IHighlightServiceListener highlightServiceListener) {
		this.highlightServiceListeners.remove(highlightServiceListener);
	}

	public void highlight(final Object sender,
			final TimeZoneDateRange[] ranges, final boolean moveInsideViewport) {
		ExecutorUtil.nonUIAsyncExec(POOL, this.highlightServiceListeners,
				new ParametrizedCallable<IHighlightServiceListener, Void>() {
					@Override
					public Void call(
							IHighlightServiceListener highlightServiceListener)
							throws Exception {
						highlightServiceListener.highlight(sender, ranges,
								moveInsideViewport);
						return null;
					}
				});
	}

	public void highlight(final Object sender,
			final Map<IIdentifier, TimeZoneDateRange[]> groupedRanges,
			final boolean moveInsideViewport) {
		ExecutorUtil.nonUIAsyncExec(POOL, this.highlightServiceListeners,
				new ParametrizedCallable<IHighlightServiceListener, Void>() {
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
