package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.HasID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;

public interface IDiffRecordSegment extends ICodeable, HasDateRange, HasID,
		ICompilable {

	public IDiffRecord getDiffFileRecord();

	public long getSegmentStart();

	public long getSegmentLength();

}