package de.fu_berlin.imp.seqan.usability_analyzer.core.services.impl;

import java.util.ArrayList;
import java.util.List;

import com.bkahlert.nebula.utils.ExecUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IWorkSession;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IWorkSessionListener;

public class WorkSessionListenerNotifier {
	private final List<IWorkSessionListener> workSessionListeners = new ArrayList<IWorkSessionListener>();

	void addWorkSessionListener(IWorkSessionListener workSessionListener) {
		this.workSessionListeners.add(workSessionListener);
	}

	void removeWorkSessionListener(IWorkSessionListener workSessionListener) {
		this.workSessionListeners.remove(workSessionListener);
	}

	void workSessionStarted(final IWorkSession workSession) {
		ExecUtils
				.nonUIAsyncExec(
						DataServiceListenerNotifier.class,
						"Work Session Started Notification",
						this.workSessionListeners,
						new ExecUtils.ParametrizedCallable<IWorkSessionListener, Void>() {
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
