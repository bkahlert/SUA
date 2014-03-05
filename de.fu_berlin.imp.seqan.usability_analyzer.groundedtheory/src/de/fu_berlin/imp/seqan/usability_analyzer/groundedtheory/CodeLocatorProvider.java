package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;
import com.bkahlert.nebula.utils.ExecUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.AdaptingLocatorProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.WorkbenchUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer.CodeViewer;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.CodeView;

public class CodeLocatorProvider extends AdaptingLocatorProvider {

	public static final String CODE_NAMESPACE = "code";

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(CodeLocatorProvider.class);

	@SuppressWarnings("unchecked")
	public CodeLocatorProvider() {
		super(ICode.class);
	}

	@Override
	public boolean isResolvabilityImpossible(URI uri) {
		return !"sua".equalsIgnoreCase(uri.getScheme())
				|| !CODE_NAMESPACE.equals(uri.getHost());
	}

	@Override
	public Class<? extends ILocatable> getType(URI uri) {
		if (this.isResolvabilityImpossible(uri)) {
			return null;
		}

		return ICode.class;
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

		String[] path = uri.getRawPath().substring(1).split("/");

		// 0: Key
		ICode code = codeService.getCode(Long.parseLong(path[0]));
		return code;
	}

	@Override
	public boolean showInWorkspace(final URI[] uris, boolean open,
			IProgressMonitor monitor) {
		if (uris.length > 0) {
			CodeView codeView = (CodeView) WorkbenchUtils.getView(CodeView.ID);
			return this.selectCodes(uris, codeView).length == uris.length;
			// try {
			// return ExecUtils.syncExec(new Callable<Boolean>() {
			// @Override
			// public Boolean call() throws Exception {
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
			// return false;
			// }
			// });
			// } catch (Exception e) {
			// LOGGER.error(e);
			// }
		}
		return true;
	}

	private URI[] selectCodes(final URI[] uris, final CodeView codeView) {
		try {
			List<URI> selectedLocatables = ExecUtils
					.syncExec(new Callable<List<URI>>() {
						@Override
						public List<URI> call() throws Exception {
							final CodeViewer viewer = codeView.getCodeViewer();
							viewer.setSelection(new StructuredSelection(uris));
							return SelectionUtils.getAdaptableObjects(
									viewer.getSelection(), URI.class);
						}
					});
			return selectedLocatables.toArray(new URI[0]);
		} catch (Exception e) {
			return new URI[0];
		}
	}

}
