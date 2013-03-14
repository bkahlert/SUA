package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services;

import java.net.URI;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;

public interface ICodeableProvider {

/**
	 * Instances of this class can provide callers of
	 * {@link ICodeableProvider#getLabelProvider(URI) with further information.
	 * @author bkahlert
	 *
	 */
	public static interface IDetailedLabelProvider extends ILabelProvider {
		/**
		 * Returns true if this {@link IDetailedLabelProvider} can fill a popup.
		 * 
		 * @param element
		 * @return
		 */
		public boolean canFillPopup(Object element);

		/**
		 * Fills the given {@link Composite} with detailed information.
		 * 
		 * @param element
		 * @param composite
		 * @return the main control; null if no popup should be displayed.
		 */
		public Control fillPopup(Object element, Composite composite);
	}

	/**
	 * Default implemention of {@link IDetailedLabelProvider}.
	 * 
	 * @author bkahlert
	 * 
	 */
	public static class DetailedLabelProvider extends LabelProvider implements
			IDetailedLabelProvider {

		@Override
		public boolean canFillPopup(Object element) {
			return false;
		}

		@Override
		public Control fillPopup(Object element, Composite composite) {
			return null;
		}

	}

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
