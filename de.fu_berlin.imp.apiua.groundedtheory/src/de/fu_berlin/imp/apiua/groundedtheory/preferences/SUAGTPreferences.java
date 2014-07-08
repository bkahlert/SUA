package de.fu_berlin.imp.apiua.groundedtheory.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import de.fu_berlin.imp.apiua.groundedtheory.Activator;

public class SUAGTPreferences extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	public SUAGTPreferences() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	public void createFieldEditors() {
		addField(new FileFieldEditor(SUAGTPreferenceConstants.CODESTORE_FILE,
				"&Code store file:", getFieldEditorParent()));
	}

	public void init(IWorkbench workbench) {
	}

}