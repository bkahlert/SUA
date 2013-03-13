package de.fu_berlin.imp.seqan.usability_analyzer.diff.model.impl;

import org.eclipse.core.runtime.IAdapterFactory;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiff;

public class DiffAdapterFactory implements IAdapterFactory {

	@Override
	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] { TimeZoneDateRange.class };
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof IDiff) {
			Diff diff = (Diff) adaptableObject;
			if (adapterType == TimeZoneDateRange.class) {
				return diff.getDateRange();
			}
			return null;
		}
		return null;
	}

}
