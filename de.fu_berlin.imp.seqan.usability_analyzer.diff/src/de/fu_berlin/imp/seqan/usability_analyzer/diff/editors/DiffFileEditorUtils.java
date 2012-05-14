package de.fu_berlin.imp.seqan.usability_analyzer.diff.editors;

import org.eclipse.compare.CompareUI;
import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFileRecord;

@SuppressWarnings("restriction")
public class DiffFileEditorUtils {
	/**
	 * Opens a new {@link CompareEditor} which displays the difference between
	 * the given and its predecessor {@link DiffFileRecord}.
	 * 
	 * @param diffFileRecord
	 */
	public static void openCompareEditor(DiffFileRecord diffFileRecord) {
		CompareUI.openCompareEditor(new DiffFileRecordCompareEditorInput(
				diffFileRecord));
	}

	/**
	 * Closes all {@link CompareEditor}s responsible for the given
	 * {@link DiffFileRecord}.
	 * 
	 * @param diffFileRecord
	 */
	public static void closeCompareEditors(DiffFileRecord diffFileRecord) {
		IEditorReference[] editorReferences = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage()
				.getEditorReferences();
		for (IEditorReference editorReference : editorReferences) {
			try {
				if (editorReference.getEditorInput() instanceof DiffFileRecordCompareEditorInput) {
					DiffFileRecordCompareEditorInput currentCompareInput = (DiffFileRecordCompareEditorInput) editorReference
							.getEditorInput();
					String currentFilename = currentCompareInput
							.getDiffFileRecord().getFilename();
					if (currentFilename.equals(diffFileRecord.getFilename())) {
						editorReference.getPage().closeEditor(
								editorReference.getEditor(true), false);
					}
				}
			} catch (PartInitException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
