package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.nebula.utils.ExecutorUtil;
import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.IdentifierFactory;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.WorkbenchUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeableProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer.EpisodeViewer;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.EpisodeView;

public class EpisodeCodeableProvider extends CodeableProvider {

	private static final Logger LOGGER = Logger
			.getLogger(EpisodeCodeableProvider.class);

	public static final String EPISODE_NAMESPACE = "episode";

	@Override
	public List<String> getAllowedNamespaces() {
		return Arrays.asList(EPISODE_NAMESPACE);
	}

	@Override
	public Callable<ICodeable> getCodedObjectCallable(
			final AtomicReference<IProgressMonitor> monitor,
			final URI codeInstanceID) {
		return new Callable<ICodeable>() {
			@Override
			public ICodeable call() throws Exception {
				ICodeService codeService = (ICodeService) PlatformUI
						.getWorkbench().getService(ICodeService.class);

				String[] path = codeInstanceID.getRawPath().substring(1)
						.split("/");

				// 0: Key
				Set<IEpisode> episodes = codeService
						.getEpisodes(IdentifierFactory.createFrom(path[0]));

				// 1: Compare URI
				URI id = codeInstanceID;
				for (IEpisode episode : episodes) {
					if (episode.getUri().equals(id)) {
						return episode;
					}
				}

				return null;
			}
		};
	}

	@Override
	public ICodeable[] showCodedObjectsInWorkspace2(
			final List<ICodeable> codedObjects) {
		if (codedObjects.size() > 0) {
			EpisodeView episodeView = (EpisodeView) WorkbenchUtils
					.getView(EpisodeView.ID);
			if (episodeView == null) {
				return codedObjects.toArray(new ICodeable[0]);
			}

			final EpisodeViewer viewer = episodeView.getEpisodeViewer();
			try {
				return ExecutorUtil.syncExec(new Callable<ICodeable[]>() {
					@Override
					public ICodeable[] call() {
						viewer.setSelection(new StructuredSelection(
								codedObjects));
						List<ICodeable> selectedCodeables = SelectionUtils
								.getAdaptableObjects(viewer.getSelection(),
										ICodeable.class);
						return selectedCodeables.toArray(new ICodeable[0]);
					}
				});
			} catch (Exception e) {
				LOGGER.error(e);
			}
		}
		return null;
	}

}
