package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.handlers;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.bkahlert.devel.rcp.selectionUtils.retriever.SelectionRetrieverFactory;
import com.bkahlert.nebula.information.InformationControlManagerUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.Utils;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.wizards.WizardUtils;

public class AddCodeInstanceHandler extends AbstractHandler {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(AddCodeInstanceHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		List<ILocatable> locatables = SelectionRetrieverFactory
				.getSelectionRetriever(ILocatable.class).getSelection();
		if (InformationControlManagerUtils.getCurrentInput() instanceof ILocatable) {
			ILocatable input = (ILocatable) InformationControlManagerUtils
					.getCurrentInput();
			if (!locatables.contains(input)) {
				locatables.add(input);
			}
		}
		List<URI> uris = new ArrayList<URI>();
		for (ILocatable locatable : locatables) {
			uris.add(locatable.getUri());
		}

		if (locatables.size() > 0) {
			WizardUtils.openAddCodeWizard(uris, Utils.getFancyCodeColor());
		}

		return null;
	}
}
