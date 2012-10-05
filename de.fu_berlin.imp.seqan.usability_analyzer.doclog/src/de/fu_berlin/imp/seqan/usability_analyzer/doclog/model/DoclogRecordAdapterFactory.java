package de.fu_berlin.imp.seqan.usability_analyzer.doclog.model;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IAdapterFactory;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.FingerprintDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.IRevealableInOS;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.IdDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;

public class DoclogRecordAdapterFactory implements IAdapterFactory {

	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] { TimeZoneDateRange.class, IdDateRange.class,
				FingerprintDateRange.class, IRevealableInOS.class };
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof DoclogRecord) {
			final DoclogRecord doclogRecord = (DoclogRecord) adaptableObject;
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
			if (adapterType == IRevealableInOS.class) {
				return new IRevealableInOS() {
					@Override
					public File getFile() throws IOException {
						return doclogRecord.getDoclog().getStaticFile();
					}

					@Override
					public String toString() {
						return doclogRecord.toString();
					}
				};
			}
			return null;
		}
		return null;
	}

}
