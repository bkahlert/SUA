package de.fu_berlin.imp.seqan.usability_analyzer.diff.preferences;

import java.io.File;

import org.eclipse.jface.util.PropertyChangeEvent;

import de.fu_berlin.imp.seqan.usability_analyzer.core.util.PreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.Activator;

public class SUADiffPreferenceUtil extends PreferenceUtil {

	public SUADiffPreferenceUtil() {
		super(Activator.getDefault());
	}

	public File getTrunkPath() {
		String trunkPath = getPreferenceStore().getString(
				SUADiffPreferenceConstants.TRUNK_PATH);
		return (trunkPath != null && !trunkPath.isEmpty()) ? new File(trunkPath)
				: null;
	}

	public boolean logfilePathChanged(PropertyChangeEvent event) {
		return event.getProperty()
				.equals(SUADiffPreferenceConstants.TRUNK_PATH);
	}
}
