package de.fu_berlin.imp.seqan.usability_analyzer.diff.model.impl;

import org.eclipse.core.runtime.IAdapterFactory;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.IdentifierDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiff;

public class DiffAdapterFactory implements IAdapterFactory {

	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] { TimeZoneDateRange.class, IdentifierDateRange.class };
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof IDiff) {
			Diff diff = (Diff) adaptableObject;
			if (adapterType == TimeZoneDateRange.class) {
				return diff.getDateRange();
			}
			if (adapterType == IdentifierDateRange.class) {
				TimeZoneDateRange dateRange = diff.getDateRange();
				return new IdentifierDateRange(diff.getIdentifier(), dateRange.getStartDate(),
						dateRange.getEndDate());
			}
			return null;
		}
		return null;
	}

}
