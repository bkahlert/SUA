package de.fu_berlin.imp.seqan.usability_analyzer.diff.editors;

import org.apache.log4j.Logger;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffRecord;

public class DiffFileEditorUtils {

	private static final Logger LOGGER = Logger
			.getLogger(DiffFileEditorUtils.class);

	/**
	 * Opens a new {@link CompareEditor} which displays the difference between
	 * the given and its predecessor {@link DiffRecord}.
	 * 
	 * @param diffRecord
	 */
	public static void openCompareEditor(DiffRecord diffRecord) {
		CompareUI.openCompareEditor(new DiffFileRecordCompareEditorInput(
				diffRecord));
	}

	/**
	 * Closes all {@link CompareEditor}s responsible for the given
	 * {@link DiffRecord}.
	 * 
	 * @param diffRecord
	 */
	public static void closeCompareEditors(DiffRecord diffRecord) {
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
					if (currentFilename.equals(diffRecord.getFilename())) {
						editorReference.getPage().closeEditor(
								editorReference.getEditor(true), false);
					}
				}
			} catch (PartInitException e) {
				LOGGER.error("Could not close compare editor", e);
				e.printStackTrace();
			}
		}
	}
}
