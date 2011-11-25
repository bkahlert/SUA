package de.fu_berlin.imp.seqan.usability_analyzer.entity.handlers;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;
import com.bkahlert.devel.rcp.selectionUtils.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.seqan.usability_analyzer.diff.editors.DiffFileRecordCompareInput;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFile;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFileRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.model.Entity;

public class AddCodeHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = SelectionUtils.getSelection();
		if (selection instanceof TextSelection) {
			IEditorPart editor = HandlerUtil.getActiveEditor(event);
			IEditorInput editorInput = editor.getEditorInput();
			if (editorInput instanceof DiffFileRecordCompareInput) {
				DiffFileRecord diffFileRecord = ((DiffFileRecordCompareInput) editorInput)
						.getDiffFileRecord();

				TextSelection textSeDocument = (TextSelection) selection;
				int startLine = textSeDocument.getStartLine();
				int offset = textSeDocument.getOffset();
				int length = textSeDocument.getLength();
				System.out.println("Selected " + length + " chars in "
						+ diffFileRecord.getFilename());
			}
		} else {
			List<Entity> entities = SelectionRetrieverFactory
					.getSelectionRetriever(Entity.class).getSelection();
			if (entities.size() > 0) {
				for (Entity entity : entities) {
					System.out.println("Code added to " + entity);
				}
			}

			List<DiffFile> diffFiles = SelectionRetrieverFactory
					.getSelectionRetriever(DiffFile.class).getSelection();
			if (diffFiles.size() > 0) {
				for (DiffFile diffFile : diffFiles) {
					System.out.println("Code added to " + diffFile);
				}
			}

			// TODO

		}
		return null;
	}

}
