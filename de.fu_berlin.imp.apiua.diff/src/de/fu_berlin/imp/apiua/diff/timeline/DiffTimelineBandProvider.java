package de.fu_berlin.imp.apiua.diff.timeline;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
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

import de.fu_berlin.imp.apiua.core.model.IOpenable;
import de.fu_berlin.imp.apiua.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.apiua.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.apiua.diff.Activator;
import de.fu_berlin.imp.apiua.diff.model.IDiff;
import de.fu_berlin.imp.apiua.diff.model.IDiffRecord;
import de.fu_berlin.imp.apiua.diff.model.IDiffs;
import de.fu_berlin.imp.apiua.diff.model.impl.Diff;
import de.fu_berlin.imp.apiua.diff.model.impl.DiffRecord;
import de.fu_berlin.imp.apiua.diff.ui.DiffLabelProvider;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.timeline.extensionProviders.ITimelineBandProvider;

public class DiffTimelineBandProvider implements
		ITimelineBandProvider<IIdentifier> {

	private static final Logger LOGGER = Logger
			.getLogger(DiffTimelineBandProvider.class);

	private enum BANDS {
		DIFF_BAND, DIFFRECORD_BAND
	}

	@Override
	public ITimelineContentProvider<IIdentifier> getContentProvider() {
		return new ITimelineContentProvider<IIdentifier>() {

			private final ITimelineListener timelineListener = new TimelineAdapter() {
				@Override
				public void clicked(TimelineEvent event) {
					if (!(event.getSource() instanceof IOpenable)) {
						return;
					}
					IOpenable openable = (IOpenable) event.getSource();
					openable.open();
				}
			};

			private IIdentifier input = null;
			private BaseTimelineGroup<? extends IBaseTimeline, IIdentifier> timelineGroup = null;

			// private final DiffContentProvider diffContentProvider = new
			// DiffContentProvider();

			@Override
			public <TIMELINE extends IBaseTimeline> void inputChanged(
					ITimelineGroupViewer<TIMELINE, IIdentifier> viewer,
					IIdentifier oldInput, IIdentifier newInput) {
				this.input = newInput;

				// try {
				// URI oldInputUri = oldInput != null ? new URIXX("apiua://"
				// + DiffLocatorProvider.DIFF_NAMESPACE + "/"
				// + oldInput) : null;
				// URI newInputUri = newInput != null ? new URIXX("apiua://"
				// + DiffLocatorProvider.DIFF_NAMESPACE + "/"
				// + newInput) : null;
				// this.diffContentProvider.inputChanged(viewer, oldInputUri,
				// newInputUri);

				if (this.timelineGroup instanceof TimelineGroup) {
					((TimelineGroup<?, ?>) this.timelineGroup)
							.removeTimelineListener(this.timelineListener);
				}
				if (viewer != null && viewer.getControl() != null) {
					this.timelineGroup = viewer.getControl();
					if (this.timelineGroup instanceof TimelineGroup) {
						((TimelineGroup<?, ?>) this.timelineGroup)
								.addTimelineListener(this.timelineListener);
					}
				}
				// } catch (URISyntaxException e) {
				// throw new RuntimeException(e);
				// }
			}

			@Override
			public boolean isValid(IIdentifier key) {
				return Activator.getDefault().getDiffDataContainer().getIDs()
						.contains(key);
			}

			@Override
			public Object[] getBands(IProgressMonitor monitor) {
				return new Object[] { BANDS.DIFF_BAND, BANDS.DIFFRECORD_BAND };
			}

			@Override
			public Object[] getEvents(Object band, IProgressMonitor monitor) {
				SubMonitor subMonitor = SubMonitor.convert(monitor, 2);
				if (!(band instanceof BANDS)) {
					subMonitor.done();
					return new Object[0];
				}

				final IDiffs diffList = (this.input instanceof IIdentifier && de.fu_berlin.imp.apiua.diff.Activator
						.getDefault().getDiffDataContainer() != null) ? de.fu_berlin.imp.apiua.diff.Activator
						.getDefault().getDiffDataContainer()
						.getDiffFiles(this.input, subMonitor.newChild(1))
						: null;

				if (diffList == null) {
					return new Object[0];
				}

				switch ((BANDS) band) {
				case DIFF_BAND:
					IDiff[] diffs = diffList.toArray();
					monitor.worked(1);
					return diffs;
				case DIFFRECORD_BAND:
					List<IDiffRecord> diffRecords = new ArrayList<IDiffRecord>();
					for (IDiff diff : diffList) {
						for (IDiffRecord diffRecord : diff.getDiffFileRecords()) {
							diffRecords.add(diffRecord);
						}
					}
					monitor.worked(1);
					return diffRecords.toArray();
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
					case DIFF_BAND:
						return "Development";
					case DIFFRECORD_BAND:
						return "Sources";
					}
				}
				return null;
			}

			@Override
			public Boolean isShowInOverviewBands(Object band) {
				if (band instanceof BANDS) {
					switch ((BANDS) band) {
					case DIFF_BAND:
						return true;
					case DIFFRECORD_BAND:
						return true;
					}
				}
				return null;
			}

			@Override
			public Float getRatio(Object band) {
				if (band instanceof BANDS) {
					switch ((BANDS) band) {
					case DIFF_BAND:
						return 0.15f;
					case DIFFRECORD_BAND:
						return null;
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
			private final DiffLabelProvider diffLabelProvider = new DiffLabelProvider();

			@Override
			public String getTitle(Object event) {
				String title = this.diffLabelProvider.getText(event);
				if (event instanceof IDiffRecord) {
					IDiffRecord diffRecord = (IDiffRecord) event;
					title += " @"
							+ diffRecord.getDiffFile().getCalculatedRevision();
				}
				return title;
			}

			@Override
			public String getTooltip(Object event) {
				return null;
			}

			@Override
			public java.net.URI getIcon(Object event) {
				Image image = this.diffLabelProvider.getImage(event);
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
				if (event instanceof IDiff) {
					IDiff diff = (IDiff) event;
					TimeZoneDateRange dateRange = diff.getDateRange();
					return dateRange.getStartDate() != null ? dateRange
							.getStartDate().getCalendar() : null;
				} else if (event instanceof IDiffRecord) {
					DiffRecord diffRecord = (DiffRecord) event;
					TimeZoneDateRange dateRange = diffRecord.getDateRange();
					return dateRange.getStartDate() != null ? dateRange
							.getStartDate().getCalendar() : null;
				}
				return null;
			}

			@Override
			public Calendar getEnd(Object event) {
				if (event instanceof IDiff) {
					IDiff diff = (IDiff) event;
					TimeZoneDateRange dateRange = diff.getDateRange();
					return dateRange.getEndDate() != null ? dateRange
							.getEndDate().getCalendar() : null;
				} else if (event instanceof IDiffRecord) {
					DiffRecord diffRecord = (DiffRecord) event;
					TimeZoneDateRange dateRange = diffRecord.getDateRange();
					return dateRange.getEndDate() != null ? dateRange
							.getEndDate().getCalendar() : null;
				}
				return null;
			}

			@Override
			public RGB[] getColors(Object event) {
				List<RGB> colors = new ArrayList<RGB>();
				if (event instanceof IDiff) {
					IDiff diff = (IDiff) event;
					try {
						for (ICode code : this.codeService.getCodes(diff
								.getUri())) {
							colors.add(code.getColor());
						}
					} catch (CodeServiceException e) {
						LOGGER.error(e);
					}
				}
				if (event instanceof IDiffRecord) {
					DiffRecord diffRecord = (DiffRecord) event;
					try {
						for (ICode code : this.codeService.getCodes(diffRecord
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
				return false;
			}

			@Override
			public String[] getClassNames(Object event) {
				if (event instanceof IDiff) {
					return new String[] { Diff.class.getSimpleName() };
				} else if (event instanceof IDiffRecord) {
					return new String[] { DiffRecord.class.getSimpleName() };
				}
				return new String[0];
			}
		};
	}
}
