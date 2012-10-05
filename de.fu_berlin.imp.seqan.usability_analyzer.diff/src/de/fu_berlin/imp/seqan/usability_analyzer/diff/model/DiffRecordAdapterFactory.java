package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IAdapterFactory;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.IRevealableInOS;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.IdDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;

public class DiffRecordAdapterFactory implements IAdapterFactory {

	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] { TimeZoneDateRange.class, IdDateRange.class,
				IRevealableInOS.class };
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof DiffRecord) {
			final DiffRecord diffRecord = (DiffRecord) adaptableObject;
			if (adapterType == TimeZoneDateRange.class) {
				// although a DiffRecord itself has a DateRange
				// it is preferable to use its Diff's DateRange to have a
				// slighter wider range
				return diffRecord.getDiffFile().getDateRange();
			}
			if (adapterType == IdDateRange.class) {
				TimeZoneDateRange dateRange = diffRecord.getDiffFile()
						.getDateRange();
				return new IdDateRange(diffRecord.getDiffFile().getID(),
						dateRange.getStartDate(), dateRange.getEndDate());
			}
			if (adapterType == IRevealableInOS.class) {
				return new IRevealableInOS() {
					@Override
					public File getFile() throws IOException {
						return diffRecord.getSourceFile();
					}

					@Override
					public String toString() {
						return diffRecord.toString();
					}
				};
			}
			return null;
		}
		return null;
	}

}
