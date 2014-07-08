package de.fu_berlin.imp.apiua.diff.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import de.fu_berlin.imp.apiua.diff.Activator;

public class SUADiffPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	public SUADiffPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	public void createFieldEditors() {
	}

	public void init(IWorkbench workbench) {
	}

}