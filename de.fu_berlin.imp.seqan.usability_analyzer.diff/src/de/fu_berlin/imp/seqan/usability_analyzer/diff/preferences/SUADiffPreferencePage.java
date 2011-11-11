package de.fu_berlin.imp.seqan.usability_analyzer.diff.preferences;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import de.fu_berlin.imp.seqan.usability_analyzer.diff.Activator;

public class SUADiffPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	public SUADiffPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	public void createFieldEditors() {
		addField(new DirectoryFieldEditor(
				SUADiffPreferenceConstants.TRUNK_PATH,
				"&Directory preference:", getFieldEditorParent()));
	}

	public void init(IWorkbench workbench) {
	}

}