package de.fu_berlin.imp.seqan.usability_analyzer.timeline.ui.views;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
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
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.bkahlert.devel.nebula.utils.ExecutorUtil;
import com.bkahlert.devel.nebula.viewer.timeline.impl.MinimalTimelineGroupViewer;
import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineLabelProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.complex.IBandGroupProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.complex.ITimelineProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.complex.ITimelineProviderFactory;
import com.bkahlert.devel.nebula.viewer.timeline.provider.complex.impl.BandGroupProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.complex.impl.TimelineProvider;
import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;
import com.bkahlert.devel.nebula.widgets.timeline.ITimelineFactory;
import com.bkahlert.devel.nebula.widgets.timeline.TimelineGroup;
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

	private static class TimelineState {
		private Calendar centerVisibleDate;
		private int zoomIndex;

		public TimelineState(Calendar centerVisibleDate, int zoomIndex) {
			Assert.isLegal(centerVisibleDate != null);
			this.centerVisibleDate = centerVisibleDate;
			this.zoomIndex = zoomIndex;
		}
	}

	private Job timelineLoader = null;

	private IWorkSessionService workSessionService;
	private IWorkSessionListener workSessionListener = new IWorkSessionListener() {

		private Set<IIdentifier> filterValidIdentifiers(
				Set<IIdentifier> identifiers) {
			Set<IIdentifier> filteredKeys = new HashSet<IIdentifier>();
			identifierLoop: for (IIdentifier key : identifiers) {
				for (ITimelineBandProvider<HighlightableTimelineGroupViewer<TimelineGroup<InformationPresentingTimeline, IIdentifier>, InformationPresentingTimeline, IIdentifier>, TimelineGroup<InformationPresentingTimeline, IIdentifier>, InformationPresentingTimeline, IIdentifier> timelineBandProvider : Activator
						.<HighlightableTimelineGroupViewer<TimelineGroup<InformationPresentingTimeline, IIdentifier>, InformationPresentingTimeline, IIdentifier>, TimelineGroup<InformationPresentingTimeline, IIdentifier>, InformationPresentingTimeline, IIdentifier> getRegisteredTimelineBandProviders()) {
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
		public void highlight(Object sender, TimeZoneDateRange[] ranges,
				boolean moveInsideViewport) {
			if (TimelineView.this.openedIdentifiers == null
					|| TimelineView.this.openedIdentifiers.size() == 0) {
				return;
			}
			Map<IIdentifier, TimeZoneDateRange[]> groupedRanges = new HashMap<IIdentifier, TimeZoneDateRange[]>();
			for (IIdentifier loadedIdentifier : TimelineView.this.openedIdentifiers) {
				groupedRanges.put(loadedIdentifier, ranges);
			}
			this.highlight(sender, groupedRanges, moveInsideViewport);
		}

		@Override
		public void highlight(Object sender,
				final Map<IIdentifier, TimeZoneDateRange[]> groupedRanges,
				final boolean moveInsideViewport) {
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
			for (IIdentifier key : groupedRanges.keySet()) {
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
						if (moveInsideViewport) {
							TimelineView.this.timelineGroupViewer.highlight(
									groupedRanges, subMonitor.newChild(1));
							TimelineView.this.timelineGroupViewer.center(
									centeredDates, subMonitor.newChild(1));
						} else {
							TimelineView.this.timelineGroupViewer.highlight(
									groupedRanges, subMonitor.newChild(2));
						}
					}
					subMonitor.done();
					return Status.OK_STATUS;
				}
			};
			TimelineView.this.timelineLoader.schedule();
		}
	};

	private TimelineGroup<InformationPresentingTimeline, IIdentifier> timelineGroup;
	private HighlightableTimelineGroupViewer<TimelineGroup<InformationPresentingTimeline, IIdentifier>, InformationPresentingTimeline, IIdentifier> timelineGroupViewer;

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

		this.saveStates();

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

	private void saveStates() {
		if (this.timelineGroup != null && !this.timelineGroup.isDisposed()) {
			final Set<IIdentifier> inputs = this.timelineGroup
					.getTimelineKeys();
			for (final IIdentifier identifier : inputs) {
				try {
					final Future<TimelineState> state = ExecutorUtil
							.asyncExec(new Callable<TimelineState>() {
								@Override
								public TimelineState call() throws Exception {
									ITimeline timeline = TimelineView.this.timelineGroup
											.getTimeline(identifier);
									if (timeline != null) {
										return new TimelineState(timeline
												.getCenterVisibleDate().get(),
												timeline.getZoomIndex().get());
									} else {
										return null;
									}
								}
							});
					ExecutorUtil.nonUIAsyncExec(new Runnable() {
						@Override
						public void run() {
							try {
								if (state != null && state.get() != null) {
									SUATimelinePreferenceUtil util = new SUATimelinePreferenceUtil();
									if (state.get().centerVisibleDate != null) {
										util.setCenterStartDate(identifier,
												state.get().centerVisibleDate);
									}
									util.setZoomIndex(identifier,
											state.get().zoomIndex);
								}
							} catch (Exception e) {
								LOGGER.error("Error saving zoom index", e);
							}
						}
					});
				} catch (Exception e) {
					LOGGER.error("Error saving zoom index", e);
				}
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

	private static class TimelineProviderFactory
			implements
			ITimelineProviderFactory<MinimalTimelineGroupViewer<TimelineGroup<InformationPresentingTimeline, IIdentifier>, InformationPresentingTimeline, IIdentifier>, TimelineGroup<InformationPresentingTimeline, IIdentifier>, InformationPresentingTimeline, IIdentifier> {
		@Override
		public ITimelineProvider<MinimalTimelineGroupViewer<TimelineGroup<InformationPresentingTimeline, IIdentifier>, InformationPresentingTimeline, IIdentifier>, TimelineGroup<InformationPresentingTimeline, IIdentifier>, InformationPresentingTimeline, IIdentifier> createTimelineProvider() {
			ITimelineProvider<MinimalTimelineGroupViewer<TimelineGroup<InformationPresentingTimeline, IIdentifier>, InformationPresentingTimeline, IIdentifier>, TimelineGroup<InformationPresentingTimeline, IIdentifier>, InformationPresentingTimeline, IIdentifier> timelineProvider;
			ITimelineLabelProvider<InformationPresentingTimeline> timelineLabelProvider = new TimelineLabelProvider<InformationPresentingTimeline>();
			List<IBandGroupProvider<MinimalTimelineGroupViewer<TimelineGroup<InformationPresentingTimeline, IIdentifier>, InformationPresentingTimeline, IIdentifier>, TimelineGroup<InformationPresentingTimeline, IIdentifier>, InformationPresentingTimeline, IIdentifier>> bandGroupProviders = new ArrayList<IBandGroupProvider<MinimalTimelineGroupViewer<TimelineGroup<InformationPresentingTimeline, IIdentifier>, InformationPresentingTimeline, IIdentifier>, TimelineGroup<InformationPresentingTimeline, IIdentifier>, InformationPresentingTimeline, IIdentifier>>();
			for (ITimelineBandProvider<MinimalTimelineGroupViewer<TimelineGroup<InformationPresentingTimeline, IIdentifier>, InformationPresentingTimeline, IIdentifier>, TimelineGroup<InformationPresentingTimeline, IIdentifier>, InformationPresentingTimeline, IIdentifier> bandProvider : Activator
					.<MinimalTimelineGroupViewer<TimelineGroup<InformationPresentingTimeline, IIdentifier>, InformationPresentingTimeline, IIdentifier>, TimelineGroup<InformationPresentingTimeline, IIdentifier>, InformationPresentingTimeline, IIdentifier> getRegisteredTimelineBandProviders()) {
				bandGroupProviders
						.add(new BandGroupProvider<MinimalTimelineGroupViewer<TimelineGroup<InformationPresentingTimeline, IIdentifier>, InformationPresentingTimeline, IIdentifier>, TimelineGroup<InformationPresentingTimeline, IIdentifier>, InformationPresentingTimeline, IIdentifier>(
								bandProvider.getContentProvider(), bandProvider
										.getBandLabelProvider(), bandProvider
										.getEventLabelProvider()));
			}
			timelineProvider = new TimelineProvider<MinimalTimelineGroupViewer<TimelineGroup<InformationPresentingTimeline, IIdentifier>, InformationPresentingTimeline, IIdentifier>, TimelineGroup<InformationPresentingTimeline, IIdentifier>, InformationPresentingTimeline, IIdentifier>(
					timelineLabelProvider, bandGroupProviders);
			return timelineProvider;
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());

		ITimelineFactory<InformationPresentingTimeline> timelineFactory = new ITimelineFactory<InformationPresentingTimeline>() {
			@Override
			public InformationPresentingTimeline createTimeline(
					Composite parent, int style) {
				return new InformationPresentingTimeline(parent, style);
			}
		};

		TimelineProviderFactory timelineProviderFactory = new TimelineProviderFactory();
		this.timelineGroup = new TimelineGroup<InformationPresentingTimeline, IIdentifier>(
				parent, SWT.NONE, timelineFactory);
		this.timelineGroup.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				TimelineView.this.saveStates();
			}
		});

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

		this.timelineGroupViewer = new HighlightableTimelineGroupViewer<TimelineGroup<InformationPresentingTimeline, IIdentifier>, InformationPresentingTimeline, IIdentifier>(
				this.timelineGroup, timelineProviderFactory);
		this.getSite().registerContextMenu(menuManager,
				this.timelineGroupViewer);
		this.getSite().setSelectionProvider(this.timelineGroupViewer);
	}

	@Override
	public void setFocus() {
		this.timelineGroup.setFocus();
	}

	/**
	 * Returns the {@link InformationPresentingTimeline} currently displaying
	 * content associated with the given {@link IIdentifier}.
	 * 
	 * @param key
	 * @return
	 * 
	 * @thread.ui must be run in the UI thread
	 */
	public InformationPresentingTimeline getTimeline(IIdentifier key) {
		if (this.timelineGroup != null && !this.timelineGroup.isDisposed()) {
			return this.timelineGroup.getTimeline(key);
		}
		return null;
	}

}
