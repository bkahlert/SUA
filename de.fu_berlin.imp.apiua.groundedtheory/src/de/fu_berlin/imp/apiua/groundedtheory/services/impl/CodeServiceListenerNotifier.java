package de.fu_berlin.imp.apiua.groundedtheory.services.impl;

import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.colors.RGB;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.apiua.groundedtheory.model.dimension.IDimension;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeServiceListener;

public class CodeServiceListenerNotifier {

	private static final Logger LOGGER = Logger
			.getLogger(CodeServiceListenerNotifier.class);

	private final Queue<ICodeServiceListener> codeServiceListeners = new ConcurrentLinkedQueue<ICodeServiceListener>();

	void addCodeServiceListener(ICodeServiceListener codeServiceListener) {
		if (this.codeServiceListeners.contains(codeServiceListener)) {
			LOGGER.warn("Tried to add an already registered listener");
		} else {
			this.codeServiceListeners.add(codeServiceListener);
		}
	}

	void removeCodeServiceListener(ICodeServiceListener codeServiceListener) {
		if (this.codeServiceListeners.contains(codeServiceListener)) {
			this.codeServiceListeners.remove(codeServiceListener);
		} else {
			Thread.dumpStack();
			LOGGER.warn("Tried to remove unregistered listener");
		}

	}

	void codesCreated(final List<ICode> codes) {
		for (final ICodeServiceListener codeServiceListener : this.codeServiceListeners) {
			ExecUtils.nonUIAsyncExec(CodeServiceListenerNotifier.class,
					"Codes Created Notification", new Callable<Void>() {
						@Override
						public Void call() throws Exception {
							codeServiceListener.codesAdded(codes);
							return null;
						}
					});
		}
	}

	void codesAssigned(final List<ICode> codes, final List<URI> uris) {
		for (final ICodeServiceListener codeServiceListener : this.codeServiceListeners) {
			ExecUtils.nonUIAsyncExec(CodeServiceListenerNotifier.class,
					"Codes Assigned Notification", new Callable<Void>() {
						@Override
						public Void call() throws Exception {
							codeServiceListener.codesAssigned(codes, uris);
							return null;
						}
					});
		}
	}

	public void codeRenamed(final ICode code, final String oldCaption,
			final String newCaption) {
		for (final ICodeServiceListener codeServiceListener : this.codeServiceListeners) {
			ExecUtils.nonUIAsyncExec(CodeServiceListenerNotifier.class,
					"Code Renamed Notification", new Callable<Void>() {
						@Override
						public Void call() throws Exception {
							codeServiceListener.codeRenamed(code, oldCaption,
									newCaption);
							return null;
						}
					});
		}
	}

	public void codeRecolored(final ICode code, final RGB oldColor,
			final RGB newColor) {
		for (final ICodeServiceListener codeServiceListener : this.codeServiceListeners) {
			ExecUtils.nonUIAsyncExec(CodeServiceListenerNotifier.class,
					"Code Recolored Notification", new Callable<Void>() {
						@Override
						public Void call() throws Exception {
							codeServiceListener.codeRecolored(code, oldColor,
									newColor);
							return null;
						}
					});
		}
	}

	void codesRemoved(final List<ICode> removedCodes, final List<URI> uris) {
		for (final ICodeServiceListener codeServiceListener : this.codeServiceListeners) {
			ExecUtils.nonUIAsyncExec(CodeServiceListenerNotifier.class,
					"Codes Removed Notification", new Callable<Void>() {
						@Override
						public Void call() throws Exception {
							codeServiceListener
									.codesRemoved(removedCodes, uris);
							return null;
						}
					});
		}
	}

	public void codeMoved(final ICode code, final ICode oldParentCode,
			final ICode newParentCode) {
		for (final ICodeServiceListener codeServiceListener : this.codeServiceListeners) {
			ExecUtils.nonUIAsyncExec(CodeServiceListenerNotifier.class,
					"Code Moved Notification", new Callable<Void>() {
						@Override
						public Void call() throws Exception {
							codeServiceListener.codeMoved(code, oldParentCode,
									newParentCode);
							return null;
						}
					});
		}
	}

	void codeDeleted(final ICode code) {
		for (final ICodeServiceListener codeServiceListener : this.codeServiceListeners) {
			ExecUtils.nonUIAsyncExec(CodeServiceListenerNotifier.class,
					"Code Deleted Notification", new Callable<Void>() {
						@Override
						public Void call() throws Exception {
							codeServiceListener.codeDeleted(code);
							return null;
						}
					});
		}
	}

