package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import org.eclipse.core.runtime.IAdapterFactory;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.IdDateRange;

public class DiffFileAdapterFactory implements IAdapterFactory {

	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] { TimeZoneDateRange.class, IdDateRange.class };
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof DiffDataResource) {
			DiffDataResource diffDataResource = (DiffDataResource) adaptableObject;
			if (adapterType == TimeZoneDateRange.class) {
				return diffDataResource.getDateRange();
			}
			if (adapterType == IdDateRange.class) {
				TimeZoneDateRange dateRange = diffDataResource.getDateRange();
				return new IdDateRange(diffDataResource.getID(),
						dateRange.getStartDate(), dateRange.getEndDate());
			}
			return null;
		}
		return null;
	}

}
