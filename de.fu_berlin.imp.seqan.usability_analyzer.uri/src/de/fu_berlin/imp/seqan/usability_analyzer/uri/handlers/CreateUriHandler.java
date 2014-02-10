package de.fu_berlin.imp.seqan.usability_analyzer.uri.handlers;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import de.fu_berlin.imp.seqan.usability_analyzer.uri.ui.wizards.WizardUtils;

public class CreateUriHandler extends AbstractHandler {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(CreateUriHandler.class);

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {

		WizardUtils.openCreateUriWizard();

		return null;
	}
}
