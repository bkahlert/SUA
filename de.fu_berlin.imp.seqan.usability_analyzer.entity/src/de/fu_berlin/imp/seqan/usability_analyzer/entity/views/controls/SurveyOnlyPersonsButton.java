package de.fu_berlin.imp.seqan.usability_analyzer.entity.views.controls;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DataSource;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.preferences.SUAEntityPreferenceUtil;

public class SurveyOnlyPersonsButton extends DataOnlyPersonsButton {

	@Override
	protected Control createControl(Composite parent) {
		final Button button = new Button(parent, SWT.CHECK);
		button.setText("Survey");
		button.setSelection(new SUAEntityPreferenceUtil()
				.getFilterdDataSources().contains(DataSource.SURVEYRECORD));
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setDataSourceFilter(DataSource.SURVEYRECORD,
						button.getSelection());
			}
		});
		return button;
	}
}
