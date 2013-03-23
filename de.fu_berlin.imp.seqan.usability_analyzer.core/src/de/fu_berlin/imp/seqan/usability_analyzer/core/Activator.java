package de.fu_berlin.imp.seqan.usability_analyzer.core;

import java.net.URL;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.fu_berlin.imp.seqan.usability_analyzer.core.extensionPoints.DateRangeUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;

// FIXME PropertyChangeService implementieren; dann müssen view wie der MemoView oder der Compiler Output View nicht mehr alle möglichen Listener registrieren, um ein Object immer korrekt darzustellen

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
					Activator.this.oldDateRangeStartEnabled ? Activator.this.oldDateRangeStart
							: null,
					Activator.this.oldDateRangeEndEnabled ? Activator.this.oldDateRangeEnd
							: null);
			TimeZoneDateRange newDateRange = null;

			TimeZoneDate newDateRangeStart = Activator.this.oldDateRangeStart;
			TimeZoneDate newDateRangeEnd = Activator.this.oldDateRangeEnd;
			boolean newDateRangeStartEnabled = Activator.this.oldDateRangeStartEnabled;
			boolean newDateRangeEndEnabled = Activator.this.oldDateRangeEndEnabled;

			if (Activator.this.corePreferenceUtil.dateRangeStartChanged(event)) {
				newDateRangeStart = new TimeZoneDate(
						(String) event.getNewValue());
			} else if (Activator.this.corePreferenceUtil
					.dateRangeEndChanged(event)) {
				newDateRangeEnd = new TimeZoneDate((String) event.getNewValue());
			} else if (Activator.this.corePreferenceUtil
					.dateRangeStartEnabledChanged(event)) {
				newDateRangeStartEnabled = (Boolean) event.getNewValue();
			} else if (Activator.this.corePreferenceUtil
					.dateRangeEndEnabledChanged(event)) {
				newDateRangeEndEnabled = (Boolean) event.getNewValue();
			}

			newDateRange = new TimeZoneDateRange(
					newDateRangeStartEnabled ? newDateRangeStart : null,
					newDateRangeEndEnabled ? newDateRangeEnd : null);
			DateRangeUtil.notifyDataSourceFilterChanged(oldDateRange,
					newDateRange);

			Activator.this.oldDateRangeStart = newDateRangeStart;
			Activator.this.oldDateRangeEnd = newDateRangeEnd;
			Activator.this.oldDateRangeStartEnabled = newDateRangeStartEnabled;
			Activator.this.oldDateRangeEndEnabled = newDateRangeEndEnabled;
		}
	};

	public static final Color COLOR_STANDARD = new Color(Display.getDefault(),
			new RGB(75, 131, 179));

	public static final Color COLOR_HIGHLIGHT = new Color(Display.getDefault(),
			new RGB(216, 255, 38));

	public Activator() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		URL confURL = this.getBundle().getEntry("log4j.properties");
		PropertyConfigurator
				.configure(FileLocator.toFileURL(confURL).getFile());

		this.corePreferenceUtil = new SUACorePreferenceUtil();

		this.corePreferenceUtil
				.addPropertyChangeListener(this.dateRangeChangeListener);
		this.oldDateRangeStart = this.corePreferenceUtil.getDateRangeStart();
		this.oldDateRangeEnd = this.corePreferenceUtil.getDateRangeEnd();
		this.oldDateRangeStartEnabled = this.corePreferenceUtil
				.getDateRangeStartEnabled();
		this.oldDateRangeEndEnabled = this.corePreferenceUtil
				.getDateRangeEndEnabled();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		this.corePreferenceUtil
				.removePropertyChangeListener(this.dateRangeChangeListener);
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
