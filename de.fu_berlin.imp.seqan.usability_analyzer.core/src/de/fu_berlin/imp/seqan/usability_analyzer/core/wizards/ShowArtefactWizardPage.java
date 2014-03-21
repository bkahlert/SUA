package de.fu_berlin.imp.seqan.usability_analyzer.core.wizards;

import java.net.URISyntaxException;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;

public class ShowArtefactWizardPage extends WizardPage {
	private static final String DESCRIPTION = "Enter or paste the URI of the artefact you want to be shown show.";
	private Text uriText;
	private URI uri;

	public ShowArtefactWizardPage() {
		super(ShowArtefactWizardPage.class.getName());
		this.setTitle("Add Code");
		this.setDescription(DESCRIPTION);
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		this.setControl(composite);

		composite.setLayout(new GridLayout(1, false));

		this.uriText = new Text(composite, SWT.BORDER | SWT.WRAP);
		this.uriText.setLayoutData(GridDataFactory.defaultsFor(this.uriText)
				.hint(SWT.DEFAULT, 50).create());
		this.uriText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				ShowArtefactWizardPage.this
						.updateCompletion(ShowArtefactWizardPage.this.uriText
								.getText());
			}
		});
	}

	private void updateCompletion(String uri) {
		if (!uri.isEmpty()) {
			try {
				this.uri = new URI(uri);
				this.setMessage(DESCRIPTION);
				this.setErrorMessage(null);
				this.setPageComplete(true);
			} catch (RuntimeException e) {
				if (e.getCause() instanceof URISyntaxException) {
					this.setErrorMessage("The URI is invalid");
					this.setPageComplete(false);
				} else {
					this.setErrorMessage("An unknown error has occured!");
					this.setPageComplete(false);
				}
			}
		} else {
			this.setErrorMessage("The URI must not be empty!");
			this.setPageComplete(false);
		}
	}

	public URI getURI() {
		return this.uri;
	}

}