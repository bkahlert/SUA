package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory;

import java.net.URI;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.ExecUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.AdaptingLocatorProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.URIUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;

public class CodeInstanceLocatorProvider extends AdaptingLocatorProvider {

	public static final String CODE_INSTANCE_NAMESPACE = "codeinstance";

	private static final Logger LOGGER = Logger
			.getLogger(CodeLocatorProvider.class);

	@SuppressWarnings("unchecked")
	public CodeInstanceLocatorProvider() {
		super(ICodeInstance.class);
	}

	@Override
	public final boolean isResolvabilityImpossible(URI uri) {
		return !"sua".equalsIgnoreCase(uri.getScheme())
				|| !CODE_INSTANCE_NAMESPACE.equals(URIUtils.getResource(uri));
	}

	@Override
	public Class<? extends ILocatable> getType(URI uri) {
		if (this.isResolvabilityImpossible(uri)) {
			return null;
		}

		return ICodeInstance.class;
	}

	@Override
	public boolean getObjectIsShortRunning(URI uri) {
		return true;
	}

	@Override
	public ILocatable getObject(URI uri, IProgressMonitor monitor) {
		if (this.isResolvabilityImpossible(uri)) {
			return null;
		}

		ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
				.getService(ICodeService.class);

		for (ICode code : codeService.getCodeStore().getTopLevelCodes()) {
			for (ICodeInstance codeInstance : codeService.getAllInstances(code)) {
				if (codeInstance.getUri().equals(uri)) {
					return codeInstance;
				}
			}
		}
		return null;
	}

	@Override
	public boolean showInWorkspace(final URI[] uris, boolean open,
			IProgressMonitor monitor) {
		if (uris.length > 0) {
			try {
				return ExecUtils.syncExec(new Callable<Boolean>() {
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
						// StructuredSelection(URIS));
						// List<ILocatable> selected = SelectionUtils
						// .getAdaptableObjects(viewer.getSelection(),
						// ILocatable.class);
						// return selected.size() == URIS.length;
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
