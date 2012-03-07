package de.fu_berlin.imp.seqan.usability_analyzer.doclog.util;

import org.eclipse.core.runtime.IProgressMonitor;

import de.fu_berlin.imp.seqan.usability_analyzer.core.util.Cache;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogDirectory;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogFile;

public class DoclogCache extends Cache<Object, DoclogFile> {

	public DoclogCache(final DoclogDirectory doclogFileDirectory, int cacheSize) {
		super(new CacheFetcher<Object, DoclogFile>() {
			@Override
			public DoclogFile fetch(Object key, IProgressMonitor progressMonitor) {
				return doclogFileDirectory.createDoclogFile(key,
						progressMonitor);
			}
		}, cacheSize);
	}

}
