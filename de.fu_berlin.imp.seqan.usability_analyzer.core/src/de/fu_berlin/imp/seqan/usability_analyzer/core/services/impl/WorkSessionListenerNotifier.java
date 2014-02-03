package de.fu_berlin.imp.seqan.usability_analyzer.core.services.impl;

import java.util.ArrayList;
import java.util.List;

import com.bkahlert.devel.nebula.utils.ExecutorUtil;

import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IWorkSession;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IWorkSessionListener;

public class WorkSessionListenerNotifier {
	private final List<IWorkSessionListener> workSessionListeners = new ArrayList<IWorkSessionListener>();
	private static final ExecutorUtil EXECUTOR_UTIL = new ExecutorUtil(
			WorkSessionListenerNotifier.class);

	void addWorkSessionListener(IWorkSessionListener workSessionListener) {
		this.workSessionListeners.add(workSessionListener);
	}

	void removeWorkSessionListener(IWorkSessionListener workSessionListener) {
		this.workSessionListeners.remove(workSessionListener);
	}

	void workSessionStarted(final IWorkSession workSession) {
		EXECUTOR_UTIL
				.nonUIAsyncExec(
						this.workSessionListeners,
						new ExecutorUtil.ParametrizedCallable<IWorkSessionListener, Void>() {
							@Override
							public Void call(
									IWorkSessionListener workSessionListener)
									throws Exception {
								workSessionListener
										.workSessionStarted(workSession);
								return null;
							}
						});
	}
}
