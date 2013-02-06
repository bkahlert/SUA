package de.fu_berlin.imp.seqan.usability_analyzer.timeline.ui.viewer;

import java.util.Calendar;

import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.nebula.viewer.timeline.ITimelineLabelProvider;
import com.bkahlert.devel.nebula.widgets.timeline.IDecorator;
import com.bkahlert.devel.nebula.widgets.timeline.IHotZone;
import com.bkahlert.devel.nebula.widgets.timeline.impl.Decorator;
import com.bkahlert.devel.nebula.widgets.timeline.impl.HotZone;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IDataSetInfo;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IDataService;

public class TimelineLabelProvider implements ITimelineLabelProvider {

	private IDataSetInfo dataSetInfo;
	private Object key = null;

	public TimelineLabelProvider(Object key) {
		this.dataSetInfo = ((IDataService) PlatformUI.getWorkbench()
				.getService(IDataService.class)).getActiveDataDirectories()
				.get(0).getInfo();
		this.key = key;
	}

	@Override
	public String getTitle() {
		String title;
		if (key instanceof ID) {
			title = "ID: " + key.toString();
		} else if (key instanceof Fingerprint) {
			title = "Fingerprint: " + key.toString();
		} else {
			title = "INVALID TYPE";
		}
		return title;
	}

	@Override
	public Calendar getCenterStart() {
		TimeZoneDate centerStartDate = this.dataSetInfo.getDateRange() != null ? this.dataSetInfo
				.getDateRange().getStartDate() : null;
		return centerStartDate != null ? centerStartDate.getCalendar() : null;
	}

	@Override
	public IHotZone[] getHotZones() {
		return new IHotZone[] { new HotZone(dataSetInfo.getDateRange()
				.getStartDate().toISO8601(), dataSetInfo.getDateRange()
				.getEndDate().toISO8601()) };
	}

	@Override
	public IDecorator[] getDecorators() {
		TimeZoneDateRange range = dataSetInfo.getDateRange();

		Calendar decoratorStart = range.getStartDate() != null ? range
				.getStartDate().getCalendar() : null;
		Calendar decoratorEnd = range.getEndDate() != null ? range.getEndDate()
				.getCalendar() : null;

		return new IDecorator[] { new Decorator(decoratorStart,
				dataSetInfo.getName(), decoratorEnd, dataSetInfo.getName()) };
	}

	@Override
	public Float getTimeZone() {
		TimeZoneDateRange range = dataSetInfo.getDateRange();
		if (range.getStartDate() != null) {
			return (range.getStartDate().getLocalTime() - range.getStartDate()
					.getTime()) / 3600000f; // e.g. 2
											// or
											// -5.5
		}
		return null;
	}

	@Override
	public Float getTapeImpreciseOpacity() {
		return 0.5f;
	}

	@Override
	public Integer getIconWidth() {
		return 16;
	}

	@Override
	public String[] getBubbleFunction() {
		return null;
	}

}
