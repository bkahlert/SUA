package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.nebula.utils.ExecutorService;
import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.IdentifierFactory;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.AdaptingLocatorProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.WorkbenchUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer.EpisodeViewer;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.EpisodeView;

public class EpisodeLocatorProvider extends AdaptingLocatorProvider {

	public static final String EPISODE_NAMESPACE = "episode";
	private static final Logger LOGGER = Logger
			.getLogger(EpisodeLocatorProvider.class);
	private static final ExecutorService EXECUTOR_SERVICE = new ExecutorService();

	@SuppressWarnings("unchecked")
	public EpisodeLocatorProvider() {
		super(IEpisode.class);
	}

	@Override
	public boolean isResolvabilityImpossible(URI uri) {
		return !"sua".equalsIgnoreCase(uri.getScheme())
				|| !EPISODE_NAMESPACE.equals(uri.getHost());
	}

	@Override
	public Class<? extends ILocatable> getType(URI uri) {
		if (this.isResolvabilityImpossible(uri)) {
			return null;
		}

		return IEpisode.class;
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
		Set<IEpisode> episodes = codeService.getEpisodes(IdentifierFactory
				.createFrom(path[0]));

		// 1: Compare URI
		for (IEpisode episode : episodes) {
			if (episode.getUri().equals(uri)) {
				return episode;
			}
		}

		return null;
	}

	@Override
	public boolean showInWorkspace(final URI[] uris, boolean open,
			IProgressMonitor monitor) {
		if (uris.length > 0) {
			try {
				return EXECUTOR_SERVICE.syncExec(new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception {
						EpisodeView episodeView = (EpisodeView) WorkbenchUtils
								.getView(EpisodeView.ID);
						if (episodeView == null) {
							return true;
						}

						EpisodeViewer viewer = episodeView.getEpisodeViewer();
						viewer.setSelection(new StructuredSelection(uris));
						List<URI> selected = SelectionUtils
								.getAdaptableObjects(viewer.getSelection(),
										URI.class);
						return selected.size() == uris.length;
					}
				});
			} catch (Exception e) {
				LOGGER.error(e);
			}
		}
		return true;
	}

}
