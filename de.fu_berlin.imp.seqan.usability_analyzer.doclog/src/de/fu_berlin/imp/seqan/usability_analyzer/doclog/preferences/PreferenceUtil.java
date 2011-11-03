package de.fu_berlin.imp.seqan.usability_analyzer.doclog.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.PropertyChangeEvent;

import de.fu_berlin.imp.seqan.usability_analyzer.doclog.Activator;

public class PreferenceUtil {

	private IPreferenceStore preferenceStore;

	public PreferenceUtil() {
		super();
		preferenceStore = Activator.getDefault().getPreferenceStore();
	}

	public int getScreenshotPageloadTimeout() {
		int screenshotPageloadTimeout = preferenceStore
				.getInt(PreferenceConstants.SCREENSHOT_PAGELOAD_TIMEOUT);
		return screenshotPageloadTimeout;
	}

	public boolean screenshotPageloadTimeoutChanged(PropertyChangeEvent event) {
		return event.getProperty().equals(
				PreferenceConstants.SCREENSHOT_PAGELOAD_TIMEOUT);
	}

	public int getScreenshotWidth() {
		int screenshotPageloadTimeout = preferenceStore
				.getInt(PreferenceConstants.SCREENSHOT_WIDTH);
		return screenshotPageloadTimeout;
	}

	public void setScreenshotWidth(int width) {
		preferenceStore.setValue(PreferenceConstants.SCREENSHOT_WIDTH, width);
	}

	public boolean screenshotWidthChanged(PropertyChangeEvent event) {
		return event.getProperty().equals(PreferenceConstants.SCREENSHOT_WIDTH);
	}
}
