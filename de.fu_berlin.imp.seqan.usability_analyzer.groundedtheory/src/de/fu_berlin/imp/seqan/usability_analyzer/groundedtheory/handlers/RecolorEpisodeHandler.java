package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.handlers;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.rcp.selectionUtils.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;

public class RecolorEpisodeHandler extends AbstractHandler {

	private static final Logger LOGGER = Logger
			.getLogger(RecolorEpisodeHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		List<IEpisode> episodes = SelectionRetrieverFactory
				.getSelectionRetriever(IEpisode.class).getSelection();

		if (episodes.size() == 1) {
			IEpisode episode = episodes.get(0);
			ColorDialog dialog = new ColorDialog(new Shell(
					Display.getDefault(), SWT.SHELL_TRIM));
			dialog.setRGB(episode.getColor());
			RGB newRGB = dialog.open();
			if (newRGB != null) {
				IEpisode newEpisode = episode.changeColor(newRGB);
				ICodeService codeService = (ICodeService) PlatformUI
						.getWorkbench().getService(ICodeService.class);
				try {
					codeService.replaceEpisodeAndSave(episode, newEpisode);
				} catch (CodeServiceException e) {
					LOGGER.error("Error replacing the "
							+ IEpisode.class.getSimpleName() + "'s color");
				}
			}
		} else {
			LOGGER.warn("Selection did not only contain a single "
					+ IEpisode.class.getSimpleName());
		}

		return null;
	}

}
