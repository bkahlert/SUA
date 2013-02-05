package de.fu_berlin.imp.seqan.usability_analyzer.diff.timeline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import com.bkahlert.devel.nebula.widgets.timeline.IOptions;
import com.bkahlert.devel.nebula.widgets.timeline.ITimelineBand;
import com.bkahlert.devel.nebula.widgets.timeline.ITimelineEvent;
import com.bkahlert.devel.nebula.widgets.timeline.impl.Options;
import com.bkahlert.devel.nebula.widgets.timeline.impl.TimelineBand;
import com.bkahlert.devel.nebula.widgets.timeline.impl.TimelineEventImageBased;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.Activator;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.Diff;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffList;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.ui.DiffLabelProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.timeline.extensionProviders.ITimelineBandProvider;

public class DiffTimelineBandProvider implements ITimelineBandProvider {

	@Override
	public boolean isValid(Object key) {
		if (key instanceof ID) {
			return Activator.getDefault().getDiffDataContainer().getIDs()
					.contains(key);
		}
		return false;
	}

	@Override
	public List<ITimelineBand> getTimelineBands(Object key,
			IProgressMonitor monitor) {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 3);
		final DiffList diffList = (key instanceof ID) ? de.fu_berlin.imp.seqan.usability_analyzer.diff.Activator
				.getDefault().getDiffDataContainer()
				.getDiffFiles((ID) key, subMonitor.newChild(1))
				: null;

		if (diffList == null)
			return null;

		List<ITimelineBand> timelineBands = new ArrayList<ITimelineBand>();

		ITimelineBand diffBand = createDiffTimelineBand(diffList);
		timelineBands.add(diffBand);
		monitor.worked(1);

		ITimelineBand diffRecordBand = createDiffRecordTimelineBand(diffList);
		timelineBands.add(diffRecordBand);
		monitor.worked(1);

		return timelineBands;
	}

	private ITimelineBand createDiffTimelineBand(final DiffList diffList) {
		IOptions diffBandOptions = new Options();
		diffBandOptions.setTitle("Development");
		diffBandOptions.setShowInOverviewBands(true);
		diffBandOptions.setRatio(0.15f);
		List<ITimelineEvent> diffEvents = new ArrayList<ITimelineEvent>();
		for (Diff diff : diffList) {
			ITimelineEvent diffEvent = createDiffEvent(diff);
			diffEvents.add(diffEvent);
		}
		ITimelineBand diffBand = new TimelineBand(diffBandOptions, diffEvents);
		return diffBand;
	}

	private ITimelineBand createDiffRecordTimelineBand(final DiffList diffList) {
		IOptions diffRecordBandOptions = new Options();
		diffRecordBandOptions.setTitle("Sources");
		diffRecordBandOptions.setShowInOverviewBands(true);
		List<ITimelineEvent> diffRecordEvents = new ArrayList<ITimelineEvent>();
		for (Diff diff : diffList) {
			for (DiffRecord diffRecord : diff.getDiffFileRecords()) {
				ITimelineEvent diffRecordEvent = createDiffRecordEvent(diffRecord);
				diffRecordEvents.add(diffRecordEvent);
			}
		}
		ITimelineBand diffRecordBand = new TimelineBand(diffRecordBandOptions,
				diffRecordEvents);
		return diffRecordBand;
	}

	private ITimelineEvent createDiffEvent(Diff diff) {
		DiffLabelProvider diffLabelProvider = new DiffLabelProvider();

		StringBuffer title = new StringBuffer();
		title.append("Compilation #" + diff.getRevision());

		TimeZoneDateRange dateRange = diff.getDateRange();
		Calendar startDate = dateRange.getStartDate() != null ? dateRange
				.getStartDate().getCalendar() : null;
		Calendar endDate = dateRange.getEndDate() != null ? dateRange
				.getEndDate().getCalendar() : null;

		List<String> classNames = Arrays.asList(Diff.class.getSimpleName());

		return new TimelineEventImageBased(title.toString(),
				diffLabelProvider.getImage(diff), null, startDate, endDate,
				classNames, diff);
	}

	private ITimelineEvent createDiffRecordEvent(DiffRecord diffRecord) {
		DiffLabelProvider diffLabelProvider = new DiffLabelProvider();

		StringBuffer title = new StringBuffer();
		title.append(diffRecord.getFilename());

		TimeZoneDateRange dateRange = diffRecord.getDateRange();
		Calendar startDate = dateRange.getStartDate() != null ? dateRange
				.getStartDate().getCalendar() : null;
		Calendar endDate = dateRange.getEndDate() != null ? dateRange
				.getEndDate().getCalendar() : null;

		List<String> classNames = Arrays.asList(DiffRecord.class
				.getSimpleName());

		ITimelineEvent event = new TimelineEventImageBased(title.toString(),
				diffLabelProvider.getImage(diffRecord), null, startDate,
				endDate, classNames, diffRecord);
		return event;
	}
}
