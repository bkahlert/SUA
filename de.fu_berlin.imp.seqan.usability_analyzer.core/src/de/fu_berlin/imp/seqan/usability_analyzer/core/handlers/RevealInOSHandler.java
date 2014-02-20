package de.fu_berlin.imp.seqan.usability_analyzer.core.handlers;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.rcp.selectionUtils.retriever.SelectionRetrieverFactory;
import com.bkahlert.nebula.utils.FileUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.IRevealableInOS;

public class RevealInOSHandler extends AbstractHandler {

	private static final Logger LOGGER = Logger
			.getLogger(RevealInOSHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final List<IRevealableInOS> revealables = SelectionRetrieverFactory
				.getSelectionRetriever(IRevealableInOS.class).getSelection();

		if (revealables.size() > 0) {
			for (IRevealableInOS revealable : revealables) {
				try {
					File file = revealable.getFile();
					if (file == null) {
						MessageDialog.openError(PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow().getShell(),
								"Error Revealing File",
								"Could not reveal file of " + revealable
										+ " because the file does not exist.");
					} else {
						FileUtils.showFileInFilesystem(file);
					}
				} catch (Exception e) {
					LOGGER.warn("Could not reveal file in your OS", e);
				}
			}
		}

		return null;
	}
}