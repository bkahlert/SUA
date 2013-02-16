package de.fu_berlin.imp.seqan.usability_analyzer.timeline.extensionProviders;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.Viewer;

import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineBandLabelProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineContentProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineEventLabelProvider;

/**
 * Wraps a list of {@link ITimelineBandProvider}s and behaves like a single one.
 * 
 * @author bkahlert
 * 
 */
public final class AggregatedTimelineBandProvider implements
		ITimelineBandProvider {
	private final List<ITimelineBandProvider> registeredTimelineBandProviders;

	public AggregatedTimelineBandProvider(
			List<ITimelineBandProvider> registeredTimelineBandProviders) {
		this.registeredTimelineBandProviders = registeredTimelineBandProviders;
	}

	@Override
	public ITimelineEventLabelProvider getEventLabelProvider() {
		return new ITimelineEventLabelProvider() {

			@Override
			public String getTitle(Object event) {
				for (ITimelineBandProvider y : registeredTimelineBandProviders) {
					String rs = y.getEventLabelProvider().getTitle(event);
					if (rs != null && !rs.isEmpty())
						return rs;
				}
				return "";
			}

			@Override
			public Calendar getStart(Object event) {
				for (ITimelineBandProvider y : registeredTimelineBandProviders) {
					Calendar rs = y.getEventLabelProvider().getStart(event);
					if (rs != null)
						return rs;
				}
				return null;
			}

			@Override
			public URI getImage(Object event) {
				for (ITimelineBandProvider y : registeredTimelineBandProviders) {
					URI rs = y.getEventLabelProvider().getImage(event);
					if (rs != null)
						return rs;
				}
				return null;
			}

			@Override
			public URI getIcon(Object event) {
				for (ITimelineBandProvider y : registeredTimelineBandProviders) {
					URI rs = y.getEventLabelProvider().getIcon(event);
					if (rs != null)
						return rs;
				}
				return null;
			}

			@Override
			public Calendar getEnd(Object event) {
				for (ITimelineBandProvider y : registeredTimelineBandProviders) {
					Calendar rs = y.getEventLabelProvider().getEnd(event);
					if (rs != null)
						return rs;
				}
				return null;
			}

			@Override
			public String getColor(Object event) {
				for (ITimelineBandProvider y : registeredTimelineBandProviders) {
					String rs = y.getEventLabelProvider().getColor(event);
					if (rs != null)
						return rs;
				}
				return null;
			}

			@Override
			public String[] getClassNames(Object event) {
				for (ITimelineBandProvider y : registeredTimelineBandProviders) {
					String[] rs = y.getEventLabelProvider()
							.getClassNames(event);
					if (rs != null)
						return rs;
				}
				return new String[0];
			}
		};
	}

	@Override
	public ITimelineContentProvider getContentProvider() {
		return new ITimelineContentProvider() {

			@Override
			public boolean isValid(Object key) {
				for (ITimelineBandProvider x : registeredTimelineBandProviders) {
					if (!x.getContentProvider().isValid(key))
						return false;
				}
				return true;
			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
				for (ITimelineBandProvider x : registeredTimelineBandProviders) {
					x.getContentProvider().inputChanged(viewer, oldInput,
							newInput);
				}
			}

			@Override
			public Object[] getEvents(Object band, IProgressMonitor monitor) {
				List<Object> events = new ArrayList<Object>();
				for (ITimelineBandProvider x : registeredTimelineBandProviders) {
					try {
						List<Object> xx = Arrays.asList(x.getContentProvider()
								.getEvents(band, monitor));
						events.addAll(xx);
					} catch (Exception e) {

					}
				}
				return events.toArray();
			}

			@Override
			public Object[] getBands(IProgressMonitor monitor) {
				List<Object> bands = new ArrayList<Object>();
				for (ITimelineBandProvider x : registeredTimelineBandProviders) {
					List<Object> xx = Arrays.asList(x.getContentProvider()
							.getBands(monitor));
					bands.addAll(xx);
				}
				return bands.toArray();
			}
		};
	}

	@Override
	public ITimelineBandLabelProvider getBandLabelProvider() {
		return new ITimelineBandLabelProvider() {

			@Override
			public Boolean isShowInOverviewBands(Object band) {
				return true;
			}

			@Override
			public String getTitle(Object band) {
				return "All";
			}

			@Override
			public Float getRatio(Object band) {
				return 1f;
			}
		};
	}
}