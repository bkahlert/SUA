package de.fu_berlin.imp.apiua.diff.ui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import de.fu_berlin.imp.apiua.diff.ui.widgets.StringListWidget;

public class StringListDialog extends Dialog {

	private String title;
	private String[] strings;
	private StringListWidget stringListWidget;
	private String[] savedTexts;

	public StringListDialog(Shell parentShell, String title, String[] strings) {
		super(parentShell);
		this.title = title;
		this.strings = strings;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(this.title);
	}

	@Override
	protected Point getInitialSize() {
		Rectangle bounds = this.getContents().getBounds();
		return new Point(bounds.width + 50, bounds.height + 150);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new FillLayout());
		stringListWidget = new StringListWidget(composite, SWT.NONE);
		stringListWidget.setTexts(strings);
		parent.pack();
		return composite;
	}

	@Override
	protected void okPressed() {
		this.savedTexts = this.stringListWidget.getTexts();
		super.okPressed();
	}

	public String[] getTexts() {
		return this.savedTexts;
	}
}
