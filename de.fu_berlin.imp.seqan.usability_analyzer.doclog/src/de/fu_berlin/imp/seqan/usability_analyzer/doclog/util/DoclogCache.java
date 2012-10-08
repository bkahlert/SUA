package de.fu_berlin.imp.seqan.usability_analyzer.doclog.util;

import org.eclipse.core.runtime.IProgressMonitor;

import de.fu_berlin.imp.seqan.usability_analyzer.core.util.Cache;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.Doclog;

public class DoclogCache extends Cache<Object, Doclog> {

	public DoclogCache(final DoclogDataContainer doclogFileDirectory, int cacheSize) {
		super(new CacheFetcher<Object, Doclog>() {
			@Override
			public Doclog fetch(Object key, IProgressMonitor progressMonitor) {
				return doclogFileDirectory.readDoclogFromSource(key,
						progressMonitor);
			}
		}, cacheSize);
	}

}
