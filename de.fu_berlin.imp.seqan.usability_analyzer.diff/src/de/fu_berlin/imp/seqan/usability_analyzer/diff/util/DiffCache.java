package de.fu_berlin.imp.seqan.usability_analyzer.diff.util;

import org.eclipse.core.runtime.IProgressMonitor;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.Cache;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffList;

public class DiffCache extends Cache<ID, DiffList> {

	public DiffCache(final DiffContainer diffContainer, int cacheSize) {
		super(new CacheFetcher<ID, DiffList>() {
			@Override
			public DiffList fetch(ID key, IProgressMonitor progressMonitor) {
				return diffContainer.createDiffFiles(key, progressMonitor);
			}
		}, cacheSize);
	}

}
