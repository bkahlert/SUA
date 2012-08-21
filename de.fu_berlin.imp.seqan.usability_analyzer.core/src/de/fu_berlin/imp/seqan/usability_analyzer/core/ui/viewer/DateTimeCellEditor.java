package de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer;

import java.util.Date;
import java.util.TimeZone;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;

public class DateTimeCellEditor extends CellEditor {

	private CDateTime cDateTime;

	public DateTimeCellEditor(Composite composite) {
		super(composite);
	}

	@Override
	protected Control createControl(Composite parent) {
		this.cDateTime = new CDateTime(parent, CDT.BORDER | CDT.COMPACT
				| CDT.DATE_LONG | CDT.TIME_MEDIUM | CDT.CLOCK_24_HOUR);
		return this.cDateTime;
	}

	@Override
	protected Object doGetValue() {
		return new TimeZoneDate(cDateTime.getSelection(), TimeZone.getDefault());
	}

	@Override
	protected void doSetFocus() {
		this.cDateTime.setFocus();
	}

	@Override
	protected void doSetValue(Object value) {
		Date date = null;
		if (value instanceof Date)
			date = (Date) value;
		if (value instanceof TimeZoneDate)
			date = ((TimeZoneDate) value).getDate();
		this.cDateTime.setSelection(date);
	}
}
