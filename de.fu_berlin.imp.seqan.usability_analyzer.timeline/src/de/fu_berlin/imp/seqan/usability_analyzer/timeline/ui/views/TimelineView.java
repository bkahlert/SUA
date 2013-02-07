package de.fu_berlin.imp.seqan.usability_analyzer.timeline.ui.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineLabelProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.complex.IBandGroupProviders;
import com.bkahlert.devel.nebula.viewer.timeline.provider.complex.ITimelineProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.complex.ITimelineProviderFactory;
import com.bkahlert.devel.nebula.viewer.timeline.provider.complex.impl.BandGroupProviders;
import com.bkahlert.devel.nebula.viewer.timeline.provider.complex.impl.TimelineProvider;
import com.bkahlert.devel.nebula.viewer.timelineGroup.ITimelineGroupViewer;
import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;
import com.bkahlert.devel.nebula.widgets.timeline.ITimelineFactory;
import com.bkahlert.devel.rcp.selectionUtils.ArrayUtils;
import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;
import com.bkahlert.devel.rcp.selectionUtils.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.FingerprintDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.IdDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IWorkSession;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IWorkSessionListener;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IWorkSessionService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.ExecutorUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.timeline.Activator;
import de.fu_berlin.imp.seqan.usability_analyzer.timeline.extensionProviders.ITimelineBandProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.timeline.ui.viewer.IncompleteTimelineGroupViewer;
import de.fu_berlin.imp.seqan.usability_analyzer.timeline.ui.widgets.Timeline;
import de.fu_berlin.imp.seqan.usability_analyzer.timeline.ui.widgets.TimelineGroup;
import de.fu_berlin.imp.seqan.usability_analyzer.timeline.ui.widgets.TimelineLabelProvider;

public class TimelineView extends ViewPart {

	public static final String ID = "de.fu_berlin.imp.seqan.usability_analyzer.timeline.ui.views.TimelineView";
	public static final Logger LOGGER = Logger.getLogger(TimelineView.class);

	private Job timelineLoader = null;

	private ISelectionListener selectionListener = new ISelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (part == TimelineView.this)
				return;

			final Map<Object, List<TimeZoneDateRange>> groupedDateRanges = new HashMap<Object, List<TimeZoneDateRange>>();
			groupedDateRanges.putAll(IdDateRange
					.group(SelectionRetrieverFactory.getSelectionRetriever(
							IdDateRange.class).getSelection()));
			groupedDateRanges.putAll(FingerprintDateRange
					.group(SelectionRetrieverFactory.getSelectionRetriever(
							FingerprintDateRange.class).getSelection()));

			if (groupedDateRanges.keySet().size() == 0)
				return;

			if (timelineLoader != null)
				timelineLoader.cancel();

