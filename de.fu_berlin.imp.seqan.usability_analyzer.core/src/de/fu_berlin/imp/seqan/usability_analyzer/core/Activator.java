package de.fu_berlin.imp.seqan.usability_analyzer.core;

import org.apache.log4j.Logger;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.fu_berlin.imp.seqan.usability_analyzer.core.extensionPoints.DateRangeUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "de.fu_berlin.imp.seqan.usability_analyzer.core"; //$NON-NLS-1$

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(Activator.class);

	// The shared instance
	private static Activator plugin;

	private SUACorePreferenceUtil corePreferenceUtil;

	private TimeZoneDate oldDateRangeStart;
	private TimeZoneDate oldDateRangeEnd;
	private boolean oldDateRangeStartEnabled;
	private boolean oldDateRangeEndEnabled;
	private IPropertyChangeListener dateRangeChangeListener = new IPropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			TimeZoneDateRange oldDateRange = new TimeZoneDateRange(
					oldDateRangeStartEnabled ? oldDateRangeStart : null,
					oldDateRangeEndEnabled ? oldDateRangeEnd : null);
			TimeZoneDateRange newDateRange = null;

			TimeZoneDate newDateRangeStart = oldDateRangeStart;
			TimeZoneDate newDateRangeEnd = oldDateRangeEnd;
			boolean newDateRangeStartEnabled = oldDateRangeStartEnabled;
			boolean newDateRangeEndEnabled = oldDateRangeEndEnabled;

			if (corePreferenceUtil.dateRangeStartChanged(event)) {
				newDateRangeStart = new TimeZoneDate(
						(String) event.getNewValue());
			} else if (corePreferenceUtil.dateRangeEndChanged(event)) {
				newDateRangeEnd = new TimeZoneDate((String) event.getNewValue());
			} else if (corePreferenceUtil.dateRangeStartEnabledChanged(event)) {
				newDateRangeStartEnabled = (Boolean) event.getNewValue();
			} else if (corePreferenceUtil.dateRangeEndEnabledChanged(event)) {
				newDateRangeEndEnabled = (Boolean) event.getNewValue();
			}

			newDateRange = new TimeZoneDateRange(
					newDateRangeStartEnabled ? newDateRangeStart : null,
					newDateRangeEndEnabled ? newDateRangeEnd : null);
			DateRangeUtil.notifyDataSourceFilterChanged(oldDateRange,
					newDateRange);

			oldDateRangeStart = newDateRangeStart;
			oldDateRangeEnd = newDateRangeEnd;
			oldDateRangeStartEnabled = newDateRangeStartEnabled;
			oldDateRangeEndEnabled = newDateRangeEndEnabled;
		}
	};

	public Activator() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		corePreferenceUtil = new SUACorePreferenceUtil();

		corePreferenceUtil.addPropertyChangeListener(dateRangeChangeListener);
		oldDateRangeStart = corePreferenceUtil.getDateRangeStart();
		oldDateRangeEnd = corePreferenceUtil.getDateRangeEnd();
		oldDateRangeStartEnabled = corePreferenceUtil
				.getDateRangeStartEnabled();
		oldDateRangeEndEnabled = corePreferenceUtil.getDateRangeEndEnabled();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		corePreferenceUtil
				.removePropertyChangeListener(dateRangeChangeListener);
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
}
