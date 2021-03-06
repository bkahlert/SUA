package de.fu_berlin.imp.apiua.entity.handlers;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.bkahlert.nebula.utils.selection.SelectionUtils;
import com.bkahlert.nebula.utils.selection.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.apiua.diff.editors.DiffFileRecordCompareEditorInput;
import de.fu_berlin.imp.apiua.diff.model.IDiffRecord;
import de.fu_berlin.imp.apiua.entity.model.Entity;

public class AddCodeInstanceHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		// TODO: move to diff plugin
		ISelection selection = SelectionUtils.getSelection();
		if (selection instanceof TextSelection) {
			IEditorPart editor = HandlerUtil.getActiveEditor(event);
			IEditorInput editorInput = editor.getEditorInput();
			if (editorInput instanceof DiffFileRecordCompareEditorInput) {
				IDiffRecord diffRecord = ((DiffFileRecordCompareEditorInput) editorInput)
						.getDiffFileRecord();

				TextSelection textSeDocument = (TextSelection) selection;
				int startLine = textSeDocument.getStartLine();
				int offset = textSeDocument.getOffset();
				int length = textSeDocument.getLength();
				System.out.println("Selected " + length + " chars in "
						+ diffRecord.getFilename());
			}
		}

		List<Entity> entities = SelectionRetrieverFactory
				.getSelectionRetriever(Entity.class).getSelection();
		if (entities.size() > 0) {
			for (Entity entity : entities) {
				System.out.println("Code added to " + entity);
			}
		}

		return null;
	}

}
