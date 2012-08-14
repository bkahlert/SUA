package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.wizards;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.ExecutorUtil;
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

	private ID id;
	private Fingerprint fingerprint;
	private TimeZoneDateRange range;

	public AddEpisodeWizard(ID id, TimeZoneDateRange range, RGB initialRrgb) {
		this.setWindowTitle(TITLE);
		this.setDefaultPageImageDescriptor(IMAGE);
		this.setNeedsProgressMonitor(false);
		this.id = id;
		this.fingerprint = null;
		this.range = range;
		this.addCodeWizardPage = new AddEpisodeWizardPage(initialRrgb);
	}

	public AddEpisodeWizard(Fingerprint fingerprint, TimeZoneDateRange range,
			RGB rgb) {
		this.setWindowTitle(TITLE);
		this.setDefaultPageImageDescriptor(IMAGE);
		this.setNeedsProgressMonitor(false);
		this.id = null;
		this.fingerprint = fingerprint;
		this.range = range;
		this.addCodeWizardPage = new AddEpisodeWizardPage(rgb);
	}

	@Override
	public void addPages() {
		this.addPage(addCodeWizardPage);
	}

	@Override
	public boolean performFinish() {
		String name = addCodeWizardPage.getEpisodeCaption();
		RGB rgb = addCodeWizardPage.getEpisodeRGB();
		IEpisode episode;

		// plugin.xml makes sure the objects only contain a single ID or
		// Fingerprint
		if (id != null)
			episode = new Episode(id, range, name, rgb);
		else
			episode = new Episode(fingerprint, range, name, rgb);

		ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
				.getService(ICodeService.class);
		try {
			codeService.addEpisodeAndSave(episode);
			WorkbenchUtils.getView(EpisodeView.ID);
			return true;
		} catch (final CodeServiceException e) {
			ExecutorUtil.syncExec(new Runnable() {
				@Override
				public void run() {
					IStatus status = new Status(IStatus.ERROR,
							Activator.PLUGIN_ID,
							"Error creating/saving a new episode", e);
					ErrorDialog.openError(getShell(), "Code Store Error",
							status.getMessage(), status);
				}
			});
			return false;
		}
	}
}
