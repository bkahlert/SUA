package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import java.util.ArrayList;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.IRangeable;

public class DiffFileList extends ArrayList<DiffFile> implements IRangeable {

	private static final long serialVersionUID = 1327362495545624012L;

	@Override
	public boolean isInRange(DateRange dateRange) {
		for (DiffFile diffFile : this) {
			if (diffFile.isInRange(dateRange))
				return true;
		}
		return false;
	}
}
