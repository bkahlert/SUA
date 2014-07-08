package de.fu_berlin.imp.apiua.core.views;

import java.util.TimeZone;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

import com.bkahlert.nebula.widgets.explanation.note.SimpleNoteComposite;

import de.fu_berlin.imp.apiua.core.model.TimeZoneDate;
import de.fu_berlin.imp.apiua.core.preferences.SUACorePreferenceUtil;

public class TimeZoneDateRangeView extends ViewPart {

	public static final String ID = "de.fu_berlin.imp.apiua.core.views.TimeZoneDateRangeView";

	private SUACorePreferenceUtil preferenceUtil = new SUACorePreferenceUtil();
	private CDateTime startDateTime;
	private CDateTime endDateTime;
	private Button startDateTimeEnabled;
	private Button endDateTimeEnabled;

	private int numColumns = 3;

	public TimeZoneDateRangeView() {

	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(this.numColumns, false));

		this.createHowTo(parent);

		this.createLabel(parent, "Start");
		this.startDateTime = this.createCDateTime(parent);
		this.startDateTimeEnabled = this.createCheckbox(parent);

		this.createLabel(parent, "End");
		this.endDateTime = this.createCDateTime(parent);
		this.endDateTimeEnabled = this.createCheckbox(parent);

		this.configure();
	}

	private SimpleNoteComposite createHowTo(Composite parent) {
		SimpleNoteComposite howTo = new SimpleNoteComposite(parent, SWT.BORDER);
		howTo.setLayoutData(GridDataFactory.fillDefaults()
				.span(this.numColumns, 1).grab(true, true).create());
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
		cDateTime.setPattern(this.preferenceUtil.getDateFormatString());
		return cDateTime;
	}

	private Button createCheckbox(Composite parent) {
		Button button = new Button(parent, SWT.CHECK);
		button.setLayoutData(GridDataFactory.swtDefaults().create());
		return button;
	}

	private void configure() {
		TimeZoneDate dateRangeStart = this.preferenceUtil.getDateRangeStart();
		TimeZoneDate dateRangeEnd = this.preferenceUtil.getDateRangeEnd();
		if (this.startDateTime != null && !this.startDateTime.isDisposed()
				&& dateRangeStart != null) {
			this.startDateTime.setSelection(dateRangeStart.getDate());
			this.startDateTime.setEnabled(this.preferenceUtil
					.getDateRangeStartEnabled());
			this.startDateTime.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					TimeZoneDateRangeView.this.preferenceUtil
							.setDateRangeStart(new TimeZoneDate(
									TimeZoneDateRangeView.this.startDateTime
											.getSelection(), TimeZone
											.getDefault()));
				}
			});
		}
		if (this.endDateTime != null && !this.endDateTime.isDisposed()
				&& dateRangeEnd != null) {
			this.endDateTime.setSelection(dateRangeEnd.getDate());
			this.endDateTime.setEnabled(this.preferenceUtil
					.getDateRangeEndEnabled());
			this.endDateTime.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					TimeZoneDateRangeView.this.preferenceUtil
							.setDateRangeEnd(new TimeZoneDate(
									TimeZoneDateRangeView.this.endDateTime
											.getSelection(), TimeZone
											.getDefault()));
				}
			});
		}
		if (this.startDateTimeEnabled != null
				&& !this.startDateTimeEnabled.isDisposed()) {
			this.startDateTimeEnabled.setSelection(this.preferenceUtil
					.getDateRangeStartEnabled());
			this.startDateTimeEnabled
					.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							boolean enabled = TimeZoneDateRangeView.this.startDateTimeEnabled
									.getSelection();
							TimeZoneDateRangeView.this.preferenceUtil
									.setDateRangeStartEnabled(enabled);
							if (TimeZoneDateRangeView.this.startDateTime != null
									&& !TimeZoneDateRangeView.this.startDateTime
											.isDisposed()) {
								TimeZoneDateRangeView.this.startDateTime
										.setEnabled(enabled);
							}
						}
					});
		}
		if (this.endDateTimeEnabled != null
				&& !this.endDateTimeEnabled.isDisposed()) {
			this.endDateTimeEnabled.setSelection(this.preferenceUtil
					.getDateRangeEndEnabled());
			this.endDateTimeEnabled
					.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							boolean enabled = TimeZoneDateRangeView.this.endDateTimeEnabled
									.getSelection();
							TimeZoneDateRangeView.this.preferenceUtil
									.setDateRangeEndEnabled(enabled);
							if (TimeZoneDateRangeView.this.endDateTime != null
									&& !TimeZoneDateRangeView.this.endDateTime
											.isDisposed()) {
								TimeZoneDateRangeView.this.endDateTime
										.setEnabled(enabled);
							}
						}
					});
		}
	}

	@Override
	public void setFocus() {
		// TODO load if view had no focus
	}

}
