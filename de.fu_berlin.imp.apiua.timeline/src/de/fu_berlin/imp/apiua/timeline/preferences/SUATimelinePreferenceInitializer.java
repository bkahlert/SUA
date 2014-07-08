package de.fu_berlin.imp.apiua.timeline.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import de.fu_berlin.imp.apiua.timeline.Activator;

public class SUATimelinePreferenceInitializer extends
		AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		@SuppressWarnings("unused")
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();

	}
}
