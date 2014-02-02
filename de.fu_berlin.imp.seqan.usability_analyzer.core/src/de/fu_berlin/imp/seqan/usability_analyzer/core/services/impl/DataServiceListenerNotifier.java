package de.fu_berlin.imp.seqan.usability_analyzer.core.services.impl;

import java.util.ArrayList;
import java.util.List;

import com.bkahlert.devel.nebula.utils.ExecutorService;
import com.bkahlert.devel.nebula.utils.ExecutorService.ParametrizedCallable;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IDataServiceListener;

public class DataServiceListenerNotifier {
	private final List<IDataServiceListener> dataServiceListeners = new ArrayList<IDataServiceListener>();
	private static final ExecutorService EXECUTOR_SERVICE = new ExecutorService(
			DataServiceListenerNotifier.class, 1);

	public void addDataDirectoryServiceListener(
			IDataServiceListener dataServiceListener) {
		this.dataServiceListeners.add(dataServiceListener);
	}

	public void removeDataDirectoryServiceListener(
			IDataServiceListener dataServiceListener) {
		this.dataServiceListeners.remove(dataServiceListener);
	}

	public void dataDirectoriesAdded(
			final List<? extends IBaseDataContainer> dataContainers) {
		EXECUTOR_SERVICE.nonUIAsyncExec(this.dataServiceListeners,
				new ParametrizedCallable<IDataServiceListener, Void>() {
					@Override
					public Void call(IDataServiceListener dataServiceListener)
							throws Exception {
						dataServiceListener
								.dataDirectoriesAdded(dataContainers);
						return null;
					}
				});
	}

	public void dataDirectoriesRemoved(
			final List<? extends IBaseDataContainer> dataContainers) {
		EXECUTOR_SERVICE.nonUIAsyncExec(this.dataServiceListeners,
				new ParametrizedCallable<IDataServiceListener, Void>() {
					@Override
					public Void call(IDataServiceListener dataServiceListener)
							throws Exception {
						dataServiceListener
								.dataDirectoriesRemoved(dataContainers);
						return null;
					}
				});
	}

	public void dataDirectoriesLoaded(
			final List<? extends IBaseDataContainer> dataContainers) {
		EXECUTOR_SERVICE.nonUIAsyncExec(this.dataServiceListeners,
				new ParametrizedCallable<IDataServiceListener, Void>() {
					@Override
					public Void call(IDataServiceListener dataServiceListener)
							throws Exception {
						dataServiceListener
								.dataDirectoriesLoaded(dataContainers);
						return null;
					}
				});
	}

	public void dataDirectoriesUnloaded(
			final List<? extends IBaseDataContainer> dataContainers) {
		EXECUTOR_SERVICE.nonUIAsyncExec(this.dataServiceListeners,
				new ParametrizedCallable<IDataServiceListener, Void>() {
					@Override
					public Void call(IDataServiceListener dataServiceListener)
							throws Exception {
						dataServiceListener
								.dataDirectoriesUnloaded(dataContainers);
						return null;
					}
				});
	}
}