	void memoAdded(final URI uri, String html) {
		for (final ICodeServiceListener codeServiceListener : this.codeServiceListeners) {
			ExecUtils.nonUIAsyncExec(CodeServiceListenerNotifier.class,
					"Memo Added Notification", new Callable<Void>() {
						@Override
						public Void call() throws Exception {
							codeServiceListener.memoAdded(uri);
							return null;
						}
					});
		}
	}

	void memoModified(final URI uri, String html) {
		for (final ICodeServiceListener codeServiceListener : this.codeServiceListeners) {
			ExecUtils.nonUIAsyncExec(CodeServiceListenerNotifier.class,
					"Memo Modified Notification", new Callable<Void>() {
						@Override
						public Void call() throws Exception {
							codeServiceListener.memoModified(uri);
							return null;
						}
					});
		}
	}

	void memoRemoved(final URI uri, String html) {
		for (final ICodeServiceListener codeServiceListener : this.codeServiceListeners) {
			ExecUtils.nonUIAsyncExec(CodeServiceListenerNotifier.class,
					"Memo Removed Notification", new Callable<Void>() {
						@Override
						public Void call() throws Exception {
							codeServiceListener.memoRemoved(uri);
							return null;
						}
					});
		}
	}

	public void episodeAdded(final IEpisode episode) {
		for (final ICodeServiceListener codeServiceListener : this.codeServiceListeners) {
			ExecUtils.nonUIAsyncExec(CodeServiceListenerNotifier.class,
					"Episode Added Notification", new Callable<Void>() {
						@Override
						public Void call() throws Exception {
							codeServiceListener.episodeAdded(episode);
							return null;
						}
					});
		}
	}

	public void episodeReplaced(final IEpisode oldEpisode,
			final IEpisode newEpisode) {
		for (final ICodeServiceListener codeServiceListener : this.codeServiceListeners) {
			ExecUtils.nonUIAsyncExec(CodeServiceListenerNotifier.class,
					"Episode Replaced Notification", new Callable<Void>() {
						@Override
						public Void call() throws Exception {
							codeServiceListener.episodeReplaced(oldEpisode,
									newEpisode);
							return null;
						}
					});
		}
	}

	public void episodesDeleted(final Set<IEpisode> deletedEpisodes) {
		for (final ICodeServiceListener codeServiceListener : this.codeServiceListeners) {
			ExecUtils.nonUIAsyncExec(CodeServiceListenerNotifier.class,
					"Episodes Deleted Notification", new Callable<Void>() {
						@Override
						public Void call() throws Exception {
							codeServiceListener
									.episodesDeleted(deletedEpisodes);
							return null;
						}
					});
		}
	}

	public void dimensionChanged(final URI uri, final IDimension oldDimension,
			final IDimension newDimension) {
		for (final ICodeServiceListener codeServiceListener : this.codeServiceListeners) {
			ExecUtils.nonUIAsyncExec(CodeServiceListenerNotifier.class,
					"Dimension Changed Notification", new Callable<Void>() {
						@Override
						public Void call() throws Exception {
							codeServiceListener.dimensionChanged(uri,
									oldDimension, newDimension);
							return null;
						}
					});
		}
	}

	public void dimensionValueChanged(final URI uri, final String oldValue,
			final String value) {
		for (final ICodeServiceListener codeServiceListener : this.codeServiceListeners) {
			ExecUtils.nonUIAsyncExec(CodeServiceListenerNotifier.class,
					"Dimension Value Change Notification",
					new Callable<Void>() {
						@Override
						public Void call() throws Exception {
							codeServiceListener.dimensionValueChanged(uri,
									oldValue, value);
							return null;
						}
					});
		}
	}

	public void propertiesChanged(final URI uri,
			final List<URI> addedProperties, final List<URI> removedProperties) {
		for (final ICodeServiceListener codeServiceListener : this.codeServiceListeners) {
			ExecUtils.nonUIAsyncExec(CodeServiceListenerNotifier.class,
					"Properties Changed Notification", new Callable<Void>() {
						@Override
						public Void call() throws Exception {
							codeServiceListener.propertiesChanged(uri,
									addedProperties, removedProperties);
							return null;
						}
					});
		}
	}

	public void axialCodingModelAdded(final URI uri) {
		for (final ICodeServiceListener codeServiceListener : this.codeServiceListeners) {
			codeServiceListener.axialCodingModelAdded(uri);
		}
	}

	public void axialCodingModelUpdated(final URI uri) {
		for (final ICodeServiceListener codeServiceListener : this.codeServiceListeners) {
			codeServiceListener.axialCodingModelUpdated(uri);
		}
	}

	public void axialCodingModelRemoved(final URI uri) {
		for (final ICodeServiceListener codeServiceListener : this.codeServiceListeners) {
			codeServiceListener.axialCodingModelRemoved(uri);
		}
	}

}
