package de.fu_berlin.imp.seqan.usability_analyzer.timeline.ui.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.bkahlert.devel.nebula.utils.ExecutorUtil;
import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineLabelProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.complex.IBandGroupProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.complex.ITimelineProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.complex.ITimelineProviderFactory;
import com.bkahlert.devel.nebula.viewer.timeline.provider.complex.impl.BandGroupProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.complex.impl.TimelineProvider;
import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;
import com.bkahlert.devel.nebula.widgets.timeline.ITimelineFactory;
import com.bkahlert.devel.nebula.widgets.timelineGroup.impl.TimelineGroup;
import com.bkahlert.devel.rcp.selectionUtils.ArrayUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IHighlightService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IHighlightServiceListener;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IWorkSession;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IWorkSessionListener;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IWorkSessionService;
import de.fu_berlin.imp.seqan.usability_analyzer.timeline.Activator;
import de.fu_berlin.imp.seqan.usability_analyzer.timeline.extensionProviders.ITimelineBandProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.timeline.preferences.SUATimelinePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.timeline.ui.viewer.HighlightableTimelineGroupViewer;
import de.fu_berlin.imp.seqan.usability_analyzer.timeline.ui.widgets.InformationPresentingTimeline;
import de.fu_berlin.imp.seqan.usability_analyzer.timeline.ui.widgets.TimelineLabelProvider;

public class TimelineView extends ViewPart {

	public static final String ID = "de.fu_berlin.imp.seqan.usability_analyzer.timeline.ui.views.TimelineView";
	public static final Logger LOGGER = Logger.getLogger(TimelineView.class);

	private Job timelineLoader = null;

	private IWorkSessionService workSessionService;
	private IWorkSessionListener workSessionListener = new IWorkSessionListener() {

		private Set<IIdentifier> filterValidIdentifiers(
				Set<IIdentifier> identifiers) {
			Set<IIdentifier> filteredKeys = new HashSet<IIdentifier>();
			identifierLoop: for (IIdentifier key : identifiers) {
				for (ITimelineBandProvider timelineBandProvider : Activator
						.getRegisteredTimelineBandProviders()) {
					if (!timelineBandProvider.getContentProvider().isValid(key)) {
						continue identifierLoop;
					}
				}
				filteredKeys.add(key);
			}
			return filteredKeys;
		}

		@Override
		public void workSessionStarted(IWorkSession workSession) {
			final Set<IIdentifier> keys = new HashSet<IIdentifier>();
			keys.addAll(ArrayUtils.getAdaptableObjects(workSession
					.getEntities().toArray(), IIdentifier.class));
			final Set<IIdentifier> filteredKeys = this
					.filterValidIdentifiers(keys);
			TimelineView.this.open(filteredKeys);
			ExecutorUtil.asyncExec(new Runnable() {
				@Override
				public void run() {
					TimelineView.this.setPartName(StringUtils.join(
							filteredKeys, ", "));
				}
			});
		}
	};

