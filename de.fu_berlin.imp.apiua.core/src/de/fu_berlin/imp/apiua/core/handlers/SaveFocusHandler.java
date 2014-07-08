package de.fu_berlin.imp.apiua.core.handlers;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.bkahlert.nebula.utils.selection.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.preferences.SUACorePreferenceUtil;

public class SaveFocusHandler extends AbstractHandler {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(SaveFocusHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final List<URI> uris = SelectionRetrieverFactory.getSelectionRetriever(
				URI.class).getSelection();

		if (uris.size() > 0) {
			new SUACorePreferenceUtil().setFocusedElements(uris);
		}

		return null;
	}
}