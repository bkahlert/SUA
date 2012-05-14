package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.preferences;

import java.io.File;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.Activator;

/**
 * Class used to initialize default preference values.
 */
public class SUAGTPreferenceInitializer extends AbstractPreferenceInitializer {

	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		String file = ResourcesPlugin.getWorkspace().getRoot().getLocation()
				.toOSString()
				+ File.separator + "CodeStore.xml";
		store.setDefault(SUAGTPreferenceConstants.CODESTORE_FILE, file);
	}
}
