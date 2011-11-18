package de.fu_berlin.imp.seqan.usability_analyzer.diff.views.controls;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

import com.bkahlert.devel.nebula.widgets.RoundedLabels;

import de.fu_berlin.imp.seqan.usability_analyzer.diff.preferences.SUADiffPreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.ui.dialogs.StringListDialog;

public class FileFilterPatternsText extends WorkbenchWindowControlContribution {

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

	public FileFilterPatternsText() {
		diffPreferenceUtil.addPropertyChangeListener(propertyChangeListener);
	}

	public FileFilterPatternsText(String id) {
		super(id);
		diffPreferenceUtil.addPropertyChangeListener(propertyChangeListener);
	}

	@Override
	public void dispose() {
		diffPreferenceUtil.removePropertyChangeListener(propertyChangeListener);
		super.dispose();
	}

	@Override
	protected Control createControl(final Composite parent) {
		this.parent = parent;
		Composite wrapper = new Composite(parent, SWT.NONE);
		wrapper.setLayout(GridLayoutFactory.swtDefaults().margins(2, 2)
				.spacing(2, 0).numColumns(2).create());
		new Label(wrapper, SWT.NONE).setText("Filters:");
		this.fileFilterPatterns = new RoundedLabels(wrapper, SWT.NONE, new RGB(
				200, 200, 200));
		this.fileFilterPatterns.setMargin(0, 0);
		wrapper.addListener(SWT.MouseEnter, new Listener() {
			@Override
			public void handleEvent(Event event) {
				StringListDialog dialog = new StringListDialog(parent
						.getShell(), "File Filters", diffPreferenceUtil
						.getFileFilterPatterns());

				if (dialog.open() == Dialog.OK) {
					String[] newFileFilterPatterns = dialog.getTexts();
					diffPreferenceUtil
							.setFileFilterPatterns(newFileFilterPatterns);
				}
			}
		});
		refresh(diffPreferenceUtil.getFileFilterPatterns());
		return wrapper;
	}

	private void refresh(String[] fileFilterPatterns) {
		if (this.fileFilterPatterns != null
				&& !this.fileFilterPatterns.isDisposed()) {
			this.fileFilterPatterns.setTexts(fileFilterPatterns);
			this.parent.layout();
		}
	}
}
