package de.fu_berlin.imp.apiua.groundedtheory;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.WorkbenchUtils;
import com.bkahlert.nebula.utils.selection.SelectionUtils;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.preferences.SUACorePreferences;
import de.fu_berlin.imp.apiua.core.services.location.AdaptingLocatorProvider;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.groundedtheory.viewer.CodeViewer;
import de.fu_berlin.imp.apiua.groundedtheory.views.CodeView;

public class CodeLocatorProvider extends AdaptingLocatorProvider {

	public static final String CODE_NAMESPACE = "code";

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(CodeLocatorProvider.class);

	public CodeLocatorProvider() {
		super(ICode.class);
	}

	@Override
	public boolean isResolvabilityImpossible(URI uri) {
		return !SUACorePreferences.URI_SCHEME.equalsIgnoreCase(uri.getScheme())
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
			List<URI> selectedLocatables = ExecUtils.syncExec(() -> {
				final CodeViewer viewer = codeView.getCodeViewer();
				viewer.setSelection(new StructuredSelection(uris));
				return SelectionUtils.getAdaptableObjects(
						viewer.getSelection(), URI.class);
			});
			return selectedLocatables.toArray(new URI[0]);
		} catch (Exception e) {
			return new URI[0];
		}
	}

}
