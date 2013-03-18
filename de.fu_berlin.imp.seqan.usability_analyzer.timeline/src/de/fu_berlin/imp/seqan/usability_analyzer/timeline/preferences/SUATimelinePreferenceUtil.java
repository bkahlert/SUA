package de.fu_berlin.imp.seqan.usability_analyzer.timeline.preferences;

import de.fu_berlin.imp.seqan.usability_analyzer.core.util.PreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.timeline.Activator;

public class SUATimelinePreferenceUtil extends PreferenceUtil {

	public SUATimelinePreferenceUtil() {
		super(Activator.getDefault());
	}

	public int getZoomIndex() {
		int zoomLevel = this.getPreferenceStore().getInt(
				SUATimelinePreferenceConstants.ZOOM_LEVEL);
		return zoomLevel;
	}

	public void setZoomIndex(int zoomLevel) {
		this.getPreferenceStore().setValue(
				SUATimelinePreferenceConstants.ZOOM_LEVEL, zoomLevel);
	}

}
