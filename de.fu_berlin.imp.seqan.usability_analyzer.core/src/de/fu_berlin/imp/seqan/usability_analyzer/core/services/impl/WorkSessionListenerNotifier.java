package de.fu_berlin.imp.seqan.usability_analyzer.core.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import com.bkahlert.devel.nebula.utils.ExecutorService.ParametrizedCallable;
import com.bkahlert.devel.nebula.utils.ExecutorUtil;

import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IWorkSession;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IWorkSessionListener;

public class WorkSessionListenerNotifier {
	private List<IWorkSessionListener> workSessionListeners = new ArrayList<IWorkSessionListener>();
	private static final ExecutorService POOL = com.bkahlert.devel.nebula.utils.ExecutorService
			.newFixedMultipleOfProcessorsThreadPool(1);

	void addWorkSessionListener(IWorkSessionListener workSessionListener) {
		this.workSessionListeners.add(workSessionListener);
	}

	void removeWorkSessionListener(IWorkSessionListener workSessionListener) {
		this.workSessionListeners.remove(workSessionListener);
	}

	void workSessionStarted(final IWorkSession workSession) {
		ExecutorUtil.nonUIAsyncExec(POOL, this.workSessionListeners,
				new ParametrizedCallable<IWorkSessionListener, Void>() {
					@Override
					public Void call(IWorkSessionListener workSessionListener)
							throws Exception {
						workSessionListener.workSessionStarted(workSession);
						return null;
					}
				});
	}
}
