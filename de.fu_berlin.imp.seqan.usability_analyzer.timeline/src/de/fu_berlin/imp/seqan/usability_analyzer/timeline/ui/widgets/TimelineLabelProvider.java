package de.fu_berlin.imp.seqan.usability_analyzer.timeline.ui.widgets;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.nebula.utils.ExecutorUtil;
import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineLabelProvider;
import com.bkahlert.devel.nebula.widgets.timeline.IBaseTimeline;
import com.bkahlert.devel.nebula.widgets.timeline.impl.Decorator;
import com.bkahlert.devel.nebula.widgets.timeline.impl.HotZone;
import com.bkahlert.devel.nebula.widgets.timeline.impl.ZoomStep;
import com.bkahlert.devel.nebula.widgets.timeline.model.IDecorator;
import com.bkahlert.devel.nebula.widgets.timeline.model.IHotZone;
import com.bkahlert.devel.nebula.widgets.timeline.model.IZoomStep;
import com.bkahlert.devel.nebula.widgets.timeline.model.Unit;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IDataSetInfo;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IDataService;

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

	public IZoomStep[] getZoomSteps(TIMELINE timeline) {
		List<IZoomStep> zoomSteps = new ArrayList<IZoomStep>();
		zoomSteps.add(new ZoomStep(0.675f, Unit.MILLISECOND, 100));
		zoomSteps.add(new ZoomStep(0.45f, Unit.MILLISECOND, 100));
		zoomSteps.add(new ZoomStep(300, Unit.SECOND, 1));
		zoomSteps.add(new ZoomStep(200, Unit.SECOND, 1));
		zoomSteps.add(new ZoomStep(135, Unit.SECOND, 1));
		zoomSteps.add(new ZoomStep(90, Unit.SECOND, 1));
		zoomSteps.add(new ZoomStep(60, Unit.SECOND, 1));
		zoomSteps.add(new ZoomStep(40, Unit.SECOND, 1));
		zoomSteps.add(new ZoomStep(27, Unit.SECOND, 1));
		zoomSteps.add(new ZoomStep(18, Unit.SECOND, 1));
		zoomSteps.add(new ZoomStep(12 * 60, Unit.MINUTE, 1));
		zoomSteps.add(new ZoomStep(480, Unit.MINUTE, 1));
		zoomSteps.add(new ZoomStep(320, Unit.MINUTE, 1));
		zoomSteps.add(new ZoomStep(213, Unit.MINUTE, 1));
		zoomSteps.add(new ZoomStep(142, Unit.MINUTE, 1));
		zoomSteps.add(new ZoomStep(63, Unit.MINUTE, 1));
		zoomSteps.add(new ZoomStep(42, Unit.MINUTE, 1));
		zoomSteps.add(new ZoomStep(28, Unit.MINUTE, 1));
		zoomSteps.add(new ZoomStep(19, Unit.MINUTE, 2));
		zoomSteps.add(new ZoomStep(1124, Unit.HOUR, 1));
		zoomSteps.add(new ZoomStep(749, Unit.HOUR, 1));
		zoomSteps.add(new ZoomStep(499, Unit.HOUR, 1));
		zoomSteps.add(new ZoomStep(333, Unit.HOUR, 1));
		zoomSteps.add(new ZoomStep(222, Unit.HOUR, 1));
		zoomSteps.add(new ZoomStep(148, Unit.HOUR, 1));
		zoomSteps.add(new ZoomStep(99, Unit.HOUR, 1));
		zoomSteps.add(new ZoomStep(66, Unit.HOUR, 1));
		zoomSteps.add(new ZoomStep(43, Unit.HOUR, 1));
		zoomSteps.add(new ZoomStep(29, Unit.HOUR, 1));
		zoomSteps.add(new ZoomStep(702, Unit.DAY, 1));
		zoomSteps.add(new ZoomStep(467, Unit.DAY, 1));
		zoomSteps.add(new ZoomStep(312, Unit.DAY, 1));
		zoomSteps.add(new ZoomStep(208, Unit.DAY, 1));
		zoomSteps.add(new ZoomStep(139, Unit.DAY, 1));
		zoomSteps.add(new ZoomStep(92, Unit.DAY, 1));
		zoomSteps.add(new ZoomStep(62, Unit.DAY, 1));
		zoomSteps.add(new ZoomStep(431, Unit.WEEK, 1));
		zoomSteps.add(new ZoomStep(287, Unit.WEEK, 1));
		zoomSteps.add(new ZoomStep(192, Unit.WEEK, 1));
		zoomSteps.add(new ZoomStep(128, Unit.WEEK, 1));
		zoomSteps.add(new ZoomStep(85, Unit.WEEK, 1));
		zoomSteps.add(new ZoomStep(57, Unit.WEEK, 1));
		zoomSteps.add(new ZoomStep(227, Unit.MONTH, 1));
		zoomSteps.add(new ZoomStep(151, Unit.MONTH, 1));
		zoomSteps.add(new ZoomStep(101, Unit.MONTH, 1));
		zoomSteps.add(new ZoomStep(67, Unit.MONTH, 1));
		zoomSteps.add(new ZoomStep(807, Unit.YEAR, 1));
		zoomSteps.add(new ZoomStep(538, Unit.YEAR, 1));
		zoomSteps.add(new ZoomStep(359, Unit.YEAR, 1));
		zoomSteps.add(new ZoomStep(239, Unit.YEAR, 1));
		zoomSteps.add(new ZoomStep(159, Unit.YEAR, 1));
		zoomSteps.add(new ZoomStep(106, Unit.YEAR, 1));
		zoomSteps.add(new ZoomStep(71, Unit.YEAR, 1));
		zoomSteps.add(new ZoomStep(473, Unit.DECADE, 1));
		zoomSteps.add(new ZoomStep(315, Unit.DECADE, 1));
		zoomSteps.add(new ZoomStep(210, Unit.DECADE, 1));
		zoomSteps.add(new ZoomStep(140, Unit.DECADE, 1));
		zoomSteps.add(new ZoomStep(93, Unit.DECADE, 1));
		zoomSteps.add(new ZoomStep(62, Unit.DECADE, 1));
		return zoomSteps.toArray(new IZoomStep[0]);
	};

	@Override
	public Integer getZoomIndex() {
		return 26;
	}

	@Override
	public Float getTimeZone(TIMELINE timeline) {
		IDataSetInfo dataSetInfo = getDataSetInfo();
		TimeZoneDateRange range = dataSetInfo.getDateRange();
		if (range.getStartDate() != null) {
			float offset = (range.getStartDate().getLocalTime() - range
					.getStartDate().getTime()) / 3600000f; // e.g. 2 or -5.5
			return offset;
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
