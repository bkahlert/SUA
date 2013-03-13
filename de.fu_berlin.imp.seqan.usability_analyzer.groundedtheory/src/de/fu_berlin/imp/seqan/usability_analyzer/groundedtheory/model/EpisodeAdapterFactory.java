package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model;

import org.eclipse.core.runtime.IAdapterFactory;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;

public class EpisodeAdapterFactory implements IAdapterFactory {

	@Override
	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] { TimeZoneDateRange.class };
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof IEpisode) {
			IEpisode episode = (IEpisode) adaptableObject;
			if (adapterType == TimeZoneDateRange.class) {
				return episode.getDateRange();
			}
			return null;
		}
		return null;
	}
}
