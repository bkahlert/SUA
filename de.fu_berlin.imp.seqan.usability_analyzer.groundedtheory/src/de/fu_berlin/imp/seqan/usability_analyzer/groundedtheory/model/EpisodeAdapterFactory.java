package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model;

import org.eclipse.core.runtime.IAdapterFactory;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.IdentifierDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;

public class EpisodeAdapterFactory implements IAdapterFactory {

	@Override
	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] { TimeZoneDateRange.class, IdentifierDateRange.class };
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof IEpisode) {
			IEpisode episode = (IEpisode) adaptableObject;
			if (adapterType == TimeZoneDateRange.class) {
				return episode.getDateRange();
			}
			if (adapterType == IdentifierDateRange.class) {
				TimeZoneDateRange dateRange = episode.getDateRange();
				return episode.getIdentifier() != null ? new IdentifierDateRange(
						episode.getIdentifier(), dateRange.getStartDate(),
						dateRange.getEndDate()) : null;
			}
			return null;
		}
		return null;
	}
}
