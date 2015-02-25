package de.fu_berlin.imp.apiua.uri.handlers;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.selection.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.uri.model.IUri;
import de.fu_berlin.imp.apiua.uri.services.IUriService;
import de.fu_berlin.imp.apiua.uri.services.UriServiceException;

public class DeleteUriHandler extends AbstractHandler {

	private static final Logger LOGGER = Logger
			.getLogger(DeleteUriHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		List<IUri> uris = SelectionRetrieverFactory.getSelectionRetriever(
				IUri.class).getSelection();
		IUriService uriService = (IUriService) PlatformUI.getWorkbench()
				.getService(IUriService.class);

		ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
				.getService(ICodeService.class);
		boolean hasCodes = false;
		for (IUri uri : uris) {
			if (codeService.getExplicitCodes(uri.getUri()).size() > 0) {
				hasCodes = true;
				break;
			}
		}

		boolean delete = false;
		if (hasCodes) {
			delete = MessageDialog
					.openQuestion(
							PlatformUI.getWorkbench()
									.getActiveWorkbenchWindow().getShell(),
							"Delete URI" + ((uris.size() != 1) ? "s" : ""),
							"At least one of the selected URIs is coded.\nDo you really want to delete the selected URIs?");
		} else {
			delete = true;
		}

		if (delete) {
			try {
				uriService.removeUris(uris);
			} catch (UriServiceException e) {
				LOGGER.error("Error deleting URIs \"" + uris + "\"", e);
			}
		}

		return null;
	}
}
