package de.fu_berlin.imp.seqan.usability_analyzer.timeline.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import de.fu_berlin.imp.seqan.usability_analyzer.timeline.Activator;

public class SUATimelinePreferenceInitializer extends
		AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		store.setDefault(SUATimelinePreferenceConstants.ZOOM_LEVEL, 26);
	}
}
