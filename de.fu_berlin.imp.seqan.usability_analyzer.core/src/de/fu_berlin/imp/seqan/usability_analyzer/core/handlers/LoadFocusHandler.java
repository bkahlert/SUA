package de.fu_berlin.imp.seqan.usability_analyzer.core.handlers;

import java.util.List;

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

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.ILocatorService;

public class LoadFocusHandler extends AbstractHandler {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(SaveFocusHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final List<URI> focusedElements = new SUACorePreferenceUtil()
				.getFocusedElements();

		Job job = new NamedJob(LoadFocusHandler.class,
				"Loading focused elements") {
			@Override
			protected IStatus runNamed(IProgressMonitor monitor) {
				ILocatorService locatorService = (ILocatorService) PlatformUI
						.getWorkbench().getService(ILocatorService.class);
				locatorService.showInWorkspace(
						focusedElements.toArray(new URI[0]), true, null);
				return Status.OK_STATUS;
			}
		};
		job.schedule();

		return null;
	}
}
