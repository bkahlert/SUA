package de.fu_berlin.imp.seqan.usability_analyzer.doclog.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import de.fu_berlin.imp.seqan.usability_analyzer.doclog.Activator;

public class SUADoclogPreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
	 * initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(SUADoclogPreferenceConstants.SCREENSHOT_PAGELOAD_TIMEOUT, 8000);
		store.setDefault(SUADoclogPreferenceConstants.SCREENSHOT_WIDTH, 200);
	}

}
