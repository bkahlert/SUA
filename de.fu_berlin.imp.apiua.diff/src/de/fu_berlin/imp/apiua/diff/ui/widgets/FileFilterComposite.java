package de.fu_berlin.imp.apiua.diff.ui.widgets;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.bkahlert.nebula.widgets.RoundedLabels;

import de.fu_berlin.imp.apiua.diff.preferences.SUADiffPreferenceUtil;
import de.fu_berlin.imp.apiua.diff.ui.dialogs.StringListDialog;

public class FileFilterComposite extends Composite {

	private SUADiffPreferenceUtil diffPreferenceUtil = new SUADiffPreferenceUtil();
	private IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			if (diffPreferenceUtil.fileFilterPatternsChanged(event)) {
				refresh(diffPreferenceUtil.getFileFilterPatterns());
			}
		}
	};

	private Composite parent;
	private RoundedLabels fileFilterPatterns;

	public FileFilterComposite(Composite parent, int style) {
		super(parent, style);
		this.parent = parent;

		this.setLayout(GridLayoutFactory.fillDefaults().numColumns(2)
				.spacing(0, 0).create());

		diffPreferenceUtil.addPropertyChangeListener(propertyChangeListener);

		Label filterLabel = new Label(this, SWT.NONE);
		filterLabel.setLayoutData(GridDataFactory.swtDefaults()
				.align(SWT.BEGINNING, SWT.BEGINNING).indent(0, 4).create());
		filterLabel.setText("Filters:");

		this.fileFilterPatterns = new RoundedLabels(this, SWT.NONE, new RGB(
				200, 200, 200));
		this.fileFilterPatterns.setLayoutData(GridDataFactory.fillDefaults()
				.grab(true, true).span(1, 2).create());
		refresh(diffPreferenceUtil.getFileFilterPatterns());

		Button modifyButton = new Button(this, SWT.NONE);
		modifyButton.setLayoutData(GridDataFactory.swtDefaults()
				.align(SWT.BEGINNING, SWT.BEGINNING).create());
		modifyButton.setText("Modify");
		modifyButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				StringListDialog dialog = new StringListDialog(
						FileFilterComposite.this.getShell(), "File Filters",
						diffPreferenceUtil.getFileFilterPatterns());

				if (dialog.open() == Dialog.OK) {
					String[] newFileFilterPatterns = dialog.getTexts();
					diffPreferenceUtil
							.setFileFilterPatterns(newFileFilterPatterns);
				}
			}
		});
	}

	@Override
	public void dispose() {
		diffPreferenceUtil.removePropertyChangeListener(propertyChangeListener);
		super.dispose();
	}

	private void refresh(String[] fileFilterPatterns) {
		if (this.fileFilterPatterns != null
				&& !this.fileFilterPatterns.isDisposed()) {
			this.fileFilterPatterns.setTexts(fileFilterPatterns);
			if (this.parent != null && !this.parent.isDisposed())
				this.parent.layout();
		}
	}
}
