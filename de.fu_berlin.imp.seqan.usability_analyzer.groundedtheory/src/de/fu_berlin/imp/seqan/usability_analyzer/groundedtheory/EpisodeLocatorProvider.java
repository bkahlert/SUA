package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;
import com.bkahlert.nebula.utils.ExecUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.AdaptingLocatorProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.URIUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.WorkbenchUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.Episodes;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisodes;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer.EpisodeViewer;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.EpisodeView;

public class EpisodeLocatorProvider extends AdaptingLocatorProvider {

	public static final String EPISODE_NAMESPACE = "episode";
	private static final Logger LOGGER = Logger
			.getLogger(EpisodeLocatorProvider.class);
	private static final ICodeService CODE_SERVICE = (ICodeService) PlatformUI
			.getWorkbench().getService(ICodeService.class);

	@SuppressWarnings("unchecked")
	public EpisodeLocatorProvider() {
		super(IEpisodes.class, IEpisode.class);
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

		List<String> trail = URIUtils.getTrail(uri);
		switch (trail.size()) {
		case 0:
			return IEpisodes.class;
		case 1:
			return IEpisode.class;
		}

		LOGGER.error("Unknown " + URI.class.getSimpleName() + " format: " + uri);
		return null;
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

		IIdentifier identifier = URIUtils.getIdentifier(uri);
		List<String> trail = URIUtils.getTrail(uri);
		switch (trail.size()) {
		case 0:
			return new Episodes(identifier);
		case 1:
			Set<IEpisode> episodes = CODE_SERVICE.getEpisodes(identifier);
			for (IEpisode episode : episodes) {
				if (episode.getUri().equals(uri)) {
					return episode;
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
