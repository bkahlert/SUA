package de.fu_berlin.imp.seqan.usability_analyzer.doclog.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.ScaleFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import de.fu_berlin.imp.seqan.usability_analyzer.doclog.Activator;

public class SUADoclogPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	public SUADoclogPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("A demonstration of a preference page implementation");
	}

	public void createFieldEditors() {
		addField(new ScaleFieldEditor(
				SUADoclogPreferenceConstants.SCREENSHOT_PAGELOAD_TIMEOUT,
				"Screenshot page load timeout", getFieldEditorParent(), 1000,
				60000, 1000, 5000));
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