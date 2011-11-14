package de.fu_berlin.imp.seqan.usability_analyzer.doclog.preferences;

import org.eclipse.jface.util.PropertyChangeEvent;

import de.fu_berlin.imp.seqan.usability_analyzer.core.util.PreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.Activator;

public class SUADoclogPreferenceUtil extends PreferenceUtil {

	public SUADoclogPreferenceUtil() {
		super(Activator.getDefault());
	}

	public int getScreenshotPageloadTimeout() {
		int screenshotPageloadTimeout = getPreferenceStore().getInt(
				SUADoclogPreferenceConstants.SCREENSHOT_PAGELOAD_TIMEOUT);
		return screenshotPageloadTimeout;
	}

	public boolean screenshotPageloadTimeoutChanged(PropertyChangeEvent event) {
		return event.getProperty().equals(
				SUADoclogPreferenceConstants.SCREENSHOT_PAGELOAD_TIMEOUT);
	}

	public int getScreenshotWidth() {
		int screenshotPageloadTimeout = getPreferenceStore().getInt(
				SUADoclogPreferenceConstants.SCREENSHOT_WIDTH);
		return screenshotPageloadTimeout;
	}

	public void setScreenshotWidth(int width) {
		getPreferenceStore().setValue(SUADoclogPreferenceConstants.SCREENSHOT_WIDTH,
				width);
	}

	public boolean screenshotWidthChanged(PropertyChangeEvent event) {
		return event.getProperty().equals(SUADoclogPreferenceConstants.SCREENSHOT_WIDTH);
	}
}
