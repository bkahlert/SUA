package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import org.apache.log4j.Logger;
import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.seqan.usability_analyzer.diff.editors.DiffFileRecordCompareEditorInput;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;

public class TextSelectionAdapterFactory implements IAdapterFactory {

	private static final Logger LOGGER = Logger
			.getLogger(TextSelectionAdapterFactory.class);

	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] { ICodeable.class, DiffRecordSegment.class };
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof ITextSelection) {
			final ITextSelection textSelection = (ITextSelection) adaptableObject;
			if (adapterType == ICodeable.class) {
				DiffRecord diffRecord = getDiffRecord(textSelection);
				return diffRecord;
			} else if (adaptableObject == DiffRecordSegment.class) {
				DiffRecord diffRecord = getDiffRecord(textSelection);

				Control focusControl = Display.getCurrent().getFocusControl();
				DiffRecord focusedDiffFileRecord = null;

				if (focusControl instanceof StyledText) {
					String text = ((StyledText) focusControl).getText();
					String left = diffRecord.getPredecessor().getSource();
					String right = diffRecord.getSource();
					if (text.equals(left))
						focusedDiffFileRecord = diffRecord.getPredecessor();
					else if (text.equals(right))
						focusedDiffFileRecord = diffRecord;
				} else {
					LOGGER.warn("The control in focus was not of type "
							+ StyledText.class.getSimpleName()
							+ " although this command is only intended to work with one.");
				}

				final int offset = textSelection.getOffset();
				final int length = textSelection.getLength();
				if (focusedDiffFileRecord == null) {
					LOGGER.warn("Could not determine the "
							+ CompareEditor.class.getSimpleName() + " in focus");
					return null;
				}

				DiffRecordSegment segment = new DiffRecordSegment(
						focusedDiffFileRecord, offset, length);
				return segment;
			}
			return null;
		}
		return null;
	}

	private DiffRecord getDiffRecord(ITextSelection textSelection) {
		IEditorPart editor = getResponsibleEditor(textSelection);

		if (editor == null)
			return null;

		IEditorInput editorInput = editor.getEditorInput();
		if (!(editorInput instanceof DiffFileRecordCompareEditorInput))
			return null;

		DiffFileRecordCompareEditorInput diffFileRecordCompareEditorInput = (DiffFileRecordCompareEditorInput) editorInput;
		DiffRecord diffRecord = diffFileRecordCompareEditorInput
				.getDiffFileRecord();
		return diffRecord;
	}

	/**
	 * This method returns the active {@link IEditorPart} start belongs to the
	 * given {@link ITextSelection}.
	 * <p>
	 * Please note that this method does return null if the responsible
	 * {@link IEditorPart} has never been activated.
	 * 
	 * @param textSelection
	 * @return
	 */
	private IEditorPart getResponsibleEditor(ITextSelection textSelection) {
		for (IWorkbenchWindow workbenchWindow : PlatformUI.getWorkbench()
				.getWorkbenchWindows()) {
			if (workbenchWindow.getSelectionService().getSelection()
					.equals(textSelection))
				return workbenchWindow.getActivePage().getActiveEditor();
		}
		return null;
	}

}
