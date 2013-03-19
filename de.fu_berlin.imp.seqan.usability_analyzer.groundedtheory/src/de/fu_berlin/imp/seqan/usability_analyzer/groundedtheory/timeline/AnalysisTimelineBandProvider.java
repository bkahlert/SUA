package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.timeline;

import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.nebula.colors.RGB;
import com.bkahlert.devel.nebula.viewer.timeline.impl.TimelineGroupViewer;
import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineBandLabelProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineContentProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineEventLabelProvider;
import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;
import com.bkahlert.devel.nebula.widgets.timeline.ITimelineListener;
import com.bkahlert.devel.nebula.widgets.timeline.TimelineEvent;
import com.bkahlert.devel.nebula.widgets.timeline.TimelineGroup;
import com.bkahlert.devel.nebula.widgets.timeline.TimelineHelper;
import com.bkahlert.devel.nebula.widgets.timeline.impl.TimelineAdapter;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.EpisodeLabelProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.timeline.extensionProviders.ITimelineBandProvider;

public class AnalysisTimelineBandProvider
		implements
		ITimelineBandProvider<TimelineGroupViewer<TimelineGroup<ITimeline, IIdentifier>, ITimeline, IIdentifier>, TimelineGroup<ITimeline, IIdentifier>, ITimeline, IIdentifier> {

	private static final Logger LOGGER = Logger
			.getLogger(AnalysisTimelineBandProvider.class);

	private enum BANDS {
		CODE_BAND
	}

	@Override
	public ITimelineContentProvider<TimelineGroupViewer<TimelineGroup<ITimeline, IIdentifier>, ITimeline, IIdentifier>, TimelineGroup<ITimeline, IIdentifier>, ITimeline, IIdentifier> getContentProvider() {
		return new ITimelineContentProvider<TimelineGroupViewer<TimelineGroup<ITimeline, IIdentifier>, ITimeline, IIdentifier>, TimelineGroup<ITimeline, IIdentifier>, ITimeline, IIdentifier>() {

			private final ICodeService codeService = (ICodeService) PlatformUI
					.getWorkbench().getService(ICodeService.class);

			private ITimelineListener timelineListener = new TimelineAdapter() {
				@Override
				public void resized(TimelineEvent event) {
					if (!(event.getSource() instanceof IEpisode)) {
						return;
					}
					IEpisode oldEpisode = (IEpisode) event.getSource();
					IEpisode newEpisode = null;
					TimeZoneDate newStartDate = event.startDate != null ? (TimeZoneDate) new TimeZoneDate(
							event.startDate) : oldEpisode.getStart();
					TimeZoneDate newEndDate = event.endDate != null ? (TimeZoneDate) new TimeZoneDate(
							event.endDate) : oldEpisode.getEnd();
					TimeZoneDateRange range = new TimeZoneDateRange(
							newStartDate, newEndDate);
					newEpisode = oldEpisode.changeRange(range);
					try {
						codeService.replaceEpisodeAndSave(oldEpisode,
								newEpisode);
					} catch (CodeServiceException e) {
						LOGGER.error(
								"Could not update episode " + event.getSource(),
								e);
					}
				}
			};

			private IIdentifier input = null;
			private TimelineRefresher timelineRefresher = null;
			private TimelineGroup<ITimeline, IIdentifier> timelineGroup = null;

			@Override
			public void inputChanged(
					TimelineGroupViewer<TimelineGroup<ITimeline, IIdentifier>, ITimeline, IIdentifier> viewer,
					IIdentifier oldInput, IIdentifier newInput) {
				this.input = newInput;
				if (this.timelineGroup != null) {
					this.timelineGroup
							.removeTimelineListener(this.timelineListener);
				}
				if (viewer != null && viewer.getControl() != null) {
					this.timelineGroup = viewer.getControl();
					this.timelineGroup
							.addTimelineListener(this.timelineListener);
				}

				if (oldInput != null && newInput != null) {
					// do nothing
				} else if (oldInput == null && newInput == null) {
					// do nothing
				} else if (oldInput == null && newInput != null) {
					this.timelineRefresher = new TimelineRefresher(viewer, 500);
					this.codeService
							.addCodeServiceListener(this.timelineRefresher);
				} else {
					this.codeService
							.removeCodeServiceListener(this.timelineRefresher);
					this.timelineRefresher = null;
				}
			}

			@Override
			public boolean isValid(IIdentifier input) {
				return true;
			}

			@Override
			public Object[] getBands(IProgressMonitor monitor) {
				return new Object[] { BANDS.CODE_BAND };
			}

			@Override
			public Object[] getEvents(Object band, IProgressMonitor monitor) {
				SubMonitor subMonitor = SubMonitor.convert(monitor, 2);
				if (!(band instanceof BANDS)) {
					subMonitor.done();
					return new Object[0];
				}

				ICodeService codeService = (ICodeService) PlatformUI
						.getWorkbench().getService(ICodeService.class);
				if (codeService == null) {
					LOGGER.error("Could not get "
							+ ICodeService.class.getSimpleName());
					subMonitor.done();
					return new Object[0];
				}

				switch ((BANDS) band) {
				case CODE_BAND:
					Set<IEpisode> episodes = codeService
							.getEpisodes((IIdentifier) this.input);
					monitor.worked(2);
					return episodes.toArray();
				}

				return new Object[0];
			}
		};
	}

	@Override
	public ITimelineBandLabelProvider getBandLabelProvider() {
		return new ITimelineBandLabelProvider() {

			@Override
			public String getTitle(Object band) {
				if (band instanceof BANDS) {
					switch ((BANDS) band) {
					case CODE_BAND:
						return "Analysis";
					}
				}
				return "";
			}

			@Override
			public Boolean isShowInOverviewBands(Object band) {
				if (band instanceof BANDS) {
					switch ((BANDS) band) {
					case CODE_BAND:
						return true;
					}
				}
				return null;
			}

			@Override
			public Float getRatio(Object band) {
				if (band instanceof BANDS) {
					switch ((BANDS) band) {
					case CODE_BAND:
						return 0.20f;
					}
				}
				return null;
			}
		};
	}

	@Override
	public ITimelineEventLabelProvider getEventLabelProvider() {
		return new ITimelineEventLabelProvider() {

			private ICodeService codeService = (ICodeService) PlatformUI
					.getWorkbench().getService(ICodeService.class);

			private ILabelProvider episodeLabelProvider = new EpisodeLabelProvider();

			@Override
			public String getTitle(Object event) {
				return this.episodeLabelProvider.getText(event);
			}

			@Override
			public String getTooltip(Object event) {
				return null;
			}

			@Override
			public URI getIcon(Object event) {
				Image image = this.episodeLabelProvider.getImage(event);
				if (image != null) {
					return TimelineHelper.createUriFromImage(image);
				}
				return null;
			}

			@Override
			public URI getImage(Object event) {
				return null;
			}

			@Override
			public Calendar getStart(Object event) {
				if (event instanceof IEpisode) {
					IEpisode episode = (IEpisode) event;
					return episode.getStart().getCalendar();
				}
				return null;
			}

			@Override
			public Calendar getEnd(Object event) {
				if (event instanceof IEpisode) {
					IEpisode episode = (IEpisode) event;
					return episode.getEnd().getCalendar();
				}
				return null;
			}

			@Override
			public RGB[] getColors(Object event) {
				List<RGB> colors = new ArrayList<RGB>();
				if (event instanceof IEpisode) {
					IEpisode episode = (IEpisode) event;
					try {
						for (ICode code : this.codeService.getCodes(episode)) {
							colors.add(code.getColor());
						}
					} catch (CodeServiceException e) {
						LOGGER.error(e);
					}
				}
				return colors.toArray(new RGB[0]);
			}

			@Override
			public boolean isResizable(Object event) {
				return event instanceof IEpisode;
			}

			@Override
			public String[] getClassNames(Object event) {
				if (event instanceof ICodeInstance) {
					return new String[] { "CODE_INSTANCE" };
				} else if (event instanceof IEpisode) {
					return new String[] { "EPISODE" };
				}
				return new String[0];
			}
		};
	}
}
