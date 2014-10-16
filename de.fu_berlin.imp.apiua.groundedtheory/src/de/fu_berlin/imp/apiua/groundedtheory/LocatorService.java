package de.fu_berlin.imp.apiua.groundedtheory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.ExecUtils;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.services.location.ILocatorService;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICodeInstance;

public class LocatorService {

	public static ILocatorService INSTANCE;

	static {
		try {
			INSTANCE = (ILocatorService) PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow()
					.getService(ILocatorService.class);
		} catch (NoClassDefFoundError e) {
		}
	}

	/**
	 * Preloads the given {@link URI}s. This process differs from
	 * {@link ILocatorService#resolve(URI[], IProgressMonitor)} in the following
	 * way:
	 * <ol>
	 * <li>The cache is resized to the preloaded elements can be accessed in a
	 * fast fashion.</li>
	 * <li>If an {@link ICodeInstance} was resolved its content will also be
	 * preloaded.</li>
	 * <li>This function does not return a {@link Future} but returns when it
	 * really preloaded the elements.</li>
	 * </ol>
	 * 
	 * @param uris
	 * @param monitor
	 * @return {@link Future#isDone()} returns <code>true</code> as soon as the
	 *         preloading has finished.
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public static void preload(List<URI> uris, IProgressMonitor monitor)
			throws InterruptedException, ExecutionException {
		SubMonitor subMonitor = SubMonitor.convert(monitor,
				(int) Math.round(uris.size() * 1.3));
		INSTANCE.setCacheSize((int) Math.round(uris.size() * 2.5));
		List<ICodeInstance> codeInstances = new ArrayList<ICodeInstance>();

		Future<List<ILocatable>> futureLocatables = INSTANCE.resolve(uris,
				subMonitor.newChild(8));
		if (ExecUtils.isUIThread()) {
			ExecUtils.busyWait(futureLocatables);
		}
		for (ILocatable locatable : futureLocatables.get()) {
			if (locatable instanceof ICodeInstance) {
				codeInstances.add((ICodeInstance) locatable);
			}
		}
		subMonitor.setWorkRemaining(codeInstances.size());
		for (ICodeInstance codeInstance : codeInstances) {
			Future<ILocatable> future = INSTANCE.resolve(codeInstance.getId(),
					subMonitor.newChild(1));
			if (ExecUtils.isUIThread()) {
				ExecUtils.busyWait(future);
			}
			future.get();
		}

		INSTANCE.resetCacheSize();
	}

}
