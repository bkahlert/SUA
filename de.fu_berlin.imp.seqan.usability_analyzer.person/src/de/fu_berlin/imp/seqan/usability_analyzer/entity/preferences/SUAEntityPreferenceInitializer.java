package de.fu_berlin.imp.seqan.usability_analyzer.entity.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import de.fu_berlin.imp.seqan.usability_analyzer.entity.Activator;

/**
 * Class used to initialize default preference values.
 */
public class SUAEntityPreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
	 * initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(SUAEntityPreferenceConstants.FILTER_DIFFS, true);
		store.setDefault(SUAEntityPreferenceConstants.FILTER_DOCLOGS, false);
		store.setDefault(SUAEntityPreferenceConstants.FILTER_SURVEYS, false);
	}

}
