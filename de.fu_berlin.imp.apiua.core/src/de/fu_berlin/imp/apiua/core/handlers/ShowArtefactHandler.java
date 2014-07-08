package de.fu_berlin.imp.apiua.core.handlers;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.NamedJob;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.services.location.ILocatorService;
import de.fu_berlin.imp.apiua.core.wizards.ShowArtefactWizard;
import de.fu_berlin.imp.apiua.core.wizards.WizardUtils;

public class ShowArtefactHandler extends AbstractHandler {

	private static final Logger LOGGER = Logger
			.getLogger(ShowArtefactHandler.class);

	private final ILocatorService locatorService = (ILocatorService) PlatformUI
			.getWorkbench().getService(ILocatorService.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			ShowArtefactWizard wizard = WizardUtils.openShowArtefactWizard();
			final URI uri = wizard.getURI();
			Job job = new NamedJob(ShowArtefactHandler.class,
					"Showing artefact") {
				@Override
				protected IStatus runNamed(IProgressMonitor monitor) {
					try {
						ShowArtefactHandler.this.locatorService
								.showInWorkspace(uri, true, monitor).get();
					} catch (Exception e) {
						LOGGER.error("Error showing artefact", e);
						return Status.CANCEL_STATUS;
					}
					return Status.OK_STATUS;
				}
			};
			job.schedule();
		} catch (Exception e) {
			throw new ExecutionException("Error showing artefact", e);
		}

		return null;
	}

}