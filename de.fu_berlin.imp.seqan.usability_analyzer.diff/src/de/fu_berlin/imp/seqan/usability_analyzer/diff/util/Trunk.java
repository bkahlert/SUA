package de.fu_berlin.imp.seqan.usability_analyzer.diff.util;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.IData;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.IDataContainer;

public class Trunk implements ITrunk {
	private IDataContainer sourcesDirectory;

	public Trunk(IDataContainer dataContainer) {
		super();
		this.sourcesDirectory = dataContainer;
	}

	/* (non-Javadoc)
	 * @see de.fu_berlin.imp.seqan.usability_analyzer.diff.util.ITrunk#getOriginSourceFile(java.lang.String)
	 */
	@Override
	public IData getSourceFile(String filename) {
		return sourcesDirectory.getResource(filename);
	}
}
