package de.fu_berlin.imp.apiua.diff.commands;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.SimpleMarkerAnnotation;

import com.bkahlert.nebula.utils.selection.SelectionUtils;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.groundedtheory.ui.wizards.WizardUtils;

public class AddCodeInstanceHandler extends AbstractHandler {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(AddCodeInstanceHandler.class);

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
		// loaded in an Image
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
		ILocatable locatable = (ILocatable) Platform.getAdapterManager()
				.getAdapter(selection, ILocatable.class);
		if (locatable != null) {
			WizardUtils.openAddCodeWizard(locatable.getUri(), null); // TODO
																		// null
			// durch
			// Farbe
			// setzen
		}
		return null;
	}
}