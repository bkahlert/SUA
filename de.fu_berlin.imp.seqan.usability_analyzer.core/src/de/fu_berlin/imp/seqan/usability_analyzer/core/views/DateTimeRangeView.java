package de.fu_berlin.imp.seqan.usability_analyzer.core.views;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.inf.nebula.explanation.note.SimpleNoteComposite;

public class DateTimeRangeView extends ViewPart {

	private SUACorePreferenceUtil preferenceUtil = new SUACorePreferenceUtil();
	private CDateTime startDateTime;
	private CDateTime endDateTime;

	public DateTimeRangeView() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(2, false));

		this.createHowTo(parent);

		this.createLabel(parent, "Start");
		this.startDateTime = this.createCDateTime(parent);

		this.createLabel(parent, "End");
		this.endDateTime = createCDateTime(parent);

		configure();
	}

	private SimpleNoteComposite createHowTo(Composite parent) {
		SimpleNoteComposite howTo = new SimpleNoteComposite(parent, SWT.BORDER);
		howTo.setLayoutData(GridDataFactory.swtDefaults().span(2, 1)
				.grab(true, false).create());
		howTo.setText("If you want to narrow down the set of data you "
				+ "want to evaluate you can do this based "
				+ "on the date and time the data were generated.");
		howTo.setImage(SWT.ICON_INFORMATION);
		return howTo;
	}

	private Label createLabel(Composite parent, String text) {
		Label label = new Label(parent, SWT.NONE);
		label.setLayoutData(GridDataFactory.swtDefaults().create());
		label.setText(text);
		return label;
	}

	private CDateTime createCDateTime(Composite parent) {
		CDateTime cDateTime = new CDateTime(parent, CDT.BORDER | CDT.COMPACT
				| CDT.DROP_DOWN | CDT.DATE_LONG | CDT.TIME_MEDIUM
				| CDT.CLOCK_24_HOUR);
		cDateTime.setLayoutData(GridDataFactory.fillDefaults()
				.grab(true, false).create());
		cDateTime.setPattern(preferenceUtil.getDateFormatString());
		return cDateTime;
	}

	private void configure() {
		if (startDateTime != null && !startDateTime.isDisposed()) {
			startDateTime.setSelection(preferenceUtil.getDateRangeStart());
			startDateTime.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					preferenceUtil.setDateRangeStart(startDateTime
							.getSelection());
				}
			});
		}
		if (endDateTime != null && !endDateTime.isDisposed()) {
			endDateTime.setSelection(preferenceUtil.getDateRangeEnd());
			endDateTime.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					preferenceUtil.setDateRangeEnd(endDateTime.getSelection());
				}
			});
		}
	}

	@Override
	public void setFocus() {
	}

}
