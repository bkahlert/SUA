package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.wizards;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.ExecUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.WorkbenchUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.Activator;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.Episode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.ImageManager;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.wizards.pages.AddEpisodeWizardPage;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.EpisodeView;

public class AddEpisodeWizard extends Wizard {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(AddEpisodeWizard.class);

	public static final String TITLE = "Define Episode";
	public static final ImageDescriptor IMAGE = ImageManager.WIZBAN_CREATE_EPISODE;

	protected AddEpisodeWizardPage addCodeWizardPage;

	private final IIdentifier identifier;
	private final TimeZoneDateRange range;

	public AddEpisodeWizard(IIdentifier identifier, TimeZoneDateRange range) {
		this.setWindowTitle(TITLE);
		this.setDefaultPageImageDescriptor(IMAGE);
		this.setNeedsProgressMonitor(false);
		this.identifier = identifier;
		this.range = range;
		this.addCodeWizardPage = new AddEpisodeWizardPage();
	}

	@Override
	public void addPages() {
		this.addPage(this.addCodeWizardPage);
	}

	@Override
	public boolean performFinish() {
		String name = this.addCodeWizardPage.getEpisodeCaption();
		IEpisode episode = new Episode(this.identifier, this.range, name);

		ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
				.getService(ICodeService.class);
		try {
			codeService.addEpisodeAndSave(episode);
			WorkbenchUtils.getView(EpisodeView.ID);
			return true;
		} catch (final CodeServiceException e) {
			ExecUtils.asyncExec(new Runnable() {
				@Override
				public void run() {
					IStatus status = new Status(IStatus.ERROR,
							Activator.PLUGIN_ID,
							"Error creating/saving a new episode", e);
					ErrorDialog.openError(AddEpisodeWizard.this.getShell(),
							"Code Store Error", status.getMessage(), status);
				}
			});
			return false;
		}
	}
}
