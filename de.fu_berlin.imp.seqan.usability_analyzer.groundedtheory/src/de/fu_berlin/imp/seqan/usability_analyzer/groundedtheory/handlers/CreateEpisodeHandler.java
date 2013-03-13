package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.handlers;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.bkahlert.devel.rcp.selectionUtils.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.HasIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.wizards.WizardUtils;

public class CreateEpisodeHandler extends AbstractHandler {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(CreateEpisodeHandler.class);

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {

		final List<HasDateRange> objects = SelectionRetrieverFactory
				.getSelectionRetriever(HasDateRange.class).getSelection();

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
