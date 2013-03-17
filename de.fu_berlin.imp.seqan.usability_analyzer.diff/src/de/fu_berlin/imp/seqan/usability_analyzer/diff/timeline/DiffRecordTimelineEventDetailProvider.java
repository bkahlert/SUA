package de.fu_berlin.imp.seqan.usability_analyzer.diff.timeline;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.devel.nebula.widgets.SimpleIllustratedComposite.IllustratedText;
import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;

import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IInformationPresenterService;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.impl.DiffRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.ui.ImageManager;
import de.fu_berlin.imp.seqan.usability_analyzer.timeline.extensionProviders.DefaultTimelineEventDetailProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.timeline.extensionProviders.ITimelineEventDetailProvider;

public class DiffRecordTimelineEventDetailProvider extends
		DefaultTimelineEventDetailProvider<IDiffRecord> implements
		ITimelineEventDetailProvider<IDiffRecord> {

	@Override
	public Class<IDiffRecord> getType() {
		return IDiffRecord.class;
	}

	@Override
	public List<IllustratedText> getMetaInformation(IDiffRecord diffRecord) {
		List<IllustratedText> metaEntries = new ArrayList<IllustratedText>();
		metaEntries.add(new IllustratedText(ImageManager.DIFFRECORD,
				DiffRecord.class.getSimpleName()));

		return metaEntries;
	}

	@Override
	public List<Entry<String, String>> getDetailInformation(
			IDiffRecord diffRecord) {
		List<Entry<String, String>> detailEntries = new ArrayList<Entry<String, String>>();

		detailEntries.add(new IInformationPresenterService.DetailEntry("Filename",
				diffRecord.getFilename() != null ? diffRecord.getFilename()
						: "-"));
		detailEntries.add(new IInformationPresenterService.DetailEntry("Source",
				diffRecord.getSource() != null ? diffRecord.getSource() : "-"));
		detailEntries.add(new IInformationPresenterService.DetailEntry("Is Temporary", diffRecord
				.isTemporary() ? "Yes" : "No"));
		detailEntries.add(new IInformationPresenterService.DetailEntry("Source Exists", diffRecord
				.sourceExists() ? "Yes" : "No"));

		detailEntries.add(new IInformationPresenterService.DetailEntry("Date",
				(diffRecord.getDateRange() != null && diffRecord.getDateRange()
						.getStartDate() != null) ? diffRecord.getDateRange()
						.getStartDate().toISO8601() : "-"));

		Long milliSecondsPassed = diffRecord.getDateRange() != null ? diffRecord
				.getDateRange().getDifference() : null;
		detailEntries.add(new IInformationPresenterService.DetailEntry("Time Passed",
				(milliSecondsPassed != null) ? DurationFormatUtils
						.formatDuration(milliSecondsPassed,
								new SUACorePreferenceUtil()
										.getTimeDifferenceFormat(), true)
						: "unknown"));
		return detailEntries;
	}

	@Override
	public void fillCustomComposite(Composite parent, IDiffRecord diff,
			ITimeline timeline) {

	}

}
