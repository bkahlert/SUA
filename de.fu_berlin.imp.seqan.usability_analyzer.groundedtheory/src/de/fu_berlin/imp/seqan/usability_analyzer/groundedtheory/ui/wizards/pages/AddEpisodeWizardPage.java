package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.wizards.pages;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class AddEpisodeWizardPage extends WizardPage {
	private static final String DESCRIPTION = "Choose an name for the new episode.";
	private Text episodeCaption;

	public AddEpisodeWizardPage() {
		super(AddEpisodeWizardPage.class.getName());
		setTitle("Define Episode");
		setDescription(DESCRIPTION);
	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		setControl(composite);

		composite.setLayout(GridLayoutFactory.fillDefaults().margins(10, 0)
				.create());

		episodeCaption = new Text(composite, SWT.BORDER);
		episodeCaption.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));
		episodeCaption.setText("");
		episodeCaption.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				updateCompletion();
			}
		});
	}

	private void updateCompletion() {
		if (!episodeCaption.getText().trim().isEmpty()) {
			this.setMessage(DESCRIPTION);
			this.setErrorMessage(null);
			setPageComplete(true);
		} else {
			this.setErrorMessage("The name must not be empty!");
			this.setPageComplete(false);
		}
	}

	public String getEpisodeCaption() {
		return this.episodeCaption.getText();
	}
}