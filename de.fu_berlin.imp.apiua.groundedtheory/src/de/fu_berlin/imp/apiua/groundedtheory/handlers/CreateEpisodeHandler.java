package de.fu_berlin.imp.apiua.groundedtheory.handlers;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.bkahlert.nebula.information.InformationControlManagerUtils;
import com.bkahlert.nebula.utils.selection.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.apiua.core.model.HasIdentifier;
import de.fu_berlin.imp.apiua.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.apiua.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.apiua.core.ui.viewer.filters.HasDateRange;
import de.fu_berlin.imp.apiua.groundedtheory.ui.wizards.WizardUtils;

public class CreateEpisodeHandler extends AbstractHandler {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(CreateEpisodeHandler.class);

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {

		final List<HasDateRange> objects = SelectionRetrieverFactory
				.getSelectionRetriever(HasDateRange.class).getSelection();
		if (InformationControlManagerUtils.getCurrentInput() instanceof HasDateRange) {
			HasDateRange input = (HasDateRange) InformationControlManagerUtils
					.getCurrentInput();
			if (!objects.contains(input)) {
				objects.add(input);
			}
		}

		TimeZoneDateRange range = TimeZoneDateRange
				.calculateOuterDateRange(objects.toArray(new HasDateRange[0]));
		IIdentifier identifier = null;
		if (objects.get(0) instanceof HasIdentifier) {
			identifier = ((HasIdentifier) objects.get(0)).getIdentifier();
		}
		if (identifier != null) {
			WizardUtils.openAddEpisodeWizard(identifier, range);
		}

		return null;
	}
}
