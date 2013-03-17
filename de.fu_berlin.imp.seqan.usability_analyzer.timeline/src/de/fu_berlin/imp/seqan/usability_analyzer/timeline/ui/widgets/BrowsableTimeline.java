package de.fu_berlin.imp.seqan.usability_analyzer.timeline.ui.widgets;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.nebula.utils.information.ISubjectInformationProvider;
import com.bkahlert.devel.nebula.widgets.timeline.ITimelineListener;
import com.bkahlert.devel.nebula.widgets.timeline.TimelineEvent;
import com.bkahlert.devel.nebula.widgets.timeline.impl.Timeline;
import com.bkahlert.devel.nebula.widgets.timeline.impl.TimelineAdapter;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IInformationPresenterService;

/**
 * Instances of this class are timelines in which you can double click on an
 * item to get detailed information about it.
 * 
 * @author bkahlert
 * 
 */
public class BrowsableTimeline extends Timeline {

	private static final Logger LOGGER = Logger
			.getLogger(BrowsableTimeline.class);

	private IInformationPresenterService informationPresenterService = (IInformationPresenterService) PlatformUI
			.getWorkbench().getService(IInformationPresenterService.class);

	public BrowsableTimeline(Composite parent, int style) {
		super(parent, style);
		try {
			this.injectCssFile(getFileUrl(BrowsableTimeline.class, "style.css"));
		} catch (RuntimeException e) {
			LOGGER.error("Could not find style.css", e);
		}

		this.addTimelineListener(new TimelineAdapter() {
			@Override
			public void hoveredIn(TimelineEvent event) {
				if (event.getSource() instanceof ILocatable) {
					// FIXME
					// BrowsableTimeline.this.informationPresenterService
					// .showDetailPopup((ILocatable) event.getSource());
				}
			}

			@Override
			public void doubleClicked(TimelineEvent event) {
				// FIXME remove
				TimelineDetailDialog detailDialog = new TimelineDetailDialog(
						null, BrowsableTimeline.this);
				detailDialog.setBlockOnOpen(false);
				detailDialog.open();
				detailDialog.load(event.getSource());
				detailDialog.getShell().setActive();
			}
		});

		this.informationPresenterService
				.enable(this,
						new ISubjectInformationProvider<BrowsableTimeline, ILocatable>() {
							private ILocatable hoveredLocatable = null;
							private ITimelineListener timelineListener = new TimelineAdapter() {
								@Override
								public void hoveredIn(TimelineEvent event) {
									if (ILocatable.class.isInstance(event
											.getSource())) {
										hoveredLocatable = (ILocatable) event
												.getSource();
									} else {
										hoveredLocatable = null;
									}
								}

								@Override
								public void hoveredOut(TimelineEvent event) {
									hoveredLocatable = null;
								}
							};

							@Override
							public void register(BrowsableTimeline subject) {
								subject.addTimelineListener(this.timelineListener);
							}

							@Override
							public void unregister(BrowsableTimeline subject) {
								subject.removeTimelineListener(this.timelineListener);
							}

							@Override
							public Point getInformationSize() {
								return new Point(20, 10);
							}

							@Override
							public ILocatable getInformation() {
								return this.hoveredLocatable;
							}
						});
	}

	@Override
	public void dispose() {
		this.informationPresenterService.disable(this);
		super.dispose();
	}

	@Override
	public String toString() {
		return BrowsableTimeline.class.getSimpleName() + "(" + this.getData()
				+ ")";
	}

}
