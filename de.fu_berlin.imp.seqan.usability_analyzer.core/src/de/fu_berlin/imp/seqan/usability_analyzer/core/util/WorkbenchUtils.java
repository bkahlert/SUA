package de.fu_berlin.imp.seqan.usability_analyzer.core.util;

import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.nebula.utils.ExecutorUtil;

public class WorkbenchUtils {
	private static final Logger LOGGER = Logger.getLogger(WorkbenchUtils.class);

	/**
	 * Returns a {@link IViewPart} with the given id.
	 * <p>
	 * This method may be called from any thread.
	 * 
	 * @param id
	 * @return
	 */
	public static IViewPart getView(final String id) {
		Callable<IViewPart> callable = new Callable<IViewPart>() {
			@Override
			public IViewPart call() throws Exception {
				return PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.getActivePage()
						.showView(id, null, IWorkbenchPage.VIEW_VISIBLE);
			}
		};
		try {
			if (Display.getCurrent() == Display.getDefault()) {
				return callable.call();
			} else {
				return ExecutorUtil.syncExec(callable);
			}
		} catch (Exception e) {
			LOGGER.error("Error retrieving " + IViewPart.class.getSimpleName(),
					e);
			return null;
		}
	}
}
