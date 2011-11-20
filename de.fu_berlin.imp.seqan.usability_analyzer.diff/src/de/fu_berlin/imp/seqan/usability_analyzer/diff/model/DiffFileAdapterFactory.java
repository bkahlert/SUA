package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import org.eclipse.core.runtime.IAdapterFactory;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.LocalDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.IdDateRange;

public class DiffFileAdapterFactory implements IAdapterFactory {

	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] { LocalDateRange.class, IdDateRange.class };
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof DiffFile) {
			DiffFile diffFile = (DiffFile) adaptableObject;
			if (adapterType == LocalDateRange.class) {
				return diffFile.getDateRange();
			}
			if (adapterType == IdDateRange.class) {
				LocalDateRange dateRange = diffFile.getDateRange();
				return new IdDateRange(diffFile.getId(),
						dateRange.getStartDate(), dateRange.getEndDate());
			}
			return null;
		}
		return null;
	}

}
