package de.fu_berlin.imp.seqan.usability_analyzer.doclog.model;

import java.util.ArrayList;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.IRangeable;

public class DoclogFileList extends ArrayList<DoclogFile> implements IRangeable {

	private static final long serialVersionUID = -7428136109215033397L;

	@Override
	public boolean isInRange(DateRange dateRange) {
		for (DoclogFile doclogFile : this) {
			if (doclogFile.isInRange(dateRange))
				return true;
		}
		return false;
	}

}
