package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.wizards.pages;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.bkahlert.devel.nebula.widgets.ColorPicker;


public class AddEpisodeWizardPage extends WizardPage {
	private static final String DESCRIPTION = "Choose an name for the new episode.";
	private Text episodeCaption;
	private ColorPicker colorPicker;
	private final RGB initialRGB;

	public AddEpisodeWizardPage(RGB rgb) {
		super(AddEpisodeWizardPage.class.getName());
		setTitle("Define Episode");
		setDescription(DESCRIPTION);
		this.initialRGB = rgb;
	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		setControl(composite);

		composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(2)
				.margins(10, 0).create());

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

		colorPicker = new ColorPicker(composite, this.initialRGB);
		colorPicker.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false,
				false));
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

	public RGB getEpisodeRGB() {
		return this.colorPicker.getRGB();
	}
}