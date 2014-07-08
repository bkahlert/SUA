package de.fu_berlin.imp.apiua.diff.util;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.Assert;

import de.fu_berlin.imp.apiua.core.model.data.IBaseDataContainer;
import de.fu_berlin.imp.apiua.core.model.identifier.IIdentifier;

public class SourceCache implements ISourceStore {
	private IBaseDataContainer baseDataContainer;
	private String scope;

	public SourceCache(IBaseDataContainer baseDataContainer) {
		Assert.isNotNull(baseDataContainer);
		this.baseDataContainer = baseDataContainer;
		this.scope = "sources";
	}

	public SourceCache(IBaseDataContainer baseDataContainer, String scope) {
		this.baseDataContainer = baseDataContainer;
		this.scope = scope;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.imp.apiua.diff.util.ISourceStore#
	 * getCachedSourceFile
	 * (de.fu_berlin.imp.apiua.core.model.ID, long,
	 * java.lang.String)
	 */
	@Override
	public File getSourceFile(IIdentifier id, String revision, String filename)
			throws IOException {
		try {
			revision = Long.parseLong(revision) + "";
		} catch (NumberFormatException e) {
			// just wanted to try to remove the leading zeros
		}
		return this.baseDataContainer.getStaticFile(this.scope, id + "/"
				+ revision + "/" + filename);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.imp.apiua.diff.util.ISourceStore#
	 * setCachedSourceFile
	 * (de.fu_berlin.imp.apiua.diff.model.Diff,
	 * java.lang.String, java.io.File)
	 */
	@Override
	public void setSourceFile(IIdentifier id, String revision, String filename,
			File file) throws IOException {

		this.baseDataContainer.putFile(this.scope, id + "/" + revision + "/"
				+ filename, file);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fu_berlin.imp.apiua.diff.util.ISourceStore#clear()
	 */
	@Override
	public void clear() {
		this.baseDataContainer.deleteScope("scources");
	}
}
