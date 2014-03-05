package de.fu_berlin.imp.seqan.usability_analyzer.uri.ui.wizards;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.ExecUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.Activator;
import de.fu_berlin.imp.seqan.usability_analyzer.uri.ImageManager;
import de.fu_berlin.imp.seqan.usability_analyzer.uri.model.IUri;
import de.fu_berlin.imp.seqan.usability_analyzer.uri.services.IUriService;
import de.fu_berlin.imp.seqan.usability_analyzer.uri.services.UriServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.uri.ui.wizards.pages.UriWizardPage;

public class UriWizard extends Wizard {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(UriWizard.class);

	public static final String TITLE = "Define URI";
	public static final ImageDescriptor IMAGE = ImageManager.WIZBAN_CREATE_URI;
	public static final ImageDescriptor IMAGE_EDIT = ImageManager.WIZBAN_EDIT_URI;

	protected UriWizardPage uriWizardPage;

	private final IUri editUri;

	public UriWizard() {
		this.setWindowTitle(TITLE);
		this.setDefaultPageImageDescriptor(IMAGE);
		this.setNeedsProgressMonitor(false);
		this.uriWizardPage = new UriWizardPage();
		this.editUri = null;
	}

	public UriWizard(IUri editUri) {
		this.setWindowTitle(TITLE);
		this.setDefaultPageImageDescriptor(IMAGE_EDIT);
		this.setNeedsProgressMonitor(false);
		this.uriWizardPage = new UriWizardPage(editUri);
		this.editUri = editUri;
	}

	@Override
	public void addPages() {
		this.addPage(this.uriWizardPage);
	}

	@Override
	public boolean performFinish() {
		IUri uri = this.uriWizardPage.getURI();

		IUriService uriService = (IUriService) PlatformUI.getWorkbench()
				.getService(IUriService.class);
		try {
			if (this.editUri != null) {
				uriService.replaceUri(this.editUri, uri);
			} else {
				uriService.addUri(uri);
			}
			return true;
		} catch (final UriServiceException e) {
			ExecUtils.asyncExec(new Runnable() {
				@Override
				public void run() {
					IStatus status = new Status(IStatus.ERROR,
							Activator.PLUGIN_ID,
							"Error creating/saving a new URIXX", e);
					ErrorDialog.openError(UriWizard.this.getShell(),
							"URI Creation Error", status.getMessage(), status);
				}
			});
			return false;
		}
	}
}
