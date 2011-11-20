package de.fu_berlin.imp.seqan.usability_analyzer.core.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;

import de.fu_berlin.imp.seqan.usability_analyzer.core.Activator;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.DateUtil;

public class SUACorePreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
	 * initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		store.setDefault(SUACorePreferenceConstants.DEFAULT_TIME_ZONE,
				"Europe/Berlin");

		long rangeStart = DateUtil.getDate(1970, 0, 1, 0, 0, 0, 0).getTime();
		long rangeEnd = DateUtil.getDate(2050, 0, 1, 0, 0, 0, 0).getTime();

		store.setDefault(SUACorePreferenceConstants.DATE_RANGE_START,
				rangeStart);
		store.setDefault(SUACorePreferenceConstants.DATE_RANGE_END, rangeEnd);
		store.setDefault(SUACorePreferenceConstants.DATE_RANGE_START_ENABLED,
				false);
		store.setDefault(SUACorePreferenceConstants.DATE_RANGE_END_ENABLED,
				false);

		store.setDefault(SUACorePreferenceConstants.DATEFORMAT,
				"yyyy-MM-dd HH:mm:ss Z");

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
