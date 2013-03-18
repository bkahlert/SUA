package de.fu_berlin.imp.seqan.usability_analyzer.timeline.ui.widgets;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.nebula.utils.information.ISubjectInformationProvider;
import com.bkahlert.devel.nebula.utils.information.InformationControl;
import com.bkahlert.devel.nebula.utils.information.InformationControlManager;
import com.bkahlert.devel.nebula.widgets.timeline.ITimelineListener;
import com.bkahlert.devel.nebula.widgets.timeline.TimelineEvent;
import com.bkahlert.devel.nebula.widgets.timeline.impl.Timeline;
import com.bkahlert.devel.nebula.widgets.timeline.impl.TimelineAdapter;
import com.bkahlert.devel.nebula.widgets.timeline.model.IDecorator;

import de.fu_berlin.imp.seqan.usability_analyzer.core.Activator;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IInformationPresenterService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IInformationPresenterService.IInformationBackgroundProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IInformationPresenterService.IInformationToolBarContributionsProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;

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

	private IInformationPresenterService informationPresenterService = (IInformationPresenterService) PlatformUI
			.getWorkbench().getService(IInformationPresenterService.class);

	private IInformationBackgroundProvider informationBackgroundProvider = new IInformationBackgroundProvider() {
		@Override
		public Color getBackground(Object element) {
			if (element instanceof HasDateRange) {
				boolean isIntersected = false;

				TimeZoneDateRange dateRange = ((HasDateRange) element)
						.getDateRange();

				if (InformationPresentingTimeline.this.getDecorators() != null) {
					for (IDecorator t : InformationPresentingTimeline.this
							.getDecorators()) {
						if (new TimeZoneDateRange(
								t.getStartDate() != null ? new TimeZoneDate(
										t.getStartDate()) : null,
								t.getEndDate() != null ? new TimeZoneDate(t
										.getEndDate()) : null)
								.isIntersected(dateRange)) {
							isIntersected = true;
							break;
						}
					}
				}

				return isIntersected ? Activator.COLOR_HIGHLIGHT
						: Activator.COLOR_STANDARD;
			}
			return null;
		}
	};

	private NavigateBackAction navigateBackAction = null;
	private NavigateForwardAction navigateForwardAction = null;
	private Listener navigationListener = new Listener() {
		@Override
		public void handleEvent(Event event) {
			if (event.keyCode == SWT.ARROW_LEFT
					&& InformationPresentingTimeline.this.navigateBackAction != null) {
				InformationPresentingTimeline.this.navigateBackAction.run();
			}
			if (event.keyCode == SWT.ARROW_RIGHT
					&& InformationPresentingTimeline.this.navigateForwardAction != null) {
				InformationPresentingTimeline.this.navigateForwardAction.run();
			}
		}
	};

	private IInformationToolBarContributionsProvider informationToolBarContributionsProvider = new IInformationToolBarContributionsProvider() {
		@Override
		public void fill(final Object element, ToolBarManager toolBarManager,
				final InformationControl<?> informationControl,
				InformationControlManager<?, ?> informationControlManager) {
			toolBarManager.add(new Separator());
			// FIXME Generics geradeziehen
			// FIXME 5lp browsen in vergangenheit hört vorzeitig auf
			if (informationControlManager == InformationPresentingTimeline.this.timelineInformationControlManager) {
				InformationPresentingTimeline.this.navigateBackAction = new NavigateBackAction(
						InformationPresentingTimeline.this, informationControl,
						element);
				InformationPresentingTimeline.this.navigateForwardAction = new NavigateForwardAction(
						InformationPresentingTimeline.this, informationControl,
						element);
				toolBarManager
						.add(InformationPresentingTimeline.this.navigateBackAction);
				toolBarManager
						.add(InformationPresentingTimeline.this.navigateForwardAction);
				informationControl.setFocus();
			}
		}
	};

	private InformationControlManager<InformationPresentingTimeline, ILocatable> timelineInformationControlManager;
	ILocatable hoveredLocatable;

	public InformationPresentingTimeline(Composite parent, int style) {

		super(parent, style);
		try {
			this.injectCssFile(getFileUrl(InformationPresentingTimeline.class,
					"style.css"));
		} catch (RuntimeException e) {
			LOGGER.error("Could not find style.css", e);
		}

		this.informationPresenterService
				.addInformationBackgroundProvider(this.informationBackgroundProvider);
		this.informationPresenterService
				.addInformationToolBarContributionProvider(this.informationToolBarContributionsProvider);

		this.timelineInformationControlManager = this.informationPresenterService
				.enable(this,
						new ISubjectInformationProvider<InformationPresentingTimeline, ILocatable>() {
							private ITimelineListener timelineListener = new TimelineAdapter() {
								@Override
								public void hoveredIn(TimelineEvent event) {
									if (ILocatable.class.isInstance(event
											.getSource())) {
										InformationPresentingTimeline.this.hoveredLocatable = (ILocatable) event
												.getSource();
									} else {
										InformationPresentingTimeline.this.hoveredLocatable = null;
									}
								}

								@Override
								public void hoveredOut(TimelineEvent event) {
									InformationPresentingTimeline.this.hoveredLocatable = null;
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
							public Point getInformationSize() {
								return new Point(20, 10);
							}

							@Override
							public ILocatable getInformation() {
								return InformationPresentingTimeline.this.hoveredLocatable;
							}
						});

		Display.getCurrent().addFilter(SWT.KeyDown, this.navigationListener);
	}

	@Override
	public void dispose() {
		Display.getCurrent().removeFilter(SWT.KeyDown, this.navigationListener);

		this.informationPresenterService
				.removeInformationBackgroundProvider(this.informationBackgroundProvider);
		this.informationPresenterService
				.removeInformationToolBarContributionProvider(this.informationToolBarContributionsProvider);
		this.informationPresenterService.disable(this);
		super.dispose();
	}

	@Override
	public String toString() {
		return InformationPresentingTimeline.class.getSimpleName() + "("
				+ this.getData() + ")";
	}

}