	private IHighlightService highlightService;
	private IHighlightServiceListener highlightServiceListener = new IHighlightServiceListener() {
		@Override
		public void highlight(Object sender, TimeZoneDateRange[] ranges) {
			if (TimelineView.this.openedIdentifiers == null
					|| TimelineView.this.openedIdentifiers.size() == 0) {
				return;
			}
			Map<IIdentifier, TimeZoneDateRange[]> groupedRanges = new HashMap<IIdentifier, TimeZoneDateRange[]>();
			for (IIdentifier loadedIdentifier : TimelineView.this.openedIdentifiers) {
				groupedRanges.put(loadedIdentifier, ranges);
			}
			this.highlight(sender, groupedRanges);
		}

		@Override
		public void highlight(Object sender,
				final Map<IIdentifier, TimeZoneDateRange[]> groupedRanges) {
			if (sender == TimelineView.this) {
				return;
			}

			final Map<Object, TimeZoneDate> centeredDates = new HashMap<Object, TimeZoneDate>();
			// if (part == TimelineView.this) {
			// if (TimelineView.this.openedIdentifiers == null) {
			// return;
			// }
			//
			// // TODO selection nur auf jeweiliger timeline anwenden (und
			// // nicht auf alle)
			// List<HasDateRange> ranges = SelectionRetrieverFactory
			// .getSelectionRetriever(HasDateRange.class)
			// .getSelection();
			// for (Object key : TimelineView.this.openedIdentifiers) {
			// List<TimeZoneDateRange> dateRanges = new
			// LinkedList<TimeZoneDateRange>();
			// for (HasDateRange range : ranges) {
			// if (range.getClass().getSimpleName().toLowerCase()
			// .contains("doclog")) {
			// return;
			// }
			// dateRanges.add(range.getDateRange());
			// }
			// groupedDateRanges.put(key, dateRanges);
			// }
			// } else {
			for (Object key : groupedRanges.keySet()) {
				TimeZoneDate centeredDate = TimeZoneDateRange
						.calculateOuterDateRange(groupedRanges.get(key))
						.getStartDate();
				centeredDates.put(key, centeredDate);
			}
			// }

			if (groupedRanges.keySet().size() == 0) {
				return;
			}

			if (TimelineView.this.timelineLoader != null) {
				TimelineView.this.timelineLoader.cancel();
			}

			TimelineView.this.timelineLoader = new Job("Updating "
					+ ITimeline.class.getSimpleName()) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					SubMonitor subMonitor = SubMonitor.convert(monitor, 2);
					if (!groupedRanges.isEmpty()) {
						TimelineView.this.timelineGroupViewer.highlight(
								groupedRanges, subMonitor.newChild(2));
						TimelineView.this.timelineGroupViewer.center(
								centeredDates, subMonitor.newChild(1));
					}
					subMonitor.done();
					return Status.OK_STATUS;
				}
			};
			TimelineView.this.timelineLoader.schedule();
		}
	};

	private TimelineGroup<ITimeline> timelineGroup;
	private HighlightableTimelineGroupViewer<TimelineGroup<ITimeline>, ITimeline> timelineGroupViewer;

	private Set<IIdentifier> openedIdentifiers = null;

	public TimelineView() {
		this.workSessionService = (IWorkSessionService) PlatformUI
				.getWorkbench().getService(IWorkSessionService.class);
		if (this.workSessionService == null) {
			LOGGER.warn("Could not get "
					+ IWorkSessionService.class.getSimpleName());
		}

		this.highlightService = (IHighlightService) PlatformUI.getWorkbench()
				.getService(IHighlightService.class);
		if (this.highlightService == null) {
			LOGGER.warn("Could not get "
					+ IHighlightService.class.getSimpleName());
		}
	}

	/**
	 * Initializes and opens {@link ITimeline}s using an {@link TimelineGroup}.
	 * <p>
	 * Existing {@link DecoratableTimeline}s are recycled. New
	 * {@link DecoratableTimeline} s will be created if necessary. If free
	 * {@link DecoratableTimeline}s stay unused they will be disposed.
	 * 
	 * @param openedIdentifiers
	 */
	public void open(final Set<IIdentifier> identifiers) {
		if (this.timelineLoader != null) {
			this.timelineLoader.cancel();
		}

		this.openedIdentifiers = identifiers;

		this.saveZoomIndex();

		this.timelineLoader = new Job("Loading "
				+ ITimeline.class.getSimpleName()) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				TimelineView.this.timelineGroupViewer.setInput(identifiers);
				TimelineView.this.timelineGroupViewer.refresh(monitor);
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		this.timelineLoader.schedule();
	}

	private void saveZoomIndex() {
		Control control = this.timelineGroupViewer.getControl();
		if (control instanceof TimelineGroup && !control.isDisposed()) {
			@SuppressWarnings("unchecked")
			final TimelineGroup<ITimeline> timelineGroup = (TimelineGroup<ITimeline>) this.timelineGroupViewer
					.getControl();
			final Set<Object> keys = timelineGroup.getTimelineKeys();
			if (keys.size() > 0) {
				ExecutorUtil.syncExec(new Runnable() {
					@Override
					public void run() {
						ITimeline timeline = timelineGroup
								.getTimeline(new ArrayList<Object>(keys).get(0));
						if (timeline != null) {
							Integer zoomIndex = timeline.getZoomIndex();
							if (zoomIndex != null) {
								new SUATimelinePreferenceUtil()
										.setZoomIndex(zoomIndex);
							}
						}
					}
				});
			}
		}
	}

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		if (this.workSessionService != null) {
			this.workSessionService
					.addWorkSessionListener(this.workSessionListener);
		}
		if (this.highlightService != null) {
			this.highlightService
					.addHighlightServiceListener(this.highlightServiceListener);
		}
	}

	@Override
	public void dispose() {
		this.saveZoomIndex();
		if (this.highlightService != null) {
			this.highlightService
					.removeHighlightServiceListener(this.highlightServiceListener);
		}
		if (this.workSessionService != null) {
			this.workSessionService
					.removeWorkSessionListener(this.workSessionListener);
		}
		super.dispose();
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());

		ITimelineFactory<ITimeline> timelineFactory = new ITimelineFactory<ITimeline>() {
			@Override
			public ITimeline createTimeline(Composite parent, int style) {
				return new InformationPresentingTimeline(parent, style);
			}
		};

		ITimelineProviderFactory<ITimeline> timelineProviderFactory = new ITimelineProviderFactory<ITimeline>() {
			@Override
			public ITimelineProvider<ITimeline> createTimelineProvider() {
				ITimelineProvider<ITimeline> timelineProvider;
				ITimelineLabelProvider<ITimeline> timelineLabelProvider = new TimelineLabelProvider<ITimeline>(
						new SUATimelinePreferenceUtil().getZoomIndex());
				List<IBandGroupProvider> bandGroupProviders = new ArrayList<IBandGroupProvider>();
				for (ITimelineBandProvider bandProvider : Activator
						.getRegisteredTimelineBandProviders()) {
					bandGroupProviders.add(new BandGroupProvider(bandProvider
							.getContentProvider(), bandProvider
							.getBandLabelProvider(), bandProvider
							.getEventLabelProvider()));
				}
				timelineProvider = new TimelineProvider<ITimeline>(
						timelineLabelProvider, bandGroupProviders);
				return timelineProvider;
			}
		};
		this.timelineGroup = new TimelineGroup<ITimeline>(parent, SWT.NONE,
				timelineFactory);

		MenuManager menuManager = new MenuManager("#PopupMenu");
		menuManager.setRemoveAllWhenShown(true);
		menuManager.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				manager.add(new Separator(
						IWorkbenchActionConstants.MB_ADDITIONS));
			}
		});
		Menu menu = menuManager.createContextMenu(this.timelineGroup);
		this.timelineGroup.setMenu(menu);

		this.timelineGroupViewer = new HighlightableTimelineGroupViewer<TimelineGroup<ITimeline>, ITimeline>(
				this.timelineGroup, timelineProviderFactory);
		this.getSite().registerContextMenu(menuManager,
				this.timelineGroupViewer);
		this.getSite().setSelectionProvider(this.timelineGroupViewer);
	}

	@Override
	public void setFocus() {
		this.timelineGroup.setFocus();
	}

}
