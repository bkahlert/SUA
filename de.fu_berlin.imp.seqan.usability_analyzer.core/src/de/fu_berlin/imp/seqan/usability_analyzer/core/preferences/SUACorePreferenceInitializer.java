package de.fu_berlin.imp.seqan.usability_analyzer.core.preferences;

import java.util.Calendar;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;

import de.fu_berlin.imp.seqan.usability_analyzer.core.Activator;

public class SUACorePreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
	 * initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		Calendar calendar = Calendar.getInstance();
		calendar.set(1970, 0, 0, 0, 0, 0);
		long rangeStart = calendar.getTime().getTime();
		calendar.set(2050, 0, 0, 0, 0, 0);
		long rangeEnd = calendar.getTime().getTime();

		store.setDefault(SUACorePreferenceConstants.DATE_RANGE_START,
				rangeStart);
		store.setDefault(SUACorePreferenceConstants.DATE_RANGE_END, rangeEnd);
		store.setDefault(SUACorePreferenceConstants.DATE_RANGE_START_ENABLED,
				false);
		store.setDefault(SUACorePreferenceConstants.DATE_RANGE_END_ENABLED,
				false);

		store.setDefault(SUACorePreferenceConstants.DATEFORMAT,
				"yyyy-MM-dd HH:mm:ss");

		store.setDefault(SUACorePreferenceConstants.TIMEDIFFERENCEFORMAT,
				"HH'h' mm'm' ss's'");

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
