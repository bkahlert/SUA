package de.fu_berlin.imp.seqan.usability_analyzer.diff.views.controls;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

import de.fu_berlin.imp.seqan.usability_analyzer.diff.extensionProviders.FileFilterUtil;

public class BinFileFilterButton extends WorkbenchWindowControlContribution {

	private static final FileFilter binFileFilter = new FileFilter() {
		private final Pattern pattern = Pattern.compile("^bin/.*",
				Pattern.CASE_INSENSITIVE);

		@Override
		public boolean accept(File pathname) {
			String path = pathname.toString();
			return !pattern.matcher(path).matches();
		}
	};

	public BinFileFilterButton() {
	}

	public BinFileFilterButton(String id) {
		super(id);
	}

	@Override
	protected Control createControl(Composite parent) {
		final Button button = new Button(parent, SWT.CHECK);
		button.setText("Exlude bin/*");
		button.setSelection(false);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (button.getSelection()) {
					FileFilterUtil.notifyFileFilterAdded(binFileFilter);
				} else {
					FileFilterUtil.notifyFileFilterRemoved(binFileFilter);
				}
			}
		});
		return button;
	}
}
