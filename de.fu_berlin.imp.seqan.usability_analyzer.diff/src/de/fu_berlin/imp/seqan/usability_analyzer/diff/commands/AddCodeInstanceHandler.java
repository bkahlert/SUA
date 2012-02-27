package de.fu_berlin.imp.seqan.usability_analyzer.diff.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.SimpleMarkerAnnotation;

import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.diff.editors.DiffFileRecordCompareInput;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFileRecord;

public class AddCodeInstanceHandler extends AbstractHandler {

	public static void addAnnotation(IMarker marker, ITextSelection selection,
			ITextEditor editor) {
		// The DocumentProvider enables to get the document currently loaded in
		// the editor
		IDocumentProvider idp = editor.getDocumentProvider();
		// This is the document we want to connect to. This is taken from
		// the current editor input.
		IDocument document = idp.getDocument(editor.getEditorInput());
		// The IannotationModel enables to add/remove/change annotation to a
		// Document
		// loaded in an Editor
		IAnnotationModel iamf = idp.getAnnotationModel(editor.getEditorInput());
		// Note: The annotation type id specify that you want to create one of
		// your
		// annotations
		SimpleMarkerAnnotation ma = new SimpleMarkerAnnotation(
				"com.ibm.example.myannotation", marker);
		// Finally add the new annotation to the model
		iamf.connect(document);
		iamf.addAnnotation(ma,
				new Position(selection.getOffset(), selection.getLength()));
		iamf.disconnect(document);
	}

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
				System.out.println("Selectedssss " + length + " chars in "
						+ diffFileRecord.getFilename());

				// TODO addAnnotation(new Marker, selection, editor);
			}
		}

		return null;
	}
}