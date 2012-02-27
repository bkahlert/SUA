package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.handlers;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.rcp.selectionUtils.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;

public class DeleteCodeHandler extends AbstractHandler {

	private static final Logger LOGGER = Logger
			.getLogger(DeleteCodeHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		List<ICode> codes = SelectionRetrieverFactory.getSelectionRetriever(
				ICode.class).getSelection();

		boolean delete = MessageDialog.openQuestion(PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getShell(),
				"Delete Code" + ((codes.size() != 1) ? "s" : ""),
				"Do you really want to delete the following codes:\n"
						+ StringUtils.join(codes.toArray()));

		if (delete) {
			ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
					.getService(ICodeService.class);

			for (ICode code : codes) {
				try {
					codeService.deleteCode(code);
				} catch (CodeServiceException e) {
					LOGGER.error("Error deleting code \"" + code + "\"", e);
				}
			}
		}

		return null;
	}
}
