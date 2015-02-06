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

import de.fu_berlin.imp.apiua.groundedtheory.model.IRelation;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelationInstance;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions.CodeStoreWriteException;
import de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions.RelationDoesNotExistException;

public class DeleteRelationHandler extends AbstractHandler {

	private static final Logger LOGGER = Logger
			.getLogger(DeleteCodeHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
				.getService(ICodeService.class);

		List<IRelation> relations = SelectionRetrieverFactory
				.getSelectionRetriever(IRelation.class).getSelection();

		List<IRelationInstance> relationInstances = new LinkedList<IRelationInstance>();
		for (IRelation relation : relations) {
			relationInstances
					.addAll(codeService.getExplicitRelationInstances(relation));
		}

		if (relationInstances.size() == 0) {
			for (IRelation relation : relations) {
				try {
					codeService.deleteRelation(relation);
				} catch (CodeStoreWriteException
						| RelationDoesNotExistException e) {
					LOGGER.error("Error deleting "
							+ IRelation.class.getSimpleName() + ": " + relation);
				}
			}
		} else {
			boolean delete = MessageDialog.openQuestion(
					PlatformUI.getWorkbench().getActiveWorkbenchWindow()
							.getShell(),
					"Delete Relation" + ((relations.size() != 1) ? "s" : ""),
					"The relation"
							+ ((relations.size() != 1) ? "s" : "")
							+ " ("
							+ StringUtils.join(relations, ", ")
							+ ") you are trying to delete are still in"
							+ " use by the following artefact"
							+ ((relationInstances.size() != 1) ? "s" : "")
							+ ":\n"
							+ StringUtils.join(relationInstances.toArray(),
									", ")
							+ "\n\nDo you really want to delete the relation"
							+ ((relations.size() != 1) ? "s" : "") + "?");
			if (delete) {
				for (IRelation relation : relations) {
					try {
						codeService.deleteRelation(relation);
					} catch (CodeStoreWriteException
							| RelationDoesNotExistException e) {
						LOGGER.error("Error deleting relation \"" + relation
								+ "\"", e);
					}
				}
			}
			;
		}

		return null;
	}
}
