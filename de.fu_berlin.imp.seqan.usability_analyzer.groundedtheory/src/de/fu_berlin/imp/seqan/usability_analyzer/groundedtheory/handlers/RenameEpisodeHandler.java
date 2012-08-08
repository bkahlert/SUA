package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.handlers;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.bkahlert.devel.rcp.selectionUtils.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer.EpisodeViewer;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.EpisodeView;

public class RenameEpisodeHandler extends AbstractHandler {

	private static final Logger LOGGER = Logger
			.getLogger(RenameEpisodeHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		IWorkbenchPart part = HandlerUtil.getActivePart(event);

		List<IEpisode> episodes = SelectionRetrieverFactory
				.getSelectionRetriever(IEpisode.class).getSelection();

		if (episodes.size() > 0) {
			if (part instanceof EpisodeView) {
				EpisodeView episodeView = (EpisodeView) part;
				EpisodeViewer episodeViewer = episodeView.getEpisodeViewer();
				episodeViewer.getViewer().editElement(episodes.get(0), 0);
			}
		} else {
			LOGGER.warn("Selection did not contain any "
					+ IEpisode.class.getSimpleName());
		}

		return null;
	}

}
