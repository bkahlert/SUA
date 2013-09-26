package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.nebula.utils.ExecutorUtil;
import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.IdentifierFactory;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.ILocatorProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.WorkbenchUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer.EpisodeViewer;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.EpisodeView;

public class EpisodeCodeableProvider implements ILocatorProvider {

	private static final Logger LOGGER = Logger
			.getLogger(EpisodeCodeableProvider.class);

	public static final String EPISODE_NAMESPACE = "episode";

	@Override
	public String[] getAllowedNamespaces() {
		return new String[] { EPISODE_NAMESPACE };
	}

	@Override
	public ILocatable getObject(URI uri, IProgressMonitor monitor) {
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
	public boolean showInWorkspace(final ILocatable[] locatables, boolean open,
			IProgressMonitor monitor) {
		if (locatables.length > 0) {
			EpisodeView episodeView = (EpisodeView) WorkbenchUtils
					.getView(EpisodeView.ID);
			if (episodeView == null) {
				return false;
			}

			final EpisodeViewer viewer = episodeView.getEpisodeViewer();
			try {
				return ExecutorUtil.syncExec(new Callable<ILocatable[]>() {
					@Override
					public ILocatable[] call() {
						viewer.setSelection(new StructuredSelection(locatables));
						List<ILocatable> selectedCodeables = SelectionUtils
								.getAdaptableObjects(viewer.getSelection(),
										ILocatable.class);
						return selectedCodeables.toArray(new ILocatable[0]);
					}
				}) != null;
			} catch (Exception e) {
				LOGGER.error(e);
			}
		}
		return false;
	}

}
