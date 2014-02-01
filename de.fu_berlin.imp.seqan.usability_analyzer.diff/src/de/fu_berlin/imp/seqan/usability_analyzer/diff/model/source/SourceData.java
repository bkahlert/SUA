package de.fu_berlin.imp.seqan.usability_analyzer.diff.model.source;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IData;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.impl.WrappingData;

public class SourceData extends WrappingData implements ISourceData {

	public SourceData(IData wrappedData) {
		super(wrappedData);
	}

}
