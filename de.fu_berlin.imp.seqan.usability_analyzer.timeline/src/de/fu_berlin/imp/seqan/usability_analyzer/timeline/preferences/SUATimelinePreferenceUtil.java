package de.fu_berlin.imp.seqan.usability_analyzer.timeline.preferences;

import java.util.Calendar;

import org.eclipse.core.runtime.Assert;

import com.bkahlert.nebula.utils.CalendarUtils;
import com.bkahlert.nebula.utils.EclipsePreferenceUtil;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.timeline.Activator;

public class SUATimelinePreferenceUtil extends EclipsePreferenceUtil {

	private static String getZoomIndexKey(IIdentifier identifier) {
		Assert.isLegal(identifier != null);
		return "zoomIndex." + identifier.toString();
	}

	private static String getCenterStartDateKey(IIdentifier identifier) {
		Assert.isLegal(identifier != null);
		return "centerStartDate." + identifier.toString();
	}

	public SUATimelinePreferenceUtil() {
		super(Activator.getDefault());
	}

	public int getZoomIndex(IIdentifier identifier) {
		String key = getZoomIndexKey(identifier);
		this.getPreferenceStore().setDefault(key, 26);
		int zoomLevel = this.getPreferenceStore().getInt(key);
		return zoomLevel;
	}

	public void setZoomIndex(IIdentifier identifier, int zoomLevel) {
		this.getPreferenceStore().setValue(getZoomIndexKey(identifier),
				zoomLevel);
	}

	public Calendar getCenterStartDate(IIdentifier identifier) {
		String centerStartDate = this.getPreferenceStore().getString(
				getCenterStartDateKey(identifier));
		try {
			return CalendarUtils.fromISO8601(centerStartDate);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	public void setCenterStartDate(IIdentifier identifier,
			Calendar centerStartDate) {
		String str = centerStartDate != null ? CalendarUtils
				.toISO8601(centerStartDate) : "";
		this.getPreferenceStore().setValue(getCenterStartDateKey(identifier),
				str);
	}
}
