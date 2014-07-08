package de.fu_berlin.imp.apiua.groundedtheory.handlers;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.information.InformationControlManagerUtils;
import com.bkahlert.nebula.utils.selection.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.apiua.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;

public class DeleteEpisodeHandler extends AbstractHandler {

	private static final Logger LOGGER = Logger
			.getLogger(DeleteEpisodeHandler.class);

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {

		final List<IEpisode> episodes = SelectionRetrieverFactory
				.getSelectionRetriever(IEpisode.class).getSelection();
		if (InformationControlManagerUtils.getCurrentInput() instanceof ILocatable) {
			IEpisode input = (IEpisode) InformationControlManagerUtils
					.getCurrentInput();
			if (!episodes.contains(input)) {
				episodes.add(input);
			}
		}

		if (episodes.size() == 0) {
			return null;
		}

		ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
				.getService(ICodeService.class);
		try {
			codeService.deleteEpisodeAndSave(episodes);
		} catch (CodeServiceException e) {
			LOGGER.error("Error while deleting " + IEpisode.class + "s", e);
		}

		return null;
	}
}
