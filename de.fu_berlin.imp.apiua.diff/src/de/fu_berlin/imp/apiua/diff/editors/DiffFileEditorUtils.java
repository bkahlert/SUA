package de.fu_berlin.imp.apiua.diff.editors;

import org.apache.log4j.Logger;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.model.WorkbenchAdapter;

import de.fu_berlin.imp.apiua.diff.model.IDiffRecord;
import de.fu_berlin.imp.apiua.diff.model.impl.DiffRecord;

@SuppressWarnings("restriction")
public class DiffFileEditorUtils {

	private static final Logger LOGGER = Logger
			.getLogger(DiffFileEditorUtils.class);

	private static IWorkbenchPage compareEditorPage = null;

	public static IWorkbenchPage createCompareEditorPage()
			throws WorkbenchException {
		if (compareEditorPage != null
				&& compareEditorPage.getWorkbenchWindow() != null
				&& compareEditorPage.getWorkbenchWindow().getShell() != null
				&& !compareEditorPage.getWorkbenchWindow().getShell()
						.isDisposed()) {
			// already working
		} else {
			compareEditorPage = PlatformUI
					.getWorkbench()
					.getActiveWorkbenchWindow()
					.openPage(
							"de.fu_berlin.imp.apiua.EditorOnlyPerspective",
							new IAdaptable() {
								@Override
								public Object getAdapter(
										@SuppressWarnings("rawtypes") Class adapter) {
									return new WorkbenchAdapter() {

									};
								}
							});
			compareEditorPage.getWorkbenchWindow().getShell().setSize(600, 400);
		}
		return compareEditorPage;
	}

	/**
	 * Opens a new {@link CompareEditor} which displays the difference between
	 * the given and its predecessor {@link DiffRecord}.
	 * <p>
	 * If the active page's editor area is not visible a separate window will be
	 * used.
	 * 
	 * @param diffRecord
	 */
	public static void openCompareEditor(IDiffRecord diffRecord) {
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		try {
			if (!page.isEditorAreaVisible()) {
				page = createCompareEditorPage();
			}
		} catch (WorkbenchException e) {
			LOGGER.error(
					"Error opening the compare editor in a separate window", e);
		}
		CompareUI.openCompareEditorOnPage(new DiffFileRecordCompareEditorInput(
				diffRecord), page);
	}

	/**
	 * Closes all {@link CompareEditor}s responsible for the given
	 * {@link DiffRecord}.
	 * 
	 * @param diffRecord
	 */
	public static void closeCompareEditors(IDiffRecord diffRecord) {
		for (IWorkbenchWindow workbenchWindow : PlatformUI.getWorkbench()
				.getWorkbenchWindows()) {
			IEditorReference[] editorReferences = workbenchWindow
					.getActivePage().getEditorReferences();
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

	/**
	 * Closes all {@link CompareEditor}s responsible for {@link DiffRecord}s in
	 * general.
	 */
	public static void closeCompareEditors() {
		for (IWorkbenchWindow workbenchWindow : PlatformUI.getWorkbench()
				.getWorkbenchWindows()) {
			IEditorReference[] editorReferences = workbenchWindow
					.getActivePage().getEditorReferences();
			for (IEditorReference editorReference : editorReferences) {
				try {
					if (editorReference.getEditorInput() instanceof DiffFileRecordCompareEditorInput) {
						editorReference.getPage().closeEditor(
								editorReference.getEditor(true), false);
					}
				} catch (PartInitException e) {
					LOGGER.error("Could not close compare editor", e);
					e.printStackTrace();
				}
			}
		}
	}
}
