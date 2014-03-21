package de.fu_berlin.imp.seqan.usability_analyzer.core.wizards;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.Wizard;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;

public class ShowArtefactWizard extends Wizard {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(ShowArtefactWizard.class);

	public static final String TITLE = "Show Artefact";
	public static final ImageDescriptor IMAGE = null;

	protected final ShowArtefactWizardPage showArtefactWizardPage;

	private URI uri = null;

	public ShowArtefactWizard() {
		this.setWindowTitle(TITLE);
		this.setDefaultPageImageDescriptor(IMAGE);
		this.setNeedsProgressMonitor(false);
		this.showArtefactWizardPage = new ShowArtefactWizardPage();
	}

	@Override
	public void addPages() {
		this.addPage(this.showArtefactWizardPage);
	}

	@Override
	public boolean performFinish() {
		if (this.showArtefactWizardPage.getURI() != null) {
			this.uri = this.showArtefactWizardPage.getURI();
		}

		return true;
	}

	public URI getURI() {
		return this.uri;
	}
}
