package de.fu_berlin.imp.seqan.usability_analyzer.timeline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import de.fu_berlin.imp.seqan.usability_analyzer.timeline.extensionProviders.ITimelineBandProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.timeline.extensionProviders.ITimelineEventDetailProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.timeline.ui.views.TimelineView;

public class Activator implements BundleActivator {

	public static final String PLUGIN_ID = "de.fu_berlin.imp.seqan.usability_analyzer.timeline";

	private static BundleContext context;

	/**
	 * Gets the registered {@link ITimelineBandProvider}s provided via the
	 * corresponding extension point.
	 * 
	 * @return
	 */
	public static List<ITimelineBandProvider> getRegisteredTimelineBandProviders() {
		IConfigurationElement[] config = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(
						"de.fu_berlin.imp.seqan.usability_analyzer.timeline");
		final List<ITimelineBandProvider> registeredTimelineBandProviders = new ArrayList<ITimelineBandProvider>();
		for (IConfigurationElement e : config) {
			try {
				Object o = e.createExecutableExtension("class");
				if (o instanceof ITimelineBandProvider) {
					registeredTimelineBandProviders
							.add((ITimelineBandProvider) o);
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
				new Comparator<ITimelineBandProvider>() {
					@Override
					public int compare(ITimelineBandProvider o1,
							ITimelineBandProvider o2) {
						return o1.getClass().getSimpleName()
								.compareTo(o2.getClass().getSimpleName());
					}
				});
		return registeredTimelineBandProviders;
	}

	/**
	 * Gets the registered {@link ITimelineEventDetailProvider}s provided via
	 * the corresponding extension point.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<ITimelineEventDetailProvider<Object>> getRegisteredTimelineEventDetailProviders() {
		IConfigurationElement[] config = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(
						"de.fu_berlin.imp.seqan.usability_analyzer.timeline");
		List<ITimelineEventDetailProvider<Object>> registeredTimelineEventDetailProvider = new ArrayList<ITimelineEventDetailProvider<Object>>();
		for (IConfigurationElement e : config) {
			try {
				Object o = e.createExecutableExtension("class");
				if (o instanceof ITimelineEventDetailProvider<?>) {
					registeredTimelineEventDetailProvider
							.add((ITimelineEventDetailProvider<Object>) o);
				}
			} catch (CoreException e1) {
				TimelineView.LOGGER.error(
						"Error retrieving a currently registered "
								+ ITimelineBandProvider.class.getSimpleName(),
						e1);
				return null;
			}
		}

		return registeredTimelineEventDetailProvider;
	}

	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
	}

	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

}
