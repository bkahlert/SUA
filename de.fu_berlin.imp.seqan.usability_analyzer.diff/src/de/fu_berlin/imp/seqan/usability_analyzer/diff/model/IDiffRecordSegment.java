package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.HasIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;

public interface IDiffRecordSegment extends ILocatable, HasDateRange, HasIdentifier,
		ICompilable {

	public IDiffRecord getDiffFileRecord();

	public long getSegmentStart();

	public long getSegmentLength();

}