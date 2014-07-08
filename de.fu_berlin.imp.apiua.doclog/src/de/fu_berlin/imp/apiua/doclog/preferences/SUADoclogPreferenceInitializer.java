package de.fu_berlin.imp.apiua.doclog.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import de.fu_berlin.imp.apiua.doclog.Activator;

public class SUADoclogPreferenceInitializer extends
		AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
	 * initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(
				SUADoclogPreferenceConstants.SCREENSHOT_PAGELOAD_TIMEOUT, 15000);
		store.setDefault(SUADoclogPreferenceConstants.SCREENSHOT_WIDTH, 200);
	}

}
