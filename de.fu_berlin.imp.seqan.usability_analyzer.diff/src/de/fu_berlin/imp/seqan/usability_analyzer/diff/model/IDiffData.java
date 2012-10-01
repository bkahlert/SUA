package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import java.util.List;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.HasID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.IData;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;

public interface IDiffData extends IData, HasDateRange, ICodeable, HasID {

	public IDiffData getPrevDiffFile();

	public long getRevision();

	public DiffFileRecordList getDiffFileRecords();

	public boolean sourcesExist();

	public List<String> getContent(long contentStart, long contentEnd);

}