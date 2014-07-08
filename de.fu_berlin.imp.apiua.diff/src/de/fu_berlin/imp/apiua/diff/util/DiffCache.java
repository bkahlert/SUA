package de.fu_berlin.imp.apiua.diff.util;

import org.eclipse.core.runtime.IProgressMonitor;

import de.fu_berlin.imp.apiua.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.apiua.core.util.Cache;
import de.fu_berlin.imp.apiua.diff.model.DiffContainer;
import de.fu_berlin.imp.apiua.diff.model.IDiffs;

public class DiffCache extends Cache<IIdentifier, IDiffs> {

	public DiffCache(final DiffContainer diffContainer, int cacheSize) {
		super(new CacheFetcher<IIdentifier, IDiffs>() {
			@Override
			public IDiffs fetch(IIdentifier identifier,
					IProgressMonitor progressMonitor) {
				return diffContainer.createDiffFiles(identifier, progressMonitor);
			}
		}, cacheSize);
	}

}
