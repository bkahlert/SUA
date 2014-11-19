package de.fu_berlin.imp.apiua.groundedtheory.handlers;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.selection.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.apiua.groundedtheory.model.IRelationInstance;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;

public class RemoveRelationInstanceHandler extends AbstractHandler {

	private static final Logger LOGGER = Logger
			.getLogger(RemoveCodeHandlerInstanceBased.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		List<IRelationInstance> relationInstances = SelectionRetrieverFactory
				.getSelectionRetriever(IRelationInstance.class).getSelection();

		if (relationInstances.size() == 0) {
			return null;
		}

		ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
				.getService(ICodeService.class);

		for (final IRelationInstance relationInstance : relationInstances) {
			try {
				codeService.deleteRelationInstance(relationInstance);
			} catch (Exception e) {
				LOGGER.error("Error removing code", e);
			}
		}

		return null;
	}
}
