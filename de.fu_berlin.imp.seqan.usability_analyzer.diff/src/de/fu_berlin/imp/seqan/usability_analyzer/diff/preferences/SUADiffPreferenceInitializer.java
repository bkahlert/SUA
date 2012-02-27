package de.fu_berlin.imp.seqan.usability_analyzer.diff.preferences;

import org.apache.commons.lang.SerializationUtils;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import de.fu_berlin.imp.seqan.usability_analyzer.diff.Activator;

public class SUADiffPreferenceInitializer extends AbstractPreferenceInitializer {

	public static final String[] defaultFileFilterPatterns = new String[] {
			"^/?bin(/.*|$)", "^/?core(/.*|$)", "^(.*)?/?CMakeFiles(/.*|$)",
			"^.*\\.cmake$", "^.*CMakeLists.txt$", "^.*Makefile$" };

	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		store.setDefault(
				SUADiffPreferenceConstants.FILE_FILTER_PATTERNS,
				new String(SerializationUtils
						.serialize(defaultFileFilterPatterns)));
	}
}
