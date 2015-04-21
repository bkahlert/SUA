package de.fu_berlin.imp.apiua.groundedtheory.handlers;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.selection.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICodeInstance;
import de.fu_berlin.imp.apiua.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;

public class DeleteCodeHandler extends AbstractHandler {

	private static final Logger LOGGER = Logger
			.getLogger(DeleteCodeHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
				.getService(ICodeService.class);

		List<ICode> codes = SelectionRetrieverFactory.getSelectionRetriever(
				ICode.class).getSelection();

		boolean childCodesExist = false;
		for (ICode code : codes) {
			if (codeService.getChildren(code).size() > 0) {
				childCodesExist = true;
				break;
			}
		}
		if (childCodesExist) {
			MessageDialog.openError(PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getShell(), "Delete Code"
					+ ((codes.size() != 1) ? "s" : ""),
					"The code" + ((codes.size() != 1) ? "s have" : " has")
							+ " child codes.\n"
							+ "You need to manually delete all child codes "
							+ "before deleting the selected ones.");
			return null;
		}

		List<ICodeInstance> codeInstances = new LinkedList<ICodeInstance>();
		for (ICode code : codes) {
			codeInstances.addAll(codeService.getExplicitInstances(code));
		}

		if (codeInstances.size() == 0) {
			for (ICode code : codes) {
				try {
					codeService.deleteCode(code);
				} catch (CodeServiceException e) {
					LOGGER.error("Error deleting "
							+ ICode.class.getSimpleName() + ": " + code + "\n"
							+ e);
					MessageDialog.openError(PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getShell(),
							"Delete Code", e.getMessage());
				}
			}
		} else {
			boolean delete = MessageDialog.openQuestion(PlatformUI
					.getWorkbench().getActiveWorkbenchWindow().getShell(),
					"Delete Code" + ((codes.size() != 1) ? "s" : ""),
					"The code" + ((codes.size() != 1) ? "s" : "") + " ("
							+ StringUtils.join(codes, ", ")
							+ ") you are trying to delete are still in"
							+ " use by the following artefact"
							+ ((codeInstances.size() != 1) ? "s" : "") + ":\n"
							+ StringUtils.join(codeInstances.toArray(), ", ")
							+ "\n\nDo you really want to delete the code"
							+ ((codes.size() != 1) ? "s" : "") + "?");
			if (delete) {
				for (ICode code : codes) {
					try {
						codeService.deleteCode(code, true);
					} catch (CodeServiceException e) {
						LOGGER.error("Error deleting code \"" + code + "\"", e);
					}
				}
			}
			;
		}

		return null;
	}
}
