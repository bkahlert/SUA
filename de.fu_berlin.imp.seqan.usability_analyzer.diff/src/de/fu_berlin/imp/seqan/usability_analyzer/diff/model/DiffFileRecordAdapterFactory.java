package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import org.eclipse.core.runtime.IAdapterFactory;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.IdDateRange;

public class DiffFileRecordAdapterFactory implements IAdapterFactory {

	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] { TimeZoneDateRange.class, IdDateRange.class };
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof DiffFileRecord) {
			DiffFileRecord diffFileRecord = (DiffFileRecord) adaptableObject;
			if (adapterType == TimeZoneDateRange.class) {
				// although a DiffFileRecord itself has a DateRange
				// it is preferable to use its DiffFile's DateRange to have a
				// slighter wider range
				return diffFileRecord.getDiffFile().getDateRange();
			}
			if (adapterType == IdDateRange.class) {
				TimeZoneDateRange dateRange = diffFileRecord.getDiffFile()
						.getDateRange();
				return new IdDateRange(diffFileRecord.getDiffFile().getID(),
						dateRange.getStartDate(), dateRange.getEndDate());
			}
			return null;
		}
		return null;
	}

}