			timelineLoader = new Job("Preparing "
					+ ITimeline.class.getSimpleName()) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					if (groupedDateRanges.size() > 0) {
						timelineGroup.refresh(groupedDateRanges, monitor);
					}
					setPartName(StringUtils.join(
							timelineGroup.getTimelineKeys(), ", "));
					return Status.OK_STATUS;
				}
			};
			timelineLoader.schedule();
		}
	};

	private IWorkSessionService workSessionService;
	private IWorkSessionListener workSessionListener = new IWorkSessionListener() {

		private Set<Object> filterValidKeys(Set<Object> keys) {
			Set<Object> filteredKeys = new HashSet<Object>();
			keyLoop: for (Object key : keys) {
				for (ITimelineBandProvider timelineBandProvider : Activator
						.getRegisteredTimelineBandProviders()) {
					if (!timelineBandProvider.getContentProvider().isValid(key))
						continue keyLoop;
				}
				filteredKeys.add(key);
			}
			return filteredKeys;
		}

		@Override
		public void workSessionStarted(IWorkSession workSession) {
			final Set<Object> keys = new HashSet<Object>();
			keys.addAll(ArrayUtils.getAdaptableObjects(workSession
					.getEntities().toArray(), ID.class));
			keys.addAll(ArrayUtils.getAdaptableObjects(workSession
					.getEntities().toArray(), Fingerprint.class));
			final Set<Object> filteredKeys = filterValidKeys(keys);
			open(filteredKeys, new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					setPartName(StringUtils.join(filteredKeys, ", "));
					return null;
				}
			});
		}
	};

	private TimelineGroup<Timeline> timelineGroup;

	public TimelineView() {
		this.workSessionService = (IWorkSessionService) PlatformUI
				.getWorkbench().getService(IWorkSessionService.class);
		if (this.workSessionService == null)
			LOGGER.warn("Could not get "
					+ IWorkSessionService.class.getSimpleName());
	}

	/**
	 * Initializes and opens {@link ITimeline}s using an {@link TimelineGroup}.
	 * <p>
	 * Existing {@link Timeline}s are recycled. New {@link Timeline} s will be
	 * created if necessary. If free {@link Timeline}s stay unused they will be
	 * disposed.
	 * 
	 * @param keys
	 * @param success
	 */
	public <T> Future<T> open(final Set<Object> keys, final Callable<T> success) {
		if (keys.size() == 0) {
			if (success != null) {
				return ExecutorUtil.asyncExec(success);
			} else
				return null;
		}

		if (timelineLoader != null)
			timelineLoader.cancel();

		final AtomicReference<Future<T>> rs = new AtomicReference<Future<T>>();
		final Semaphore mutex = new Semaphore(0);
		timelineLoader = new Job("Loading " + ITimeline.class.getSimpleName()) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				rs.set(timelineGroup.load(keys, monitor, success));
				monitor.done();
				mutex.release();
				return Status.OK_STATUS;
			}
		};
		timelineLoader.schedule();
		try {
			mutex.acquire();
		} catch (InterruptedException e) {
			LOGGER.error(e);
		}
		return rs.get();
	}

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		if (this.workSessionService != null)
			this.workSessionService.addWorkSessionListener(workSessionListener);
		SelectionUtils.getSelectionService().addSelectionListener(
				selectionListener);
	}

	@Override
	public void dispose() {
		SelectionUtils.getSelectionService().removeSelectionListener(
				selectionListener);
		if (this.workSessionService != null)
			this.workSessionService
					.removeWorkSessionListener(workSessionListener);
		super.dispose();
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());

		ITimelineFactory<Timeline> timelineFactory = new ITimelineFactory<Timeline>() {
			@Override
			public Timeline createTimeline(Composite parent, int style) {
				return new Timeline(parent, style);
			}
		};

		ITimelineProviderFactory<Timeline> timelineProviderFactory = new ITimelineProviderFactory<Timeline>() {
			@Override
			public ITimelineProvider<Timeline> createTimelineProvider() {
				ITimelineProvider<Timeline> timelineProvider;
				ITimelineLabelProvider<Timeline> timelineLabelProvider = new TimelineLabelProvider<Timeline>();
				List<IBandGroupProviders> bandGroupProviders = new ArrayList<IBandGroupProviders>();
				for (ITimelineBandProvider bandProvider : Activator
						.getRegisteredTimelineBandProviders()) {
					bandGroupProviders.add(new BandGroupProviders(bandProvider
							.getContentProvider(), bandProvider
							.getBandLabelProvider(), bandProvider
							.getEventLabelProvider()));
				}
				timelineProvider = new TimelineProvider<Timeline>(
						timelineLabelProvider, bandGroupProviders);
				return timelineProvider;
			}
		};
		this.timelineGroup = new TimelineGroup<Timeline>(parent, SWT.NONE,
				timelineFactory, timelineProviderFactory);

		MenuManager menuManager = new MenuManager("#PopupMenu");
		menuManager.setRemoveAllWhenShown(true);
		menuManager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				manager.add(new Separator(
						IWorkbenchActionConstants.MB_ADDITIONS));
			}
		});
		Menu menu = menuManager.createContextMenu(this.timelineGroup);
		this.timelineGroup.setMenu(menu);

		ITimelineGroupViewer timelineGroupViewer = new IncompleteTimelineGroupViewer<TimelineGroup<Timeline>>(
				timelineGroup);
		getSite().registerContextMenu(menuManager, timelineGroupViewer);
		getSite().setSelectionProvider(timelineGroupViewer);
	}

	@Override
	public void setFocus() {
		this.timelineGroup.setFocus();
	}

}
