package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.handlers;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.bkahlert.devel.rcp.selectionUtils.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.HasFingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.HasID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
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

		if (objects.get(0) instanceof HasID
				&& ((HasID) objects.get(0)).getID() != null)
			WizardUtils.openAddEpisodeWizard(((HasID) objects.get(0)).getID(),
					range);
		else
			WizardUtils.openAddEpisodeWizard(
					((HasFingerprint) objects.get(0)).getFingerprint(), range);

		return null;
	}
}
