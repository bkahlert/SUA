package de.fu_berlin.imp.apiua.core.handlers;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.bkahlert.nebula.utils.selection.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.apiua.core.model.IOpenable;

public class OpenHandler extends AbstractHandler {

	private static final Logger LOGGER = Logger.getLogger(OpenHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final List<IOpenable> openables = SelectionRetrieverFactory
				.getSelectionRetriever(IOpenable.class).getSelection();

		if (openables.size() > 0) {
			for (IOpenable openable : openables) {
				try {
					openable.open();
				} catch (Exception e) {
					LOGGER.error("Could not open " + openable, e);
				}
			}
		}

		return null;
	}
}