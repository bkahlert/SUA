package de.fu_berlin.imp.apiua.groundedtheory.model.dimension;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

class NominalDimensionRenameDialog extends TitleAreaDialog {

	private Text titleText;

	private String title;

	public NominalDimensionRenameDialog(Shell parentShell, String title) {
		super(parentShell);
		this.title = title;
	}

	@Override
	public void create() {
		super.create();
		this.setTitle("Rename");
		this.setMessage("Please enter the new title for this nominal",
				IMessageProvider.INFORMATION);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		container.setLayout(new FillLayout());

		this.titleText = new Text(container, SWT.BORDER);
		this.titleText.setText(this.title);
		return area;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	private void saveInput() {
		this.title = this.titleText.getText();
	}

	@Override
	protected void okPressed() {
		this.saveInput();
		super.okPressed();
	}

	public String getTitle() {
		return this.title;
	}
}