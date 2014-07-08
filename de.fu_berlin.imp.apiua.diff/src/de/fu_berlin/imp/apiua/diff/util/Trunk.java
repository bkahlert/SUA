package de.fu_berlin.imp.apiua.diff.util;

import de.fu_berlin.imp.apiua.core.model.data.IData;
import de.fu_berlin.imp.apiua.core.model.data.IDataContainer;

public class Trunk implements ITrunk {
	private IDataContainer sourcesDirectory;

	public Trunk(IDataContainer dataContainer) {
		super();
		this.sourcesDirectory = dataContainer;
	}

	/* (non-Javadoc)
	 * @see de.fu_berlin.imp.apiua.diff.util.ITrunk#getOriginSourceFile(java.lang.String)
	 */
	@Override
	public IData getSourceFile(String filename) {
		return sourcesDirectory.getResource(filename);
	}
}
