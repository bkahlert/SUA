package de.fu_berlin.imp.seqan.usability_analyzer.doclog.model;

import org.eclipse.core.runtime.IAdapterFactory;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.FingerprintDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.IdDateRange;

public class DoclogRecordAdapterFactory implements IAdapterFactory {

	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] { TimeZoneDateRange.class, IdDateRange.class,
				FingerprintDateRange.class };
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof DoclogRecord) {
			DoclogRecord doclogRecord = (DoclogRecord) adaptableObject;
			if (adapterType == TimeZoneDateRange.class) {
				return doclogRecord.getDateRange();
			}
			if (adapterType == IdDateRange.class) {
				ID id = doclogRecord.getDoclog().getID();
				if (id == null)
					return null;

				TimeZoneDateRange dateRange = doclogRecord.getDateRange();
				return new IdDateRange(id, dateRange.getStartDate(),
						dateRange.getEndDate());
			}
			if (adapterType == FingerprintDateRange.class) {
				Fingerprint fingerprint = doclogRecord.getDoclog()
						.getFingerprint();
				if (fingerprint == null)
					return null;

				TimeZoneDateRange dateRange = doclogRecord.getDateRange();
				return new FingerprintDateRange(fingerprint,
						dateRange.getStartDate(), dateRange.getEndDate());
			}
			return null;
		}
		return null;
	}

}
