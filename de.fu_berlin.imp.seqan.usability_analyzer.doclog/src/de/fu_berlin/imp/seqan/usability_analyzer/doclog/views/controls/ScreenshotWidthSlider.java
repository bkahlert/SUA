package de.fu_berlin.imp.seqan.usability_analyzer.doclog.views.controls;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

import de.fu_berlin.imp.seqan.usability_analyzer.doclog.Activator;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.preferences.PreferenceUtil;

public class ScreenshotWidthSlider extends WorkbenchWindowControlContribution {

	private IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			if (preferenceUtil.screenshotWidthChanged(event)) {
				if (slider != null && !slider.isDisposed())
					slider.setSelection((Integer) event.getNewValue());
			}
		}
	};

	private PreferenceUtil preferenceUtil = new PreferenceUtil();
	private Slider slider;

	public ScreenshotWidthSlider() {
		Activator.getDefault().getPreferenceStore()
				.addPropertyChangeListener(propertyChangeListener);
	}

	@Override
	protected Control createControl(Composite parent) {
		slider = new Slider(parent, SWT.NONE);
		slider.setMinimum(100);
		slider.setMaximum(1000);
		slider.setIncrement(10);
		slider.setPageIncrement(100);
		slider.setSelection(preferenceUtil.getScreenshotWidth());
		slider.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				preferenceUtil.setScreenshotWidth(slider.getSelection());
			}
		});
		return slider;
	}

	@Override
	public void dispose() {
		Activator.getDefault().getPreferenceStore()
				.removePropertyChangeListener(propertyChangeListener);
		super.dispose();
	}

}
