package de.fu_berlin.imp.seqan.usability_analyzer.doclog.model;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IAdapterFactory;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.IRevealableInOS;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;

public class DoclogRecordAdapterFactory implements IAdapterFactory {

	@Override
	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] { TimeZoneDateRange.class, IRevealableInOS.class };
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof DoclogRecord) {
			final DoclogRecord doclogRecord = (DoclogRecord) adaptableObject;
			if (adapterType == TimeZoneDateRange.class) {
				return doclogRecord.getDateRange();
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
