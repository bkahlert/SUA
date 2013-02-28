package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services;

import java.net.URI;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.eclipse.jface.viewers.ILabelProvider;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;

public interface ICodeableProvider {
	/**
	 * Returns the object the given code instance ID is associated with.
	 * 
	 * @param codeInstanceID
	 * @return
	 */
	public FutureTask<ICodeable> getCodedObject(URI codeInstanceID);

	/**
	 * Shows / highlights the objects associated with the given code instance
	 * IDs in the active Eclipse Workbench.
	 * 
	 * @param codeInstanceIDs
	 * @param open
	 *            if true also opens the instance
	 * @return true if all {@link URI}s could be resolved and displayed in the
	 *         workbench.
	 */
	public Future<Boolean> showCodedObjectsInWorkspace(
			List<URI> codeInstanceIDs, boolean open);

	/**
	 * Returns a label provider able to provide a label and image for an
	 * {@link ICodeable}.
	 * 
	 * @param codeInstanceID
	 * @return
	 */
	public ILabelProvider getLabelProvider(URI codeInstanceID);
}
