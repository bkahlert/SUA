package de.fu_berlin.imp.seqan.usability_analyzer.core;

import java.util.Date;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.fu_berlin.imp.seqan.usability_analyzer.core.extensionPoints.DateRangeUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "de.fu_berlin.imp.seqan.usability_analyzer.core"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	private SUACorePreferenceUtil preferenceUtil;

	private DateRange oldDateRange;
	private IPropertyChangeListener dateRangeChangeListener = new IPropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			DateRange newDateRange = null;

			if (preferenceUtil.dateRangeStartChanged(event)) {
				newDateRange = new DateRange(new Date(
						(Long) event.getNewValue()),
						(oldDateRange != null) ? oldDateRange.getEndDate()
								: null);
			}

			if (preferenceUtil.dateRangeEndChanged(event)) {
				newDateRange = new DateRange(
						(oldDateRange != null) ? oldDateRange.getStartDate()
								: null, new Date((Long) event.getNewValue()));
			}

			if (newDateRange != null) {
				DateRangeUtil.notifyDataSourceFilterChanged(oldDateRange,
						newDateRange);
				oldDateRange = newDateRange;
			}
		}
	};

	/**
	 * The constructor
	 */
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
		preferenceUtil = new SUACorePreferenceUtil();
		preferenceUtil.addPropertyChangeListener(dateRangeChangeListener);
		oldDateRange = new DateRange(preferenceUtil.getDateRangeStart(),
				preferenceUtil.getDateRangeEnd());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		preferenceUtil.removePropertyChangeListener(dateRangeChangeListener);
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
