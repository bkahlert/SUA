package de.fu_berlin.imp.seqan.usability_analyzer.diff.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import de.fu_berlin.imp.seqan.usability_analyzer.diff.Activator;

public class SUADiffPreferenceInitializer extends AbstractPreferenceInitializer {

	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();

	}

}
