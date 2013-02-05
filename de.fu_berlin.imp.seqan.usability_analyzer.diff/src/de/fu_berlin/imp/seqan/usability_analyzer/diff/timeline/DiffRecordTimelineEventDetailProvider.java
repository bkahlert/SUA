package de.fu_berlin.imp.seqan.usability_analyzer.diff.timeline;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.devel.nebula.widgets.SimpleIllustratedComposite.IllustratedText;
import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;

import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.ui.ImageManager;
import de.fu_berlin.imp.seqan.usability_analyzer.timeline.extensionProviders.DefaultTimelineEventDetailProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.timeline.extensionProviders.ITimelineEventDetailProvider;

public class DiffRecordTimelineEventDetailProvider extends
		DefaultTimelineEventDetailProvider<DiffRecord> implements
		ITimelineEventDetailProvider<DiffRecord> {

	@Override
	public Class<DiffRecord> getType() {
		return DiffRecord.class;
	}

	@Override
	public List<IllustratedText> getMetaInformation(DiffRecord diffRecord) {
		List<IllustratedText> metaEntries = new ArrayList<IllustratedText>();
		metaEntries.add(new IllustratedText(ImageManager.DIFFFILERECORD,
				DiffRecord.class.getSimpleName()));

		return metaEntries;
	}

	@Override
	public List<Entry<String, String>> getDetailInformation(
			DiffRecord diffRecord) {
		List<Entry<String, String>> detailEntries = new ArrayList<Entry<String, String>>();

		detailEntries.add(new DetailEntry("Filename",
				diffRecord.getFilename() != null ? diffRecord.getFilename()
						: "-"));
		detailEntries.add(new DetailEntry("Source",
				diffRecord.getSource() != null ? diffRecord.getSource() : "-"));
		detailEntries.add(new DetailEntry("Is Temporary", diffRecord
				.isTemporary() ? "Yes" : "No"));
		detailEntries.add(new DetailEntry("Source Exists", diffRecord
				.sourceExists() ? "Yes" : "No"));

		detailEntries.add(new DetailEntry("Date",
				(diffRecord.getDateRange() != null && diffRecord.getDateRange()
						.getStartDate() != null) ? diffRecord.getDateRange()
						.getStartDate().toISO8601() : "-"));

		Long milliSecondsPassed = diffRecord.getDateRange() != null ? diffRecord
				.getDateRange().getDifference() : null;
		detailEntries.add(new DetailEntry("Time Passed",
				(milliSecondsPassed != null) ? DurationFormatUtils
						.formatDuration(milliSecondsPassed,
								new SUACorePreferenceUtil()
										.getTimeDifferenceFormat(), true)
						: "unknown"));
		return detailEntries;
	}

	@Override
	public void fillCustomComposite(Composite parent, DiffRecord diff,
			ITimeline timeline) {

	}

}
