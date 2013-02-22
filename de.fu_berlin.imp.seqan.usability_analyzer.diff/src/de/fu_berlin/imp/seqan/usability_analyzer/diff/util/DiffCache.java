package de.fu_berlin.imp.seqan.usability_analyzer.diff.util;

import org.eclipse.core.runtime.IProgressMonitor;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.Cache;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffs;

public class DiffCache extends Cache<ID, IDiffs> {

	public DiffCache(final DiffContainer diffContainer, int cacheSize) {
		super(new CacheFetcher<ID, IDiffs>() {
			@Override
			public IDiffs fetch(ID key, IProgressMonitor progressMonitor) {
				return diffContainer.createDiffFiles(key, progressMonitor);
			}
		}, cacheSize);
	}

}
