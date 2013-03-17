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
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiff;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.ui.ImageManager;
import de.fu_berlin.imp.seqan.usability_analyzer.timeline.extensionProviders.DefaultTimelineEventDetailProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.timeline.extensionProviders.ITimelineEventDetailProvider;

public class DiffTimelineEventDetailProvider extends
		DefaultTimelineEventDetailProvider<IDiff> implements
		ITimelineEventDetailProvider<IDiff> {

	@Override
	public Class<IDiff> getType() {
		return IDiff.class;
	}

	@Override
	public List<IllustratedText> getMetaInformation(IDiff diff) {
		List<IllustratedText> metaEntries = new ArrayList<IllustratedText>();
		metaEntries.add(new IllustratedText(ImageManager.DIFF, IDiff.class
				.getSimpleName()));

		return metaEntries;
	}

	@Override
	public List<Entry<String, String>> getDetailInformation(IDiff diff) {
		List<Entry<String, String>> detailEntries = new ArrayList<Entry<String, String>>();
		detailEntries.add(new IInformationPresenterService.DetailEntry("Name", diff.getName() != null ? diff
				.getName() : "-"));
		detailEntries.add(new IInformationPresenterService.DetailEntry("Revision", diff.getRevision() + ""));
		detailEntries.add(new IInformationPresenterService.DetailEntry("File Size", diff.getLength()
				+ " Bytes"));

		detailEntries.add(new IInformationPresenterService.DetailEntry("Date",
				(diff.getDateRange() != null && diff.getDateRange()
						.getStartDate() != null) ? diff.getDateRange()
						.getStartDate().toISO8601() : "-"));

		Long milliSecondsPassed = diff.getDateRange() != null ? diff
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
	public void fillCustomComposite(Composite parent, IDiff diff,
			ITimeline timeline) {

	}

}
