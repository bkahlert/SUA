package de.fu_berlin.imp.seqan.usability_analyzer.diff.util;

import org.eclipse.core.runtime.IProgressMonitor;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.Cache;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFileDirectory;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFileList;

public class DiffCache extends Cache<ID, DiffFileList> {

	public DiffCache(final DiffFileDirectory diffFileDirectory, int cacheSize) {
		super(new CacheFetcher<ID, DiffFileList>() {
			@Override
			public DiffFileList fetch(ID key, IProgressMonitor progressMonitor) {
				return diffFileDirectory.createDiffFiles(key, progressMonitor);
			}
		}, cacheSize);
	}

}
