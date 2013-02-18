package de.fu_berlin.imp.seqan.usability_analyzer.timeline.ui.views;

import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.Viewer;

import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineBandLabelProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineContentProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineEventLabelProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.complex.IBandGroupProvider;

/**
 * Displays multiple bands as one single band
 * 
 * @author bkahlert
 * 
 */
public class AggregatedBandGroupProvider implements IBandGroupProvider {

	private List<IBandGroupProvider> bandGroupProviders = new ArrayList<IBandGroupProvider>();

	private Map<Object, IBandGroupProvider> bandToProvider = new HashMap<Object, IBandGroupProvider>();
	private Map<Object, Object> eventToBand = new HashMap<Object, Object>();

	public AggregatedBandGroupProvider(
			List<IBandGroupProvider> bandGroupProviders) {
		this.bandGroupProviders.addAll(bandGroupProviders);
	}

	private void clearProviderCache(IBandGroupProvider bandGroupProvider) {
		for (Iterator<Entry<Object, IBandGroupProvider>> bandToProviderIterator = bandToProvider
				.entrySet().iterator(); bandToProviderIterator.hasNext();) {
			Entry<Object, IBandGroupProvider> bandToProviderEntry = bandToProviderIterator
					.next();
			if (bandToProviderEntry.getValue() == bandGroupProvider) {
				clearBandCache(bandToProviderEntry.getKey());
				bandToProviderIterator.remove();
			}
		}
	}

	private void clearBandCache(Object band) {
		for (Iterator<Entry<Object, Object>> eventToBandIterator = eventToBand
				.entrySet().iterator(); eventToBandIterator.hasNext();) {
			Entry<Object, Object> eventToBandEntry = eventToBandIterator.next();
			if (eventToBandEntry.getValue() == band) {
				eventToBandIterator.remove();
			}
		}
	}

	@Override
	public ITimelineContentProvider getContentProvider() {
		return new ITimelineContentProvider() {

			@Override
			public boolean isValid(Object key) {
				for (IBandGroupProvider bandGroupProvider : bandGroupProviders) {
					if (!bandGroupProvider.getContentProvider().isValid(key))
						return false;
				}
				return false;
			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
				for (IBandGroupProvider bandGroupProvider : bandGroupProviders) {
					bandGroupProvider.getContentProvider().inputChanged(viewer,
							oldInput, newInput);
				}
			}

			@Override
			public Object[] getBands(IProgressMonitor monitor) {
				SubMonitor subMonitor = SubMonitor.convert(monitor,
						bandGroupProviders.size());
				for (IBandGroupProvider bandGroupProvider : bandGroupProviders) {
					clearProviderCache(bandGroupProvider);
					for (Object band : bandGroupProvider.getContentProvider()
							.getBands(subMonitor.newChild(1))) {
						bandToProvider.put(band, bandGroupProvider);
					}
				}
				subMonitor.done();
				return new Object[] { new Object() };
			}

			@Override
			public Object[] getEvents(Object irrelevant,
					IProgressMonitor monitor) {
				SubMonitor subMonitor = SubMonitor.convert(monitor,
						bandGroupProviders.size());
				List<Object> events = new ArrayList<Object>();
				for (IBandGroupProvider bandGroupProvider : bandGroupProviders) {
					for (Object band : bandToProvider.keySet()) {
						for (Object event : bandGroupProvider
								.getContentProvider().getEvents(band,
										subMonitor.newChild(1))) {
							eventToBand.put(event, band);
							events.add(event);
						}
					}
				}
				subMonitor.done();
				return events.toArray();
			}
		};
	}

	@Override
	public ITimelineBandLabelProvider getBandLabelProvider() {
		return new ITimelineBandLabelProvider() {

			@Override
			public Boolean isShowInOverviewBands(Object band) {
				for (IBandGroupProvider bandGroupProvider : bandGroupProviders) {
					Boolean value = bandGroupProvider.getBandLabelProvider()
							.isShowInOverviewBands(null);
					if (value != null)
						return value;
				}
				return null;
			}

			@Override
			public String getTitle(Object band) {
				for (IBandGroupProvider bandGroupProvider : bandGroupProviders) {
					String value = bandGroupProvider.getBandLabelProvider()
							.getTitle(null);
					if (value != null)
						return value;
				}
				return null;
			}

			@Override
			public Float getRatio(Object band) {
				for (IBandGroupProvider bandGroupProvider : bandGroupProviders) {
					Float value = bandGroupProvider.getBandLabelProvider()
							.getRatio(null);
					if (value != null)
						return value;
				}
				return null;
			}
		};
	}

	@Override
	public ITimelineEventLabelProvider getEventLabelProvider() {
		return new ITimelineEventLabelProvider() {

			@Override
			public String getTitle(Object event) {
				for (IBandGroupProvider bandGroupProvider : bandGroupProviders) {
					String value = bandGroupProvider.getEventLabelProvider()
							.getTitle(event);
					if (value != null)
						return value;
				}
				return null;
			}

			@Override
			public Calendar getStart(Object event) {
				for (IBandGroupProvider bandGroupProvider : bandGroupProviders) {
					Calendar value = bandGroupProvider.getEventLabelProvider()
							.getStart(event);
					if (value != null)
						return value;
				}
				return null;
			}

			@Override
			public URI getImage(Object event) {
				for (IBandGroupProvider bandGroupProvider : bandGroupProviders) {
					URI value = bandGroupProvider.getEventLabelProvider()
							.getImage(event);
					if (value != null)
						return value;
				}
				return null;
			}

			@Override
			public URI getIcon(Object event) {
				for (IBandGroupProvider bandGroupProvider : bandGroupProviders) {
					URI value = bandGroupProvider.getEventLabelProvider()
							.getIcon(event);
					if (value != null)
						return value;
				}
				return null;
			}

			@Override
			public Calendar getEnd(Object event) {
				for (IBandGroupProvider bandGroupProvider : bandGroupProviders) {
					Calendar value = bandGroupProvider.getEventLabelProvider()
							.getEnd(event);
					if (value != null)
						return value;
				}
				return null;
			}

			@Override
			public String getColor(Object event) {
				for (IBandGroupProvider bandGroupProvider : bandGroupProviders) {
					String value = bandGroupProvider.getEventLabelProvider()
							.getColor(event);
					if (value != null)
						return value;
				}
				return null;
			}

			@Override
			public boolean isResizable(Object event) {
				for (IBandGroupProvider bandGroupProvider : bandGroupProviders) {
					boolean value = bandGroupProvider.getEventLabelProvider()
							.isResizable(event);
					if (value == true)
						return true;
				}
				return false;
			}

			@Override
			public String[] getClassNames(Object event) {
				for (IBandGroupProvider bandGroupProvider : bandGroupProviders) {
					String[] value = bandGroupProvider.getEventLabelProvider()
							.getClassNames(event);
					if (value != null)
						return value;
				}
				return null;
			}
		};
	}

}
