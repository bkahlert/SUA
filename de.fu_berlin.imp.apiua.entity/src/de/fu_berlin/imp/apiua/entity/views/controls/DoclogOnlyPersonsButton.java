package de.fu_berlin.imp.apiua.entity.views.controls;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import de.fu_berlin.imp.apiua.core.model.DataSource;
import de.fu_berlin.imp.apiua.entity.preferences.SUAEntityPreferenceUtil;

public class DoclogOnlyPersonsButton extends DataOnlyPersonsButton {

	@Override
	protected Control createControl(Composite parent) {
		final Button button = new Button(parent, SWT.CHECK);
		button.setText("Doclog");
		button.setSelection(new SUAEntityPreferenceUtil()
				.getFilterdDataSources().contains(DataSource.DOCLOG));
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setDataSourceFilter(DataSource.DOCLOG, button.getSelection());
			}
		});
		return button;
	}
}
