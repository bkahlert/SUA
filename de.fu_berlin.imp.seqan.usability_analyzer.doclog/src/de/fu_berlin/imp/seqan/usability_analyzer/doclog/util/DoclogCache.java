package de.fu_berlin.imp.seqan.usability_analyzer.doclog.util;

import org.eclipse.core.runtime.IProgressMonitor;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.Cache;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.Doclog;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogDataContainer;

public class DoclogCache extends Cache<IIdentifier, Doclog> {

	public DoclogCache(final DoclogDataContainer doclogFileDirectory,
			int cacheSize) {
		super(new CacheFetcher<IIdentifier, Doclog>() {
			@Override
			public Doclog fetch(IIdentifier identifier,
					IProgressMonitor progressMonitor) {
				return doclogFileDirectory.readDoclogFromSource(identifier,
						progressMonitor);
			}
		}, cacheSize);
	}

}
