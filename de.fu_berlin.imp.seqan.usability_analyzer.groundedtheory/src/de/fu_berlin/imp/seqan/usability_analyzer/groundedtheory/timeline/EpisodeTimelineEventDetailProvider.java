package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.timeline;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.devel.nebula.widgets.SimpleIllustratedComposite.IllustratedText;
import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;

import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.ImageManager;
import de.fu_berlin.imp.seqan.usability_analyzer.timeline.extensionProviders.DefaultTimelineEventDetailProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.timeline.extensionProviders.ITimelineEventDetailProvider;

public class EpisodeTimelineEventDetailProvider extends
		DefaultTimelineEventDetailProvider<IEpisode> implements
		ITimelineEventDetailProvider<IEpisode> {

	@Override
	public Class<IEpisode> getType() {
		return IEpisode.class;
	}

	@Override
	public List<IllustratedText> getMetaInformation(IEpisode diff) {
		List<IllustratedText> metaEntries = new ArrayList<IllustratedText>();
		metaEntries.add(new IllustratedText(ImageManager.EPISODE,
				IEpisode.class.getSimpleName()));

		return metaEntries;
	}

	@Override
	public List<Entry<String, String>> getDetailInformation(IEpisode episode) {
		List<Entry<String, String>> detailEntries = new ArrayList<Entry<String, String>>();
		detailEntries.add(new DetailEntry("Owner",
				episode.getIdentifier() != null ? episode.getIdentifier()
						.getIdentifier() : ""));
		detailEntries.add(new DetailEntry("Caption",
				episode.getCaption() != null ? episode.getCaption() : "-"));
		detailEntries.add(new DetailEntry("Creation",
				(episode.getCreation() != null) ? episode.getCreation()
						.toISO8601() : "-"));

		detailEntries.add(new DetailEntry("Start",
				(episode.getDateRange() != null && episode.getDateRange()
						.getStartDate() != null) ? episode.getDateRange()
						.getStartDate().toISO8601() : "-"));

		detailEntries.add(new DetailEntry("End",
				(episode.getDateRange() != null && episode.getDateRange()
						.getEndDate() != null) ? episode.getDateRange()
						.getEndDate().toISO8601() : "-"));

		Long milliSecondsPassed = episode.getDateRange() != null ? episode
				.getDateRange().getDifference() : null;
		detailEntries.add(new DetailEntry("Span",
				(milliSecondsPassed != null) ? DurationFormatUtils
						.formatDuration(milliSecondsPassed,
								new SUACorePreferenceUtil()
										.getTimeDifferenceFormat(), true)
						: "unknown"));
		return detailEntries;
	}

	@Override
	public void fillCustomComposite(Composite parent, IEpisode episode,
			ITimeline timeline) {

	}

}
