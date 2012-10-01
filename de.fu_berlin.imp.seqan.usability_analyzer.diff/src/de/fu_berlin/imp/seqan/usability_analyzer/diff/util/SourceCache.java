package de.fu_berlin.imp.seqan.usability_analyzer.diff.util;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.Assert;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.IBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffDataResource;

public class SourceCache {
	private IBaseDataContainer baseDataContainer;
	private String scope;

	public SourceCache(IBaseDataContainer baseDataDirectory) {
		Assert.isNotNull(baseDataDirectory);
		this.baseDataContainer = baseDataDirectory;
		this.scope = "sources";
	}

	public SourceCache(IBaseDataContainer baseDataContainer, String scope) {
		this.baseDataContainer = baseDataContainer;
		this.scope = scope;
	}

	public File getCachedSourceFile(DiffDataResource diffDataResource,
			String filename) throws IOException {
		long revision = Long.parseLong(diffDataResource.getRevision());

		return this.baseDataContainer.getFile(this.scope,
				diffDataResource.getID() + "/" + revision + "/" + filename);
	}

	public void setCachedSourceFile(DiffDataResource diffDataResource,
			String filename, File file) throws IOException {
		long revision = Long.parseLong(diffDataResource.getRevision());

		this.baseDataContainer.putFile(this.scope, diffDataResource.getID()
				+ "/" + revision + "/" + filename, file);
	}

	public void clear() {
		this.baseDataContainer.deleteScope("scources");
	}
}
