package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.timeline;

import java.net.URI;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.nebula.colors.RGB;
import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineBandLabelProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineContentProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineEventLabelProvider;
import com.bkahlert.devel.nebula.widgets.timeline.ITimelineListener;
import com.bkahlert.devel.nebula.widgets.timeline.TimelineEvent;
import com.bkahlert.devel.nebula.widgets.timeline.TimelineHelper;
import com.bkahlert.devel.nebula.widgets.timeline.impl.TimelineAdapter;
import com.bkahlert.devel.nebula.widgets.timelineGroup.ITimelineGroup;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.Cache;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.Cache.CacheFetcher;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.GTCodeableProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;
import de.fu_berlin.imp.seqan.usability_analyzer.timeline.extensionProviders.ITimelineBandProvider;

public class AnalysisTimelineBandProvider implements ITimelineBandProvider {

	private static final Logger LOGGER = Logger
			.getLogger(AnalysisTimelineBandProvider.class);

	private enum BANDS {
		CODE_BAND
	}

	@Override
	public ITimelineContentProvider getContentProvider() {
		return new ITimelineContentProvider() {

			private ICodeService codeService = (ICodeService) PlatformUI
					.getWorkbench().getService(ICodeService.class);

			private ITimelineListener timelineListener = new TimelineAdapter() {
				@Override
				public void resized(TimelineEvent event) {
					if (!(event.getSource() instanceof IEpisode))
						return;
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

			private Object input = null;
			private TimelineRefresher timelineRefresher = null;
			private ITimelineGroup<?> timelineGroup = null;

			@Override
			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
				this.input = newInput;
				if (this.timelineGroup != null) {
					this.timelineGroup.removeTimelineListener(timelineListener);
				}
				if (viewer != null
						&& viewer.getControl() instanceof ITimelineGroup) {
					this.timelineGroup = (ITimelineGroup<?>) viewer
							.getControl();
					this.timelineGroup.addTimelineListener(timelineListener);
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
			public boolean isValid(Object key) {
				if (key instanceof ID) {
					return true;
				} else if (key instanceof Fingerprint) {
					return true;
				} else {
					return false;
				}
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
					List<ICodeInstance> instances = codeService
							.getInstances(this.input);
					Set<IEpisode> episodes;
					if (this.input instanceof ID)
						episodes = codeService.getEpisodes((ID) this.input);
					else
						episodes = codeService
								.getEpisodes((Fingerprint) this.input);
					monitor.worked(2);
					return ArrayUtils.addAll(instances.toArray(),
							episodes.toArray());
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

			private Cache<ICodeInstance, ICodeable> cache = new Cache<ICodeInstance, ICodeable>(
					new CacheFetcher<ICodeInstance, ICodeable>() {
						@Override
						public ICodeable fetch(ICodeInstance key,
								IProgressMonitor progressMonitor) {
							if (codeService == null)
								return null;
							return codeService.getCodedObject(key.getId());
						}
					}, 5);

			private ILabelProvider codeableLabelProvider = new GTCodeableProvider()
					.getLabelProvider();

			@Override
			public String getTitle(Object event) {
				if (event instanceof ICodeInstance) {
					ICodeInstance codeInstance = (ICodeInstance) event;
					return codeInstance.getCode().getCaption();
				}
				return codeableLabelProvider.getText(event);
			}

			@Override
			public URI getIcon(Object event) {
				Image image = codeableLabelProvider.getImage(event);
				if (image != null)
					return TimelineHelper.createUriFromImage(image);
				return null;
			}

			@Override
			public URI getImage(Object event) {
				return null;
			}

			@Override
			public Calendar getStart(Object event) {
				if (event instanceof ICodeInstance) {
					ICodeInstance codeInstance = (ICodeInstance) event;
					ICodeable codeable = cache.getPayload(codeInstance, null);
					if (codeable instanceof HasDateRange) {
						TimeZoneDateRange dateRange = ((HasDateRange) codeable)
								.getDateRange();
						return dateRange != null
								&& dateRange.getStartDate() != null ? dateRange
								.getStartDate().getCalendar() : null;
					}
				} else if (event instanceof IEpisode) {
					IEpisode episode = (IEpisode) event;
					return episode.getStart().getCalendar();
				}
				return null;
			}

			@Override
			public Calendar getEnd(Object event) {
				if (event instanceof ICodeInstance) {
					ICodeInstance codeInstance = (ICodeInstance) event;
					ICodeable codeable = cache.getPayload(codeInstance, null);
					if (codeable instanceof HasDateRange) {
						TimeZoneDateRange dateRange = ((HasDateRange) codeable)
								.getDateRange();
						return dateRange.getEndDate() != null ? dateRange
								.getEndDate().getCalendar() : null;
					}
				} else if (event instanceof IEpisode) {
					IEpisode episode = (IEpisode) event;
					return episode.getEnd().getCalendar();
				}
				return null;
			}

			@Override
			public String getColor(Object event) {
				if (event instanceof ICodeInstance) {
					ICodeInstance codeInstance = (ICodeInstance) event;
					ICodeable codeable = cache.getPayload(codeInstance, null);
					if (codeable instanceof IEpisode) {
						IEpisode episode = (IEpisode) codeable;
						return new RGB(episode.getColor()).toHexString();
					}
				} else if (event instanceof IEpisode) {
					IEpisode episode = (IEpisode) event;
					return new RGB(episode.getColor()).toHexString();
				}
				return null;
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
