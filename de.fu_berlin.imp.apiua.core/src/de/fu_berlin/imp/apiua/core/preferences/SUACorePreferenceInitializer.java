package de.fu_berlin.imp.apiua.core.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;

import de.fu_berlin.imp.apiua.core.Activator;
import de.fu_berlin.imp.apiua.core.model.TimeZoneDate;

public class SUACorePreferenceInitializer extends AbstractPreferenceInitializer {

	public static final String DEFAULT_SMART_DATETIME = "''yy-MM-dd HH:mm:ss";
	public static final String DEFAULT_TIMEDIFFERENCEFORMAT = "HH'h' mm'm' ss's'";

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		store.setDefault(SUACorePreferenceConstants.DATA_DIRECTORIES, "");

		store.setDefault(SUACorePreferenceConstants.DEFAULT_TIME_ZONE,
				"Europe/Berlin");

		store.setDefault(SUACorePreferenceConstants.DATE_RANGE_START,
				new TimeZoneDate("1970-01-01T00:00:00+00:00").toISO8601());
		store.setDefault(SUACorePreferenceConstants.DATE_RANGE_END,
				new TimeZoneDate("2050-01-01T00:00:00+00:00").toISO8601());
		store.setDefault(SUACorePreferenceConstants.DATE_RANGE_START_ENABLED,
				false);
		store.setDefault(SUACorePreferenceConstants.DATE_RANGE_END_ENABLED,
				false);

		store.setDefault(SUACorePreferenceConstants.DATEFORMAT,
				"yyyy-MM-dd HH:mm:ss Z");

		store.setDefault(SUACorePreferenceConstants.TIMEDIFFERENCEFORMAT,
				DEFAULT_TIMEDIFFERENCEFORMAT);

		PreferenceConverter.setDefault(store,
				SUACorePreferenceConstants.COLOR_OK, new RGB(122, 163, 94));
		PreferenceConverter.setDefault(store,
				SUACorePreferenceConstants.COLOR_DIRTY, new RGB(236, 175, 86));
		PreferenceConverter.setDefault(store,
				SUACorePreferenceConstants.COLOR_ERROR, new RGB(200, 67, 66));
		PreferenceConverter
				.setDefault(store, SUACorePreferenceConstants.COLOR_MISSING,
						new RGB(130, 84, 139));
	}
}
