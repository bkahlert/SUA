package de.fu_berlin.imp.seqan.usability_analyzer.diff.util;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.IData;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.IDataContainer;

public class SourceOrigin {
	private IDataContainer sourcesDirectory;

	public SourceOrigin(IDataContainer dataContainer) {
		super();
		this.sourcesDirectory = dataContainer;
	}

	public IData getOriginSourceFile(String filename) {
		return sourcesDirectory.getResource(filename);
	}
}
