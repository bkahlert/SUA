package de.fu_berlin.imp.seqan.usability_analyzer.core.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import com.bkahlert.devel.nebula.utils.ExecutorUtil;
import com.bkahlert.devel.nebula.utils.ExecutorUtil.ParametrizedCallable;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IDataServiceListener;

public class DataListenerNotifier {
	private List<IDataServiceListener> dataServiceListeners = new ArrayList<IDataServiceListener>();
	private static final ExecutorService POOL = ExecutorUtil
			.newFixedMultipleOfProcessorsThreadPool(1);

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
		ExecutorUtil.nonUIAsyncExec(POOL, dataServiceListeners,
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
		ExecutorUtil.nonUIAsyncExec(POOL, dataServiceListeners,
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
		ExecutorUtil.nonUIAsyncExec(POOL, dataServiceListeners,
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
		ExecutorUtil.nonUIAsyncExec(POOL, dataServiceListeners,
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
