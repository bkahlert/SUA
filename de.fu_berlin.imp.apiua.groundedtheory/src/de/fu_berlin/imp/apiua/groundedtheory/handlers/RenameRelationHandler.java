package de.fu_berlin.imp.apiua.groundedtheory.handlers;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.bkahlert.nebula.utils.selection.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelation;
import de.fu_berlin.imp.apiua.groundedtheory.viewer.RelationViewer;
import de.fu_berlin.imp.apiua.groundedtheory.views.RelationView;

public class RenameRelationHandler extends AbstractHandler {

	private static final Logger LOGGER = Logger
			.getLogger(RenameCodeHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		IWorkbenchPart part = HandlerUtil.getActivePart(event);

		List<IRelation> relations = SelectionRetrieverFactory
				.getSelectionRetriever(IRelation.class).getSelection();

		if (relations.size() > 0) {
			if (part instanceof RelationView) {
				RelationView relationView = (RelationView) part;
				RelationViewer relationViewer = relationView
						.getRelationViewer();
				relationViewer.getViewer().editElement(
						relations.get(0).getUri(), 0);
			}
		} else {
			LOGGER.warn("Selection did not contain any "
					+ ICode.class.getSimpleName());
		}

		return null;
	}

}
