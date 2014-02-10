package de.fu_berlin.imp.seqan.usability_analyzer.timeline.ui.widgets;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.nebula.utils.ExecUtils;
import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineLabelProvider;
import com.bkahlert.devel.nebula.widgets.timeline.IBaseTimeline;
import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;
import com.bkahlert.devel.nebula.widgets.timeline.impl.Decorator;
import com.bkahlert.devel.nebula.widgets.timeline.impl.HotZone;
import com.bkahlert.devel.nebula.widgets.timeline.impl.ZoomStep;
import com.bkahlert.devel.nebula.widgets.timeline.model.IDecorator;
import com.bkahlert.devel.nebula.widgets.timeline.model.IHotZone;
import com.bkahlert.devel.nebula.widgets.timeline.model.IZoomStep;
import com.bkahlert.devel.nebula.widgets.timeline.model.Unit;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IDataSetInfo;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IDataService;
import de.fu_berlin.imp.seqan.usability_analyzer.timeline.preferences.SUATimelinePreferenceUtil;

public class TimelineLabelProvider<TIMELINE extends IBaseTimeline> implements
		ITimelineLabelProvider<TIMELINE> {

	private static final Logger LOGGER = Logger
			.getLogger(TimelineLabelProvider.class);

	public TimelineLabelProvider() {
	}

	private static <TIMELINE extends IBaseTimeline> IIdentifier getIdentifier(
			final TIMELINE timeline) {
		IIdentifier identifier = null;
		try {
			identifier = ExecUtils.syncExec(new Callable<IIdentifier>() {
				@Override
				public IIdentifier call() throws Exception {
					Object identifier = timeline.getData();
					return identifier instanceof IIdentifier ? (IIdentifier) identifier
							: null;
				}
			});
		} catch (Exception e) {
			LOGGER.error(
					"Can't determine the " + ITimeline.class.getSimpleName()
							+ "'s " + IIdentifier.class.getSimpleName(), e);
		}
		return identifier;
	}

	private static IDataSetInfo getDataSetInfo() {
		return ((IDataService) PlatformUI.getWorkbench().getService(
				IDataService.class)).getActiveDataDirectories().get(0)
				.getInfo();
	}

	@Override
	public String getTitle(TIMELINE timeline) {
		IIdentifier identifier = getIdentifier(timeline);
		String title = "NULL";
		if (identifier != null) {
			title = identifier.getClass().getSimpleName() + ": "
					+ identifier.getIdentifier();
		}
		return title;
	}

	@Override
	public Calendar getCenterStart(TIMELINE timeline) {
		Calendar centerVisibleDate = null;
		IIdentifier identifier = getIdentifier(timeline);
		if (identifier != null) {
			centerVisibleDate = new SUATimelinePreferenceUtil()
					.getCenterStartDate(identifier);
		}

		if (centerVisibleDate == null) {
			IDataSetInfo dataSetInfo = getDataSetInfo();
			centerVisibleDate = dataSetInfo.getDateRange() != null
					&& dataSetInfo.getDateRange().getStartDate() != null ? dataSetInfo
					.getDateRange().getStartDate().getCalendar()
					: null;
		}

		return centerVisibleDate;
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
	public Integer getZoomIndex(final TIMELINE timeline) {
		int zoomIndex;
		IIdentifier identifier = getIdentifier(timeline);
		if (identifier != null) {
			zoomIndex = new SUATimelinePreferenceUtil()
					.getZoomIndex(identifier);
		} else {
			zoomIndex = this.getZoomSteps(timeline).length / 2;
		}
		return zoomIndex;
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
