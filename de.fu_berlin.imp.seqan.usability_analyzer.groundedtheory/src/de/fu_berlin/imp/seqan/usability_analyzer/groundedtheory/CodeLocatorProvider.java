package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory;

import java.net.URI;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.nebula.utils.ExecutorService;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.ILocatorProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;

public class CodeLocatorProvider implements ILocatorProvider {

	public static final String CODE_NAMESPACE = "code";
	private static final Logger LOGGER = Logger
			.getLogger(CodeLocatorProvider.class);
	private static final ExecutorService EXECUTOR_SERVICE = new ExecutorService();

	@Override
	public String[] getAllowedNamespaces() {
		return new String[] { CODE_NAMESPACE };
	}

	@Override
	public ILocatable getObject(URI uri, IProgressMonitor monitor) {
		ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
				.getService(ICodeService.class);

		String[] path = uri.getRawPath().substring(1).split("/");

		// 0: Key
		ICode code = codeService.getCode(Long.parseLong(path[0]));
		return code;
	}

	@Override
	public boolean showInWorkspace(final ILocatable[] locatables, boolean open,
			IProgressMonitor monitor) {
		if (locatables.length > 0) {
			try {
				return EXECUTOR_SERVICE.syncExec(new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception {
						// EpisodeView episodeView = (EpisodeView)
						// WorkbenchUtils
						// .getView(EpisodeView.ID);
						// if (episodeView == null) {
						// return true;
						// }
						//
						// EpisodeViewer viewer =
						// episodeView.getEpisodeViewer();
						// viewer.setSelection(new
						// StructuredSelection(locatables));
						// List<ILocatable> selected = SelectionUtils
						// .getAdaptableObjects(viewer.getSelection(),
						// ILocatable.class);
						// return selected.size() == locatables.length;
						return false;
					}
				});
			} catch (Exception e) {
				LOGGER.error(e);
			}
		}
		return true;
	}

}
