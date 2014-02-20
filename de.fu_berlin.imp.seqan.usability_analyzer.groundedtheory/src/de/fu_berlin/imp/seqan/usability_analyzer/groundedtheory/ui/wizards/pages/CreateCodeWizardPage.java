package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.wizards.pages;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.bkahlert.nebula.utils.LayoutUtils;
import com.bkahlert.nebula.utils.colors.RGB;
import com.bkahlert.nebula.widgets.ColorPicker;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;

/**
 * Allows the user to create a new {@link ICode}
 * 
 * @author bkahlert
 */
public class CreateCodeWizardPage extends WizardPage {
	private static final String DESCRIPTION = "Type in the name for the new code.";
	private Text newCodeCaption;
	private RGB initialColor;
	private ColorPicker colorPicker;

	public CreateCodeWizardPage(RGB initialColor) {
		super(CreateCodeWizardPage.class.getName());
		setTitle("Create Code");
		setDescription(DESCRIPTION);
		this.initialColor = initialColor;
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		setControl(composite);

		composite.setLayout(LayoutUtils.createGridLayout(3, false, 10, 10));
		int space = new GridLayout().horizontalSpacing;
		GridData gridData;

		/*
		 * Row 1
		 */
		Label jidLabel = new Label(composite, SWT.NONE);
		gridData = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		jidLabel.setLayoutData(gridData);
		jidLabel.setText("Name");

		Text jidCombo = new Text(composite, SWT.BORDER);
		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gridData.horizontalIndent = space;
		jidCombo.setLayoutData(gridData);
		this.newCodeCaption = jidCombo;
		this.newCodeCaption.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				updateCompletion();
			}
		});

		colorPicker = new ColorPicker(composite,
				this.initialColor.toClassicRGB());
		colorPicker.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false,
				false));
	}

	private void updateCompletion() {
		if (!getNewCodeCaption().isEmpty()) {
			this.setMessage(DESCRIPTION);
			this.setErrorMessage(null);
			setPageComplete(true);
		} else {
			this.setErrorMessage("Please type in a name for the new code!");
			this.setPageComplete(false);
		}
	}

	public String getNewCodeCaption() {
		return this.newCodeCaption.getText();
	}

	public RGB getNewCodeColor() {
		return new RGB(this.colorPicker.getRGB());
	}
}