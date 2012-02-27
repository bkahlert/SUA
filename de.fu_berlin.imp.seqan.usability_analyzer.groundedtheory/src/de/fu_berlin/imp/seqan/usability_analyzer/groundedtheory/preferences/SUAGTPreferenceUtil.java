package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.preferences;

import java.io.File;

import org.eclipse.jface.util.PropertyChangeEvent;

import de.fu_berlin.imp.seqan.usability_analyzer.core.util.PreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.Activator;

public class SUAGTPreferenceUtil extends PreferenceUtil {

	public SUAGTPreferenceUtil() {
		super(Activator.getDefault());
	}

	public File getCodeStoreFile() {
		return new File(getPreferenceStore().getString(
				SUAGTPreferenceConstants.CODESTORE_FILE));
	}

	public File getDefaultCodeStoreFile() {
		return new File(getPreferenceStore().getDefaultString(
				SUAGTPreferenceConstants.CODESTORE_FILE));
	}

	public void setCodeStoreFile(File file) {
		getPreferenceStore().setValue(SUAGTPreferenceConstants.CODESTORE_FILE,
				file.toString());
	}

	public boolean codeStoreFileChanged(PropertyChangeEvent event) {
		return event.getProperty().equals(
				SUAGTPreferenceConstants.CODESTORE_FILE);
	}
}
