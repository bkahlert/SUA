package de.fu_berlin.imp.apiua.uri.handlers;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.bkahlert.nebula.utils.selection.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.apiua.uri.model.IUri;
import de.fu_berlin.imp.apiua.uri.ui.wizards.WizardUtils;

public class EditUriHandler extends AbstractHandler {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(EditUriHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		List<IUri> uris = SelectionRetrieverFactory.getSelectionRetriever(
				IUri.class).getSelection();

		if (uris.size() > 0) {
			IUri uri = uris.get(0);
			WizardUtils.openEditUriWizard(uri);
		}

		return null;
	}
}
