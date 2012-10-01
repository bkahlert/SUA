package de.fu_berlin.imp.seqan.usability_analyzer.core.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.IBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IDataDirectoriesServiceListener;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.ExecutorUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.ExecutorUtil.ParametrizedCallable;

public class DataResourceListenerNotifier {
	private List<IDataDirectoriesServiceListener> dataDirectoriesServiceListeners = new ArrayList<IDataDirectoriesServiceListener>();
	private static final ExecutorService POOL = ExecutorUtil
			.newFixedMultipleOfProcessorsThreadPool(1);

	public void addDataDirectoryServiceListener(
			IDataDirectoriesServiceListener dataDirectoriesServiceListener) {
		this.dataDirectoriesServiceListeners
				.add(dataDirectoriesServiceListener);
	}

	public void removeDataDirectoryServiceListener(
			IDataDirectoriesServiceListener dataDirectoriesServiceListener) {
		this.dataDirectoriesServiceListeners
				.remove(dataDirectoriesServiceListener);
	}

	public void dataDirectoriesAdded(
			final List<? extends IBaseDataContainer> dataContainers) {
		ExecutorUtil
				.nonUIAsyncExec(
						POOL,
						dataDirectoriesServiceListeners,
						new ParametrizedCallable<IDataDirectoriesServiceListener, Void>() {
							@Override
							public Void call(
									IDataDirectoriesServiceListener dataDirectoriesServiceListener)
									throws Exception {
								dataDirectoriesServiceListener
										.dataDirectoriesAdded(dataContainers);
								return null;
							}
						});
	}

	public void dataDirectoriesRemoved(
			final List<? extends IBaseDataContainer> dataContainers) {
		ExecutorUtil
				.nonUIAsyncExec(
						POOL,
						dataDirectoriesServiceListeners,
						new ParametrizedCallable<IDataDirectoriesServiceListener, Void>() {
							@Override
							public Void call(
									IDataDirectoriesServiceListener dataDirectoriesServiceListener)
									throws Exception {
								dataDirectoriesServiceListener
										.dataDirectoriesRemoved(dataContainers);
								return null;
							}
						});
	}

	public void activeDataDirectoriesChanged(
			final List<? extends IBaseDataContainer> dataContainers) {
		ExecutorUtil
				.nonUIAsyncExec(
						POOL,
						dataDirectoriesServiceListeners,
						new ParametrizedCallable<IDataDirectoriesServiceListener, Void>() {
							@Override
							public Void call(
									IDataDirectoriesServiceListener dataDirectoriesServiceListener)
									throws Exception {
								dataDirectoriesServiceListener
										.activeDataDirectoriesChanged(dataContainers);
								return null;
							}
						});
	}
}
