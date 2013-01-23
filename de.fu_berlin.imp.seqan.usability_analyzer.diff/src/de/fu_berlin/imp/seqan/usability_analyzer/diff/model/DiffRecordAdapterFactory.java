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
				return new TimeZoneDateRange(diffRecord.getDiffFile()
						.getDateRange().getStartDate(), diffRecord
						.getDateRange().getEndDate());
			}
			if (adapterType == IdDateRange.class) {
				return new IdDateRange(diffRecord.getDiffFile().getID(),
						diffRecord.getDiffFile().getDateRange().getStartDate(),
						diffRecord.getDateRange().getEndDate());
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
