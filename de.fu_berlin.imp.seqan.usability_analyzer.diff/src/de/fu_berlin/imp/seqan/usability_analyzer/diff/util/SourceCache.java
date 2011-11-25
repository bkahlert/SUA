package de.fu_berlin.imp.seqan.usability_analyzer.diff.util;

import java.io.File;

import org.eclipse.core.runtime.Assert;

import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFile;

public class SourceCache {
	private File sourceCacheDirectory;

	public SourceCache(File sourceCacheDirectory) {
		Assert.isNotNull(sourceCacheDirectory);
		this.sourceCacheDirectory = sourceCacheDirectory;
	}

	public File getSourceCacheDirectory() {
		return sourceCacheDirectory;
	}

	public File getCachedSourceFile(DiffFile diffFile, String filename) {
		long revision = Long.parseLong(diffFile.getRevision());

		String path = this.sourceCacheDirectory.getAbsolutePath() + "/"
				+ diffFile.getId() + "/" + revision + "/" + filename;
		return new File(path.replace("//", "/"));
	}
}
