package de.fu_berlin.imp.apiua.core.services.impl;

import java.util.ArrayList;
import java.util.List;

import com.bkahlert.nebula.utils.ExecUtils;

import de.fu_berlin.imp.apiua.core.model.data.IBaseDataContainer;
import de.fu_berlin.imp.apiua.core.services.IDataServiceListener;

public class DataServiceListenerNotifier {
	private final List<IDataServiceListener> dataServiceListeners = new ArrayList<IDataServiceListener>();

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
		ExecUtils
				.nonUIAsyncExec(
						DataServiceListenerNotifier.class,
						"Data Directories Added Notification",
						this.dataServiceListeners,
						new ExecUtils.ParametrizedCallable<IDataServiceListener, Void>() {
							@Override
							public Void call(
									IDataServiceListener dataServiceListener)
									throws Exception {
								dataServiceListener
										.dataDirectoriesAdded(dataContainers);
								return null;
							}
						});
	}

	public void dataDirectoriesRemoved(
			final List<? extends IBaseDataContainer> dataContainers) {
		ExecUtils
				.nonUIAsyncExec(
						DataServiceListenerNotifier.class,
						"Data Directories Removed Notification",
						this.dataServiceListeners,
						new ExecUtils.ParametrizedCallable<IDataServiceListener, Void>() {
							@Override
							public Void call(
									IDataServiceListener dataServiceListener)
									throws Exception {
								dataServiceListener
										.dataDirectoriesRemoved(dataContainers);
								return null;
							}
						});
	}

	public void dataDirectoriesLoaded(
			final List<? extends IBaseDataContainer> dataContainers) {
		ExecUtils
				.nonUIAsyncExec(
						DataServiceListenerNotifier.class,
						"Data Directories Loaded Notification",
						this.dataServiceListeners,
						new ExecUtils.ParametrizedCallable<IDataServiceListener, Void>() {
							@Override
							public Void call(
									IDataServiceListener dataServiceListener)
									throws Exception {
								dataServiceListener
										.dataDirectoriesLoaded(dataContainers);
								return null;
							}
						});
	}

	public void dataDirectoriesUnloaded(
			final List<? extends IBaseDataContainer> dataContainers) {
		ExecUtils
				.nonUIAsyncExec(
						DataServiceListenerNotifier.class,
						"Data Directories Unloaded Notification",
						this.dataServiceListeners,
						new ExecUtils.ParametrizedCallable<IDataServiceListener, Void>() {
							@Override
							public Void call(
									IDataServiceListener dataServiceListener)
									throws Exception {
								dataServiceListener
										.dataDirectoriesUnloaded(dataContainers);
								return null;
							}
						});
	}
}
