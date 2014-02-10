package de.fu_berlin.imp.seqan.usability_analyzer.timeline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.fu_berlin.imp.seqan.usability_analyzer.timeline.extensionProviders.ITimelineBandProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.timeline.ui.views.TimelineView;

public class Activator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "de.fu_berlin.imp.seqan.usability_analyzer.timeline";

	private static final Logger LOGGER = Logger.getLogger(Activator.class);

	private static Activator plugin;

	/**
	 * Gets the registered {@link ITimelineBandProvider}s provided via the
	 * corresponding extension point.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <INPUT> List<ITimelineBandProvider<INPUT>> getRegisteredTimelineBandProviders() {
		IConfigurationElement[] config = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(
						"de.fu_berlin.imp.seqan.usability_analyzer.timeline");
		final List<ITimelineBandProvider<INPUT>> registeredTimelineBandProviders = new ArrayList<ITimelineBandProvider<INPUT>>();
		for (IConfigurationElement configElement : config) {
			try {
				Object o = configElement.createExecutableExtension("class");
				if (o instanceof ITimelineBandProvider) {
					try {
						registeredTimelineBandProviders
								.add((ITimelineBandProvider<INPUT>) o);
					} catch (ClassCastException ex) {
						LOGGER.error("The provided "
								+ ITimelineBandProvider.class.getSimpleName()
								+ " could not be cast.", ex);
					}
				}
			} catch (CoreException e1) {
				TimelineView.LOGGER.error(
						"Error retrieving a currently registered "
								+ ITimelineBandProvider.class.getSimpleName(),
						e1);
				return null;
			}
		}

		// sort alphabetically
		Collections.sort(registeredTimelineBandProviders,
				new Comparator<ITimelineBandProvider<INPUT>>() {
					@Override
					public int compare(ITimelineBandProvider<INPUT> o1,
							ITimelineBandProvider<INPUT> o2) {
						return o1.getClass().getSimpleName()
								.compareTo(o2.getClass().getSimpleName());
					}
				});
		return registeredTimelineBandProviders;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
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
