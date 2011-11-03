package de.fu_berlin.imp.seqan.usability_analyzer.core.preferences;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import de.fu_berlin.imp.seqan.usability_analyzer.core.Activator;

public class SUACorePreferences extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	public SUACorePreferences() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("General settings");
	}

	public void createFieldEditors() {
		addField(new DirectoryFieldEditor(
				SUACorePreferenceConstants.LOGFILE_PATH,
				"&Log file directory:", getFieldEditorParent()));
		addField(new FileFieldEditor(
				SUACorePreferenceConstants.SURVEYFILE_PATH, "&Survey file:",
				getFieldEditorParent()));

		addField(new StringFieldEditor(SUACorePreferenceConstants.DATEFORMAT,
				"Date format", getFieldEditorParent()));
		addField(new StringFieldEditor(
				SUACorePreferenceConstants.TIMEDIFFERENCEFORMAT,
				"Time difference format", getFieldEditorParent()));

		addField(new ColorFieldEditor(SUACorePreferenceConstants.COLOR_OK,
				"&OK color:", getFieldEditorParent()));
		addField(new ColorFieldEditor(SUACorePreferenceConstants.COLOR_DIRTY,
				"&Dirty color", getFieldEditorParent()));
		addField(new ColorFieldEditor(SUACorePreferenceConstants.COLOR_ERROR,
				"&Error color", getFieldEditorParent()));
		addField(new ColorFieldEditor(SUACorePreferenceConstants.COLOR_MISSING,
				"&Missing color", getFieldEditorParent()));
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