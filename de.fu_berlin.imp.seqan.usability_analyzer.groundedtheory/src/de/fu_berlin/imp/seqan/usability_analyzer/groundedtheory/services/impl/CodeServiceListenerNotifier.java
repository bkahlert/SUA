package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;

import com.bkahlert.devel.nebula.colors.RGB;
import com.bkahlert.devel.nebula.utils.ExecutorUtil;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeServiceListener;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeServiceListener2;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;

public class CodeServiceListenerNotifier {

	private static final Logger LOGGER = Logger
			.getLogger(CodeServiceListenerNotifier.class);

	private ExecutorService notifierPool = ExecutorUtil
			.newFixedMultipleOfProcessorsThreadPool(2);
	private List<ICodeServiceListener> codeServiceListeners = new ArrayList<ICodeServiceListener>();

	void addCodeServiceListener(ICodeServiceListener codeServiceListener) {
		if (codeServiceListeners.contains(codeServiceListener)) {
			LOGGER.warn("Tried to add an already registered listener");
		} else {
			codeServiceListeners.add(codeServiceListener);
		}
	}

	void removeCodeServiceListener(ICodeServiceListener codeServiceListener) {
		if (codeServiceListeners.contains(codeServiceListener)) {
			codeServiceListeners.remove(codeServiceListener);
		} else {
			LOGGER.warn("Tried to remove unregistered listener");
		}

	}

	void codesCreated(final List<ICode> codes) {
		for (final ICodeServiceListener codeServiceListener : codeServiceListeners) {
			notifierPool.submit(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					codeServiceListener.codesAdded(codes);
					return null;
				}
			});
		}
	}

	void codeAssigned(final List<ICode> codes, final List<ICodeable> codeables) {
		for (final ICodeServiceListener codeServiceListener : codeServiceListeners) {
			notifierPool.submit(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					codeServiceListener.codesAssigned(codes, codeables);
					return null;
				}
			});
		}
	}

	public void codeRenamed(final ICode code, final String oldCaption,
			final String newCaption) {
		for (final ICodeServiceListener codeServiceListener : codeServiceListeners) {
			notifierPool.submit(new Callable<Void>() {
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
		for (final ICodeServiceListener codeServiceListener : codeServiceListeners) {
			notifierPool.submit(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					codeServiceListener.codeRecolored(code, oldColor, newColor);
					return null;
				}
			});
		}
	}

	void codesRemoved(final List<ICode> removedCodes,
			final List<ICodeable> codeables) {
		for (final ICodeServiceListener codeServiceListener : codeServiceListeners) {
			notifierPool.submit(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					codeServiceListener.codesRemoved(removedCodes, codeables);
					return null;
				}
			});
		}
	}

	public void codeMoved(final ICode code, final ICode oldParentCode,
			final ICode newParentCode) {
		for (final ICodeServiceListener codeServiceListener : codeServiceListeners) {
			notifierPool.submit(new Callable<Void>() {
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
		for (final ICodeServiceListener codeServiceListener : codeServiceListeners) {
			notifierPool.submit(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					codeServiceListener.codeDeleted(code);
					return null;
				}
			});
		}
	}

	void memoAdded(final ICode code, String html) {
		for (final ICodeServiceListener codeServiceListener : codeServiceListeners) {
			notifierPool.submit(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					codeServiceListener.memoAdded(code);
					return null;
				}
			});
		}
	}

	void memoAdded(final ICodeInstance codeInstance, String html) {
		for (final ICodeServiceListener codeServiceListener : codeServiceListeners) {
			notifierPool.submit(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					if (codeServiceListener instanceof ICodeServiceListener2)
						((ICodeServiceListener2) codeServiceListener)
								.memoAdded(codeInstance);
					return null;
				}
			});
		}
	}

	void memoAdded(final ICodeable codeable, String html) {
		for (final ICodeServiceListener codeServiceListener : codeServiceListeners) {
			notifierPool.submit(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					codeServiceListener.memoAdded(codeable);
					return null;
				}
			});
		}
	}

	void memoModified(final ICode code, String html) {
		for (final ICodeServiceListener codeServiceListener : codeServiceListeners) {
			notifierPool.submit(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					codeServiceListener.memoModified(code);
					return null;
				}
			});
		}
	}

	void memoModified(final ICodeInstance codeInstance, String html) {
		for (final ICodeServiceListener codeServiceListener : codeServiceListeners) {
			notifierPool.submit(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					if (codeServiceListener instanceof ICodeServiceListener2)
						((ICodeServiceListener2) codeServiceListener)
								.memoModified(codeInstance);
					return null;
				}
			});
		}
	}

	void memoModified(final ICodeable codeable, String html) {
		for (final ICodeServiceListener codeServiceListener : codeServiceListeners) {
			notifierPool.submit(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					codeServiceListener.memoModified(codeable);
					return null;
				}
			});
		}
	}

	void memoRemoved(final ICode code, String html) {
		for (final ICodeServiceListener codeServiceListener : codeServiceListeners) {
			notifierPool.submit(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					codeServiceListener.memoRemoved(code);
					return null;
				}
			});
		}
	}

	void memoRemoved(final ICodeInstance codeInstance, String html) {
		for (final ICodeServiceListener codeServiceListener : codeServiceListeners) {
			notifierPool.submit(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					if (codeServiceListener instanceof ICodeServiceListener2)
						((ICodeServiceListener2) codeServiceListener)
								.memoRemoved(codeInstance);
					return null;
				}
			});
		}
	}

	void memoRemoved(final ICodeable codeable, String html) {
		for (final ICodeServiceListener codeServiceListener : codeServiceListeners) {
			notifierPool.submit(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					codeServiceListener.memoRemoved(codeable);
					return null;
				}
			});
		}
	}

	public void episodeAdded(final IEpisode episode) {
		for (final ICodeServiceListener codeServiceListener : codeServiceListeners) {
			notifierPool.submit(new Callable<Void>() {
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
		for (final ICodeServiceListener codeServiceListener : codeServiceListeners) {
			notifierPool.submit(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					codeServiceListener.episodeReplaced(oldEpisode, newEpisode);
					return null;
				}
			});
		}
	}

	public void episodesDeleted(final Set<IEpisode> deletedEpisodes) {
		for (final ICodeServiceListener codeServiceListener : codeServiceListeners) {
			notifierPool.submit(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					codeServiceListener.episodesDeleted(deletedEpisodes);
					return null;
				}
			});
		}
	}

}
