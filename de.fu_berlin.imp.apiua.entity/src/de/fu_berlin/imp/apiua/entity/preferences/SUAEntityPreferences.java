package de.fu_berlin.imp.apiua.entity.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import de.fu_berlin.imp.apiua.entity.Activator;

public class SUAEntityPreferences extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	public SUAEntityPreferences() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	public void createFieldEditors() {
		addField(new BooleanFieldEditor(SUAEntityPreferenceConstants.FILTER_DIFFS,
				"Filter &Diff on startup", getFieldEditorParent()));

		addField(new BooleanFieldEditor(SUAEntityPreferenceConstants.FILTER_DIFFS,
				"Filter D&oclocs on startup", getFieldEditorParent()));

		addField(new BooleanFieldEditor(SUAEntityPreferenceConstants.FILTER_DIFFS,
				"Filter &Survey on startup", getFieldEditorParent()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

}