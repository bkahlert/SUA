package de.fu_berlin.imp.apiua.timeline.ui.widgets;

import de.fu_berlin.imp.apiua.core.Activator;
import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.services.IUriPresenterService;
import de.fu_berlin.imp.apiua.core.services.IInformationPresenterService.IInformationBackgroundProvider;
import de.fu_berlin.imp.apiua.core.services.location.ILocatorService;
import de.fu_berlin.imp.apiua.core.ui.viewer.filters.HasDateRange;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.datetime.CalendarRange;
import com.bkahlert.nebula.information.ISubjectInformationProvider;
import com.bkahlert.nebula.utils.CalendarUtils;
import com.bkahlert.nebula.widgets.browser.BrowserUtils;
import com.bkahlert.nebula.widgets.timeline.ITimelineListener;
import com.bkahlert.nebula.widgets.timeline.TimelineEvent;
import com.bkahlert.nebula.widgets.timeline.impl.Timeline;
import com.bkahlert.nebula.widgets.timeline.impl.TimelineAdapter;
import com.bkahlert.nebula.widgets.timeline.model.IDecorator;

/**
 * Instances of this class are timelines in which you can hover over an item to
 * get information about it.
 * 
 * @author bkahlert
 * 
 */
public class InformationPresentingTimeline extends Timeline {

	private static final Logger LOGGER = Logger
			.getLogger(InformationPresentingTimeline.class);

	// TODO move background coloring functionality to TimelineGroupViewer
	private final IUriPresenterService informationPresenterService = (IUriPresenterService) PlatformUI
			.getWorkbench().getService(IUriPresenterService.class);

	private final IInformationBackgroundProvider<URI> informationBackgroundProvider = new IInformationBackgroundProvider<URI>() {
		private final ILocatorService locatorService = (ILocatorService) PlatformUI
				.getWorkbench().getService(ILocatorService.class);

		private boolean intersects(IDecorator[] decorators, CalendarRange range) {
			if (decorators == null) {
				return false;
			}
			for (IDecorator t : decorators) {
				if (new CalendarRange(
						t.getStartDate() != null ? CalendarUtils.fromISO8601(t
								.getStartDate()) : null,
						t.getEndDate() != null ? CalendarUtils.fromISO8601(t
								.getEndDate()) : null).isIntersected(range)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public Color getBackground(URI uri) {
			ILocatable locatable;
			try {
				locatable = this.locatorService.resolve(uri, null).get();
			} catch (Exception e) {
				LOGGER.error("Error resolving " + uri);
				return null;
			}

			if (locatable instanceof HasDateRange) {
				CalendarRange dateRange = ((HasDateRange) locatable)
						.getDateRange().getCalendarRange();

				if (this.intersects(
						InformationPresentingTimeline.this.getDecorators(),
						dateRange)) {
					return Activator.COLOR_HIGHLIGHT;
				}
				if (this.intersects(InformationPresentingTimeline.this
						.getPermanentDecorators(), dateRange)) {
					return Activator.COLOR_STANDARD;
				}
			}
			return null;
		}
	};

	private URI hoveredUri;

	public InformationPresentingTimeline(Composite parent, int style) {

		super(parent, style);

		try {
			this.injectCssFile(BrowserUtils.getFileUrl(InformationPresentingTimeline.class,
					"style.css"));
		} catch (RuntimeException e) {
			LOGGER.error("Could not find style.css", e);
		}

		this.informationPresenterService
				.addInformationBackgroundProvider(this.informationBackgroundProvider);

		this.informationPresenterService
				.enable(this,
						new ISubjectInformationProvider<InformationPresentingTimeline, URI>() {
							private final ITimelineListener timelineListener = new TimelineAdapter() {
								@Override
								public void hoveredIn(TimelineEvent event) {
									if (ILocatable.class.isInstance(event
											.getSource())) {
										InformationPresentingTimeline.this.hoveredUri = ((ILocatable) event
												.getSource()).getUri();
									} else {
										InformationPresentingTimeline.this.hoveredUri = null;
									}
								}

								@Override
								public void hoveredOut(TimelineEvent event) {
									InformationPresentingTimeline.this.hoveredUri = null;
								}
							};

							@Override
							public void register(
									InformationPresentingTimeline subject) {
								subject.addTimelineListener(this.timelineListener);
							}

							@Override
							public void unregister(
									InformationPresentingTimeline subject) {
								subject.removeTimelineListener(this.timelineListener);
							}

							@Override
							public Point getHoverArea() {
								return new Point(20, 10);
							}

							@Override
							public URI getInformation() {
								return InformationPresentingTimeline.this.hoveredUri;
							}
						});
	}

	@Override
	public void dispose() {
		this.informationPresenterService
				.removeInformationBackgroundProvider(this.informationBackgroundProvider);
		this.informationPresenterService.disable(this);
		super.dispose();
	}

	@Override
	public String toString() {
		return InformationPresentingTimeline.class.getSimpleName() + "("
				+ this.getData() + ")";
	}

}
