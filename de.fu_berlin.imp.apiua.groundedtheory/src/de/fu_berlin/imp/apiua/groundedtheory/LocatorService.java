package de.fu_berlin.imp.apiua.groundedtheory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.ViewerUtils;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.services.location.ILocatorService;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICodeInstance;

public class LocatorService {

	@SuppressWarnings("unused")
	private static Logger LOGGER = Logger.getLogger(LocatorService.class);

	public static ILocatorService INSTANCE;

	static {
		if (!ExecUtils.isUIThread()) {
			throw new RuntimeException("Must be called from UI thread");
		}
		try {
			INSTANCE = (ILocatorService) PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow()
					.getService(ILocatorService.class);
		} catch (NoClassDefFoundError e) {
		}
	}

	// private static final int CACHE_RESET_DELAY = 2000;
	//
	// private static DelayableThread delayableThread = null;
	//
	// private static void delayedCacheDestroy() {
	// if (delayableThread != null) {
	// delayableThread.setDelay(CACHE_RESET_DELAY);
	// } else {
	// delayableThread = new DelayableThread(new Runnable() {
	// @Override
	// public void run() {
	// INSTANCE.resetCacheSize();
	// delayableThread = null;
	// }
	// }, CACHE_RESET_DELAY);
	// }
	// }

	/**
	 * Preloads the given {@link URI}s. This process differs from
	 * {@link ILocatorService#resolve(URI[], IProgressMonitor)} in the following
	 * way:
	 * <ol>
	 * <li>If an {@link ICodeInstance} was resolved its content will also be
	 * preloaded.</li>
	 * <li>This function does not return a {@link Future} but returns when it
	 * actually preloaded the elements.</li>
	 * </ol>
	 *
	 * @param cacheKey
	 * @param uris
	 * @param monitor
	 * @return {@link Future#isDone()} returns <code>true</code> as soon as the
	 *         preloading has finished.
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public static void preload(String cacheKey, List<URI> uris,
			IProgressMonitor monitor) throws InterruptedException,
			ExecutionException {
		SubMonitor subMonitor = SubMonitor.convert(monitor,
				(int) Math.round(uris.size() * 1.3));
		INSTANCE.createCache(cacheKey, (int) Math.round(uris.size() * 2.5));
		List<URI> codeInstancePhenomenons = new ArrayList<URI>();

		Future<List<ILocatable>> futureLocatables = INSTANCE.preload(cacheKey,
				uris, subMonitor.newChild(8));
		if (ExecUtils.isUIThread()) {
			ExecUtils.busyWait(futureLocatables);
		}
		for (ILocatable locatable : futureLocatables.get()) {
			if (locatable instanceof ICodeInstance) {
				codeInstancePhenomenons
				.add(((ICodeInstance) locatable).getId());
			}
		}
		subMonitor.setWorkRemaining(codeInstancePhenomenons.size());
		Future<List<ILocatable>> future = INSTANCE.preload(cacheKey,
				codeInstancePhenomenons, subMonitor.newChild(1));
		if (ExecUtils.isUIThread()) {
			ExecUtils.busyWait(future);
		}
		future.get();
	}

	/**
	 * Resolves (using {@link LocatorService}) all the given objects and their
	 * descendants. This becomes handy to preload items, so a later running UI
	 * thread code will not need to spend the time resolving.
	 *
	 * @param viewer
	 * @param parentElements
	 * @param monitor
	 *
	 * @throws Exception
	 */
	public static void preload(Class<?> clazz, final TreeViewer viewer,
			final List<?> parentElements, IProgressMonitor monitor)
					throws Exception {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 10);
		List<URI> uris = ExecUtils.syncExec(new Callable<List<URI>>() {
			@Override
			public List<URI> call() throws Exception {
				List<Object> elements = new ArrayList<Object>();
				for (Object parentObject : parentElements) {
					elements.addAll(ViewerUtils.getDescendants(viewer,
							parentObject));
				}
				List<URI> uris = new ArrayList<URI>();
				for (Object object : elements) {
					if (object instanceof URI) {
						URI uri = (URI) object;
						if (!uris.contains(uri)) {
							uris.add(uri);
						}
					}
				}
				return uris;
			}
		});
		subMonitor.worked(1);
		preload(clazz.toString(), uris, subMonitor.newChild(9));
		subMonitor.done();
	}

}
