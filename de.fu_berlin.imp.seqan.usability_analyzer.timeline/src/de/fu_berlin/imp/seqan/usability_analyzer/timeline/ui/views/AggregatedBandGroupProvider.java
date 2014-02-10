package de.fu_berlin.imp.seqan.usability_analyzer.timeline.ui.views;

import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import com.bkahlert.devel.nebula.colors.RGB;
import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;
import com.bkahlert.nebula.viewer.timeline.impl.AbstractTimelineGroupViewer;
import com.bkahlert.nebula.viewer.timeline.provider.atomic.ITimelineBandLabelProvider;
import com.bkahlert.nebula.viewer.timeline.provider.atomic.ITimelineContentProvider;
import com.bkahlert.nebula.viewer.timeline.provider.atomic.ITimelineEventLabelProvider;
import com.bkahlert.nebula.viewer.timeline.provider.complex.IBandGroupProvider;
import com.bkahlert.nebula.widgets.timelinegroup.impl.TimelineGroup;

/**
 * Displays multiple bands as one single band
 * 
 * @author bkahlert
 * 
 */
public class AggregatedBandGroupProvider<TIMELINEGROUPVIEWER extends AbstractTimelineGroupViewer<TIMELINEGROUP, TIMELINE, INPUT>, TIMELINEGROUP extends TimelineGroup<TIMELINE, INPUT>, TIMELINE extends ITimeline, INPUT>
		implements
		IBandGroupProvider<TIMELINEGROUPVIEWER, TIMELINEGROUP, TIMELINE, INPUT> {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(AggregatedBandGroupProvider.class);
	private List<IBandGroupProvider<TIMELINEGROUPVIEWER, TIMELINEGROUP, TIMELINE, INPUT>> bandGroupProviders = new ArrayList<IBandGroupProvider<TIMELINEGROUPVIEWER, TIMELINEGROUP, TIMELINE, INPUT>>();

	private Map<Object, IBandGroupProvider<TIMELINEGROUPVIEWER, TIMELINEGROUP, TIMELINE, INPUT>> bandToProvider = new HashMap<Object, IBandGroupProvider<TIMELINEGROUPVIEWER, TIMELINEGROUP, TIMELINE, INPUT>>();
	private Map<Object, Object> eventToBand = new HashMap<Object, Object>();

	public AggregatedBandGroupProvider(
			List<IBandGroupProvider<TIMELINEGROUPVIEWER, TIMELINEGROUP, TIMELINE, INPUT>> bandGroupProviders) {
		this.bandGroupProviders.addAll(bandGroupProviders);
	}

	private void clearProviderCache(
			IBandGroupProvider<TIMELINEGROUPVIEWER, TIMELINEGROUP, TIMELINE, INPUT> bandGroupProvider) {
		for (Iterator<Entry<Object, IBandGroupProvider<TIMELINEGROUPVIEWER, TIMELINEGROUP, TIMELINE, INPUT>>> bandToProviderIterator = this.bandToProvider
				.entrySet().iterator(); bandToProviderIterator.hasNext();) {
			Entry<Object, IBandGroupProvider<TIMELINEGROUPVIEWER, TIMELINEGROUP, TIMELINE, INPUT>> bandToProviderEntry = bandToProviderIterator
					.next();
			if (bandToProviderEntry.getValue() == bandGroupProvider) {
				this.clearBandCache(bandToProviderEntry.getKey());
				bandToProviderIterator.remove();
			}
		}
	}

	private void clearBandCache(Object band) {
		for (Iterator<Entry<Object, Object>> eventToBandIterator = this.eventToBand
				.entrySet().iterator(); eventToBandIterator.hasNext();) {
			Entry<Object, Object> eventToBandEntry = eventToBandIterator.next();
			if (eventToBandEntry.getValue() == band) {
				eventToBandIterator.remove();
			}
		}
	}

	@Override
	public ITimelineContentProvider<TIMELINEGROUPVIEWER, TIMELINEGROUP, TIMELINE, INPUT> getContentProvider() {
		return new ITimelineContentProvider<TIMELINEGROUPVIEWER, TIMELINEGROUP, TIMELINE, INPUT>() {

			@Override
			public void inputChanged(TIMELINEGROUPVIEWER viewer,
					INPUT oldInput, INPUT newInput) {
				for (IBandGroupProvider<TIMELINEGROUPVIEWER, TIMELINEGROUP, TIMELINE, INPUT> bandGroupProvider : AggregatedBandGroupProvider.this.bandGroupProviders) {
					bandGroupProvider.getContentProvider().inputChanged(viewer,
							oldInput, newInput);
				}
			}

			@Override
			public boolean isValid(INPUT key) {
				for (IBandGroupProvider<TIMELINEGROUPVIEWER, TIMELINEGROUP, TIMELINE, INPUT> bandGroupProvider : AggregatedBandGroupProvider.this.bandGroupProviders) {
					if (!bandGroupProvider.getContentProvider().isValid(key)) {
						return false;
					}
				}
				return false;
			}

			@Override
			public Object[] getBands(IProgressMonitor monitor) {
				SubMonitor subMonitor = SubMonitor.convert(monitor,
						AggregatedBandGroupProvider.this.bandGroupProviders
								.size());
				for (IBandGroupProvider<TIMELINEGROUPVIEWER, TIMELINEGROUP, TIMELINE, INPUT> bandGroupProvider : AggregatedBandGroupProvider.this.bandGroupProviders) {
					AggregatedBandGroupProvider.this
							.clearProviderCache(bandGroupProvider);
					for (Object band : bandGroupProvider.getContentProvider()
							.getBands(subMonitor.newChild(1))) {
						AggregatedBandGroupProvider.this.bandToProvider.put(
								band, bandGroupProvider);
					}
				}
				subMonitor.done();
				return new Object[] { new Object() };
			}

			@Override
			public Object[] getEvents(Object irrelevant,
					IProgressMonitor monitor) {
				SubMonitor subMonitor = SubMonitor.convert(monitor,
						AggregatedBandGroupProvider.this.bandGroupProviders
								.size());
				List<Object> events = new ArrayList<Object>();
				for (IBandGroupProvider<TIMELINEGROUPVIEWER, TIMELINEGROUP, TIMELINE, INPUT> bandGroupProvider : AggregatedBandGroupProvider.this.bandGroupProviders) {
					for (Object band : AggregatedBandGroupProvider.this.bandToProvider
							.keySet()) {
						for (Object event : bandGroupProvider
								.getContentProvider().getEvents(band,
										subMonitor.newChild(1))) {
							AggregatedBandGroupProvider.this.eventToBand.put(
									event, band);
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
				for (IBandGroupProvider<TIMELINEGROUPVIEWER, TIMELINEGROUP, TIMELINE, INPUT> bandGroupProvider : AggregatedBandGroupProvider.this.bandGroupProviders) {
					Boolean value = bandGroupProvider.getBandLabelProvider()
							.isShowInOverviewBands(null);
					if (value != null) {
						return value;
					}
				}
				return null;
			}

			@Override
			public String getTitle(Object band) {
				for (IBandGroupProvider<TIMELINEGROUPVIEWER, TIMELINEGROUP, TIMELINE, INPUT> bandGroupProvider : AggregatedBandGroupProvider.this.bandGroupProviders) {
					String value = bandGroupProvider.getBandLabelProvider()
							.getTitle(null);
					if (value != null) {
						return value;
					}
				}
				return null;
			}

			@Override
			public Float getRatio(Object band) {
				for (IBandGroupProvider<TIMELINEGROUPVIEWER, TIMELINEGROUP, TIMELINE, INPUT> bandGroupProvider : AggregatedBandGroupProvider.this.bandGroupProviders) {
					Float value = bandGroupProvider.getBandLabelProvider()
							.getRatio(null);
					if (value != null) {
						return value;
					}
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
				for (IBandGroupProvider<TIMELINEGROUPVIEWER, TIMELINEGROUP, TIMELINE, INPUT> bandGroupProvider : AggregatedBandGroupProvider.this.bandGroupProviders) {
					String value = bandGroupProvider.getEventLabelProvider()
							.getTitle(event);
					if (value != null) {
						return value;
					}
				}
				return null;
			}

			@Override
			public String getTooltip(Object event) {
				for (IBandGroupProvider<TIMELINEGROUPVIEWER, TIMELINEGROUP, TIMELINE, INPUT> bandGroupProvider : AggregatedBandGroupProvider.this.bandGroupProviders) {
					String value = bandGroupProvider.getEventLabelProvider()
							.getTooltip(event);
					if (value != null) {
						return value;
					}
				}
				return null;
			}

			@Override
			public Calendar getStart(Object event) {
				for (IBandGroupProvider<TIMELINEGROUPVIEWER, TIMELINEGROUP, TIMELINE, INPUT> bandGroupProvider : AggregatedBandGroupProvider.this.bandGroupProviders) {
					Calendar value = bandGroupProvider.getEventLabelProvider()
							.getStart(event);
					if (value != null) {
						return value;
					}
				}
				return null;
			}

			@Override
			public URI getImage(Object event) {
				for (IBandGroupProvider<TIMELINEGROUPVIEWER, TIMELINEGROUP, TIMELINE, INPUT> bandGroupProvider : AggregatedBandGroupProvider.this.bandGroupProviders) {
					URI value = bandGroupProvider.getEventLabelProvider()
							.getImage(event);
					if (value != null) {
						return value;
					}
				}
				return null;
			}

			@Override
			public URI getIcon(Object event) {
				for (IBandGroupProvider<TIMELINEGROUPVIEWER, TIMELINEGROUP, TIMELINE, INPUT> bandGroupProvider : AggregatedBandGroupProvider.this.bandGroupProviders) {
					URI value = bandGroupProvider.getEventLabelProvider()
							.getIcon(event);
					if (value != null) {
						return value;
					}
				}
				return null;
			}

			@Override
			public Calendar getEnd(Object event) {
				for (IBandGroupProvider<TIMELINEGROUPVIEWER, TIMELINEGROUP, TIMELINE, INPUT> bandGroupProvider : AggregatedBandGroupProvider.this.bandGroupProviders) {
					Calendar value = bandGroupProvider.getEventLabelProvider()
							.getEnd(event);
					if (value != null) {
						return value;
					}
				}
				return null;
			}

			@Override
			public RGB[] getColors(Object event) {
				for (IBandGroupProvider<TIMELINEGROUPVIEWER, TIMELINEGROUP, TIMELINE, INPUT> bandGroupProvider : AggregatedBandGroupProvider.this.bandGroupProviders) {
					RGB[] value = bandGroupProvider.getEventLabelProvider()
							.getColors(event);
					if (value != null) {
						return value;
					}
				}
				return null;
			}

			@Override
			public boolean isResizable(Object event) {
				for (IBandGroupProvider<TIMELINEGROUPVIEWER, TIMELINEGROUP, TIMELINE, INPUT> bandGroupProvider : AggregatedBandGroupProvider.this.bandGroupProviders) {
					boolean value = bandGroupProvider.getEventLabelProvider()
							.isResizable(event);
					if (value == true) {
						return true;
					}
				}
				return false;
			}

			@Override
			public String[] getClassNames(Object event) {
				for (IBandGroupProvider<TIMELINEGROUPVIEWER, TIMELINEGROUP, TIMELINE, INPUT> bandGroupProvider : AggregatedBandGroupProvider.this.bandGroupProviders) {
					String[] value = bandGroupProvider.getEventLabelProvider()
							.getClassNames(event);
					if (value != null) {
						return value;
					}
				}
				return null;
			}
		};
	}

}
