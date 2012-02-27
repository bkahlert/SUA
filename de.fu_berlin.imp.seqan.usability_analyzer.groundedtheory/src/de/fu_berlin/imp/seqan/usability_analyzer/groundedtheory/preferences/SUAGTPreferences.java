package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.preferences;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.Activator;

public class SUAGTPreferences extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	public SUAGTPreferences() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	public void createFieldEditors() {
		addField(new DirectoryFieldEditor(SUAGTPreferenceConstants.CODESTORE_FILE,
				"&Directory preference:", getFieldEditorParent()));
	}

	public void init(IWorkbench workbench) {
	}

}