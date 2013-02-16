package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model;

import org.eclipse.core.runtime.IAdapterFactory;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.FingerprintDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.IdDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;

public class EpisodeAdapterFactory implements IAdapterFactory {

	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] { TimeZoneDateRange.class, IdDateRange.class,
				FingerprintDateRange.class };
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof IEpisode) {
			IEpisode episode = (IEpisode) adaptableObject;
			if (adapterType == TimeZoneDateRange.class) {
				return episode.getDateRange();
			}
			if (adapterType == IdDateRange.class) {
				TimeZoneDateRange dateRange = episode.getDateRange();
				return episode.getId() != null ? new IdDateRange(episode.getId(),
						dateRange.getStartDate(), dateRange.getEndDate())
						: null;
			}
			if (adapterType == FingerprintDateRange.class) {
				TimeZoneDateRange dateRange = episode.getDateRange();
				return episode.getFingerprint() != null ? new FingerprintDateRange(
						episode.getFingerprint(), dateRange.getStartDate(),
						dateRange.getEndDate()) : null;
			}
			return null;
		}
		return null;
	}
}
