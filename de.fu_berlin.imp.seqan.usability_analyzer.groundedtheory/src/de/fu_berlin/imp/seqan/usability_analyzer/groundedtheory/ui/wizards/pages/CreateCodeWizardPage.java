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

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.inf.nebula.utils.LayoutUtils;

/**
 * Allows the user to create a new {@link ICode}
 * 
 * @author bkahlert
 */
public class CreateCodeWizardPage extends WizardPage {
	private static final String DESCRIPTION = "Type in the name for the new code.";
	private Text newCodeCaption;

	public CreateCodeWizardPage() {
		super(CreateCodeWizardPage.class.getName());
		setTitle("Create Code");
		setDescription(DESCRIPTION);
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		setControl(composite);

		composite.setLayout(LayoutUtils.createGridLayout(2, false, 10, 0));
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
}