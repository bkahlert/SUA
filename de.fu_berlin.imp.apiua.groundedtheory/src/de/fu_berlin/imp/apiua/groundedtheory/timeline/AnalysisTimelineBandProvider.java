package de.fu_berlin.imp.apiua.groundedtheory.timeline;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.ImageUtils;
import com.bkahlert.nebula.utils.colors.RGB;
import com.bkahlert.nebula.viewer.timeline.ITimelineGroupViewer;
import com.bkahlert.nebula.viewer.timeline.provider.atomic.ITimelineBandLabelProvider;
import com.bkahlert.nebula.viewer.timeline.provider.atomic.ITimelineContentProvider;
import com.bkahlert.nebula.viewer.timeline.provider.atomic.ITimelineEventLabelProvider;
import com.bkahlert.nebula.widgets.timeline.IBaseTimeline;
import com.bkahlert.nebula.widgets.timeline.ITimelineListener;
import com.bkahlert.nebula.widgets.timeline.TimelineEvent;
import com.bkahlert.nebula.widgets.timeline.impl.TimelineAdapter;
import com.bkahlert.nebula.widgets.timelinegroup.impl.BaseTimelineGroup;
import com.bkahlert.nebula.widgets.timelinegroup.impl.TimelineGroup;

import de.fu_berlin.imp.apiua.core.model.TimeZoneDate;
import de.fu_berlin.imp.apiua.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.apiua.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.apiua.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.groundedtheory.storage.ICodeInstance;
import de.fu_berlin.imp.apiua.groundedtheory.ui.GTLabelProvider;
import de.fu_berlin.imp.apiua.timeline.extensionProviders.ITimelineBandProvider;

public class AnalysisTimelineBandProvider implements
		ITimelineBandProvider<IIdentifier> {

	private static final Logger LOGGER = Logger
			.getLogger(AnalysisTimelineBandProvider.class);

	private enum BANDS {
		CODE_BAND
	}

	@Override
	public ITimelineContentProvider<IIdentifier> getContentProvider() {
		return new ITimelineContentProvider<IIdentifier>() {

			private final ICodeService codeService = (ICodeService) PlatformUI
					.getWorkbench().getService(ICodeService.class);

			private final ITimelineListener timelineListener = new TimelineAdapter() {
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
			private BaseTimelineGroup<? extends IBaseTimeline, IIdentifier> timelineGroup = null;

			@Override
			public <TIMELINE extends IBaseTimeline> void inputChanged(
					ITimelineGroupViewer<TIMELINE, IIdentifier> viewer,
					IIdentifier oldInput, IIdentifier newInput) {
				this.input = newInput;
				if (this.timelineGroup instanceof TimelineGroup<?, ?>) {
					((TimelineGroup<?, ?>) this.timelineGroup)
							.removeTimelineListener(this.timelineListener);
				}
				if (viewer != null && viewer.getControl() != null) {
					this.timelineGroup = viewer.getControl();
					((TimelineGroup<?, ?>) this.timelineGroup)
							.addTimelineListener(this.timelineListener);
				}

				if (oldInput != null && newInput != null) {
					// do nothing
				} else if (oldInput == null && newInput == null) {
					// do nothing
				} else if (oldInput == null && newInput != null) {
					if (viewer instanceof Viewer) {
						this.timelineRefresher = new TimelineRefresher(
								(Viewer) viewer, 500);
					}
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
							.getEpisodes(this.input);
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

			private final ICodeService codeService = (ICodeService) PlatformUI
					.getWorkbench().getService(ICodeService.class);

			private final ILabelProvider episodeLabelProvider = new GTLabelProvider();

			@Override
			public String getTitle(Object event) {
				return this.episodeLabelProvider.getText(event);
			}

			@Override
			public String getTooltip(Object event) {
				return null;
			}

			@Override
			public java.net.URI getIcon(Object event) {
				Image image = this.episodeLabelProvider.getImage(event);
				if (image != null) {
					return ImageUtils.createUriFromImage(image);
				}
				return null;
			}

			@Override
			public java.net.URI getImage(Object event) {
				return null;
			}

			@Override
			public Calendar getStart(Object event) {
				if (event instanceof IEpisode) {
					IEpisode episode = (IEpisode) event;
					return episode.getStart() != null ? episode.getStart()
							.getCalendar() : null;
				}
				return null;
			}

			@Override
			public Calendar getEnd(Object event) {
				if (event instanceof IEpisode) {
					IEpisode episode = (IEpisode) event;
					return episode.getEnd() != null ? episode.getEnd()
							.getCalendar() : null;
				}
				return null;
			}

			@Override
			public RGB[] getColors(Object event) {
				List<RGB> colors = new ArrayList<RGB>();
				if (event instanceof IEpisode) {
					IEpisode episode = (IEpisode) event;
					try {
						for (ICode code : this.codeService.getCodes(episode
								.getUri())) {
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
