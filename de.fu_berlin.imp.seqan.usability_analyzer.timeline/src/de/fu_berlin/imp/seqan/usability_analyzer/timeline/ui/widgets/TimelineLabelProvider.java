package de.fu_berlin.imp.seqan.usability_analyzer.timeline.ui.widgets;

import java.util.Calendar;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineLabelProvider;
import com.bkahlert.devel.nebula.widgets.timeline.IBaseTimeline;
import com.bkahlert.devel.nebula.widgets.timeline.impl.Decorator;
import com.bkahlert.devel.nebula.widgets.timeline.impl.HotZone;
import com.bkahlert.devel.nebula.widgets.timeline.model.IDecorator;
import com.bkahlert.devel.nebula.widgets.timeline.model.IHotZone;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IDataSetInfo;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IDataService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.ExecutorUtil;

public class TimelineLabelProvider<TIMELINE extends IBaseTimeline> implements
		ITimelineLabelProvider<TIMELINE> {

	private static final Logger LOGGER = Logger
			.getLogger(TimelineLabelProvider.class);

	private static <TIMELINE extends IBaseTimeline> Object getKey(
			final TIMELINE timeline) {
		try {
			return ExecutorUtil.syncExec(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					return timeline.getData();
				}
			});
		} catch (Exception e) {
			LOGGER.error("Error retrieving the timeline's key", e);
		}
		return null;
	}

	private static IDataSetInfo getDataSetInfo() {
		return ((IDataService) PlatformUI.getWorkbench().getService(
				IDataService.class)).getActiveDataDirectories().get(0)
				.getInfo();
	}

	@Override
	public String getTitle(TIMELINE timeline) {
		Object key = getKey(timeline);
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
	public Calendar getCenterStart(TIMELINE timeline) {
		IDataSetInfo dataSetInfo = getDataSetInfo();
		TimeZoneDate centerStartDate = dataSetInfo.getDateRange() != null ? dataSetInfo
				.getDateRange().getStartDate() : null;
		return centerStartDate != null ? centerStartDate.getCalendar() : null;
	}

	@Override
	public IHotZone[] getHotZones(TIMELINE timeline) {
		IDataSetInfo dataSetInfo = getDataSetInfo();
		return new IHotZone[] { new HotZone(dataSetInfo.getDateRange()
				.getStartDate().toISO8601(), dataSetInfo.getDateRange()
				.getEndDate().toISO8601()) };
	}

	@Override
	public IDecorator[] getDecorators(TIMELINE timeline) {
		IDataSetInfo dataSetInfo = getDataSetInfo();
		TimeZoneDateRange range = dataSetInfo.getDateRange();

		Calendar decoratorStart = range.getStartDate() != null ? range
				.getStartDate().getCalendar() : null;
		Calendar decoratorEnd = range.getEndDate() != null ? range.getEndDate()
				.getCalendar() : null;

		return new IDecorator[] { new Decorator(decoratorStart,
				dataSetInfo.getName(), decoratorEnd, dataSetInfo.getName()) };
	}

	@Override
	public Float getTimeZone(TIMELINE timeline) {
		IDataSetInfo dataSetInfo = getDataSetInfo();
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
	public Float getTapeImpreciseOpacity(TIMELINE timeline) {
		return 0.5f;
	}

	@Override
	public Integer getIconWidth(TIMELINE timeline) {
		return 16;
	}

	@Override
	public String[] getBubbleFunction(TIMELINE timeline) {
		return null;
	}

}
