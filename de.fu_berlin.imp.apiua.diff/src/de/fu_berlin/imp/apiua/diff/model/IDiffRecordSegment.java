package de.fu_berlin.imp.apiua.diff.model;

import de.fu_berlin.imp.apiua.core.model.HasIdentifier;
import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.ui.viewer.filters.HasDateRange;

public interface IDiffRecordSegment extends ILocatable, HasDateRange, HasIdentifier,
		ICompilable {

	public IDiffRecord getDiffFileRecord();

	public long getSegmentStart();

	public long getSegmentLength();

}