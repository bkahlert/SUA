package de.fu_berlin.imp.apiua.diff.handlers;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.selection.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.apiua.diff.model.ICompilable;
import de.fu_berlin.imp.apiua.diff.services.ICompilationService;

public class SetCompilationStateHandler extends AbstractHandler {

	private static final Logger LOGGER = Logger
			.getLogger(SetCompilationStateHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final List<ICompilable> compilables = SelectionRetrieverFactory
				.getSelectionRetriever(ICompilable.class).getSelection();

		Boolean compilationState;
		String compilationStateParameter = event
				.getParameter("de.fu_berlin.imp.apiua.diff.setCompilationState.compiles");
		if (compilationStateParameter != null
				&& compilationStateParameter.equalsIgnoreCase("true")) {
			compilationState = true;
		} else if (compilationStateParameter != null
				&& compilationStateParameter.equalsIgnoreCase("false")) {
			compilationState = false;
		} else {
			compilationState = null;
		}

		ICompilationService compilationService = (ICompilationService) PlatformUI
				.getWorkbench().getService(ICompilationService.class);
		if (!compilationService.compiles(
				compilables.toArray(new ICompilable[0]), compilationState)) {
			LOGGER.warn("Error setting compilation state for " + compilables
					+ " to " + compilationStateParameter);
		}

		return null;
	}
}