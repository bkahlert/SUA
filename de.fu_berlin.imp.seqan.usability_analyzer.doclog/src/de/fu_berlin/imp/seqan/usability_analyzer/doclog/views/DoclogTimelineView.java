package de.fu_berlin.imp.seqan.usability_analyzer.doclog.views;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.bkahlert.devel.nebula.widgets.timeline.Timeline;
import com.bkahlert.devel.rcp.selectionUtils.ArrayUtils;
import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;
import com.bkahlert.devel.rcp.selectionUtils.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.FingerprintDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.IdDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IWorkSession;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IWorkSessionListener;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IWorkSessionService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.ExecutorUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.Activator;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.Doclog;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.ui.widgets.DoclogTimeline;

public class DoclogTimelineView extends ViewPart {

	public static final String ID = "de.fu_berlin.imp.seqan.usability_analyzer.doclog.views.DoclogTimelineView";
	private static final Logger LOGGER = Logger
			.getLogger(DoclogTimelineView.class);

	private static Set<Object> filterValidKeys(List<?> keys) {
		HashSet<Object> validKeys = new HashSet<Object>();
		for (Object key : keys) {
			if (Activator.getDefault().getDoclogDataDirectory()
					.getDateRange(key) != null)
				validKeys.add(key);
		}
		return validKeys;
	}

	Job timelineLoader = null;

	private ISelectionListener selectionListener = new ISelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
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
					+ Timeline.class.getSimpleName()) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					if (groupedDateRanges.size() > 0) {
						refresh(groupedDateRanges, monitor);
					}
					return Status.OK_STATUS;
				}
			};
			timelineLoader.schedule();
		}
	};

	private IWorkSessionService workSessionService;
	private IWorkSessionListener workSessionListener = new IWorkSessionListener() {
		@Override
		public void IWorkSessionStarted(IWorkSession workSession) {
			final Set<Object> keys = new HashSet<Object>();
			keys.addAll(filterValidKeys(ArrayUtils.getAdaptableObjects(
					workSession.getEntities().toArray(), ID.class)));
			keys.addAll(filterValidKeys(ArrayUtils.getAdaptableObjects(
					workSession.getEntities().toArray(), Fingerprint.class)));
			open(keys, null);
		}
	};

	private Composite composite;

	public DoclogTimelineView() {
		this.workSessionService = (IWorkSessionService) PlatformUI
				.getWorkbench().getService(IWorkSessionService.class);
		if (this.workSessionService == null)
			LOGGER.warn("Could not get "
					+ IWorkSessionService.class.getSimpleName());
	}

	/**
	 * Initializes and opens {@link DoclogTimeline}s.
	 * <p>
	 * Existing {@link DoclogTimeline}s are recycled. New {@link DoclogTimeline}
	 * s will be created if necessary. If free {@link DoclogTimeline}s stay
	 * unused they will be disposed.
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
		timelineLoader = new Job("Preparing " + Timeline.class.getSimpleName()) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				rs.set(init(keys, monitor, success));
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

	public <T> Future<T> init(Set<Object> keys,
			IProgressMonitor progressMonitor, final Callable<T> success) {
		progressMonitor.beginTask("Preparing " + Timeline.class.getSimpleName()
				+ "s", keys.size() * 3 + 2);
		if (progressMonitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		Object[] unpreparedKeys = prepareExistingTimelines(keys);
		if (progressMonitor.isCanceled()) {
			throw new OperationCanceledException();
		}

		progressMonitor.worked(2);

		for (final Object key : unpreparedKeys) {
			if (progressMonitor.isCanceled()) {
				throw new OperationCanceledException();
			}

			Doclog doclog = Activator
					.getDefault()
					.getDoclogDataDirectory()
					.getDoclogFile(key,
							new SubProgressMonitor(progressMonitor, 1));

			if (doclog == null) {
				LOGGER.error(Doclog.class.getSimpleName() + " for " + key
						+ " was null. Only valid (= "
						+ Doclog.class.getSimpleName()
						+ " exists) are allowed.");
				continue;
			}

			progressMonitor.worked(1);

			final AtomicReference<DoclogTimeline> doclogTimeline = new AtomicReference<DoclogTimeline>(
					getTimeline(key));
			final AtomicReference<Object> doclogTimelineKey = new AtomicReference<Object>();
			if (doclogTimeline.get() != null) {
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						doclogTimelineKey.set(doclogTimeline.get().getData());
					}
				});
				progressMonitor.worked(1);
			} else {
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						doclogTimeline.set(new DoclogTimeline(composite,
								SWT.NONE));
						doclogTimeline.get().setData(key);
						doclogTimelineKey.set(key);
					}
				});
				progressMonitor.worked(1);
			}
			if (key.equals(doclogTimelineKey)) {
				progressMonitor.worked(1);
			} else {
				doclogTimeline.get().show(doclog, getTitle(key));
				progressMonitor.worked(1);
			}
			progressMonitor.done();

			if (progressMonitor.isCanceled()) {
				disposeTimelines(key);
				throw new OperationCanceledException();
			}
		}

		Future<T> rs = ExecutorUtil.asyncExec(new Callable<T>() {
			@Override
			public T call() throws Exception {
				composite.layout();
				if (success != null)
					return success.call();
				return null;
			}
		});

		progressMonitor.done();

		return rs;
	}

	private static String getTitle(Object key) {
		String title;
		if (key instanceof ID) {
			title = "ID: " + key.toString();
		} else if (key instanceof Fingerprint) {
			title = "Fingerprint: " + key.toString();
		} else {
			title = "INVALID TYPE";
		}
		return title;
	}

	public void refresh(
			final Map<Object, List<TimeZoneDateRange>> groupedDateRanges,
			IProgressMonitor progressMonitor) {
		progressMonitor.beginTask("Updading " + Timeline.class.getSimpleName()
				+ "s", groupedDateRanges.keySet().size());
		for (Object key : groupedDateRanges.keySet()) {
			if (progressMonitor.isCanceled())
				throw new OperationCanceledException();
			DoclogTimeline timeline = getTimeline(key);
			if (timeline == null) {
				LOGGER.warn(DoclogTimeline.class.getSimpleName()
						+ " does not exist anymore for " + key);
				continue;
			}

			final List<TimeZoneDateRange> dateRanges = groupedDateRanges
					.get(key);
			final TimeZoneDateRange minMaxDateRange = calculateIntersectedDateRange(
					Activator.getDefault().getDoclogDataDirectory()
							.getDoclogFile(key, progressMonitor), dateRanges);
			final DoclogTimeline timeline_ = timeline;
			if (progressMonitor.isCanceled())
				throw new OperationCanceledException();
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					timeline_.center(minMaxDateRange);
					timeline_.highlight(dateRanges);
				}
			});
			if (progressMonitor.isCanceled())
				throw new OperationCanceledException();
		}

		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				setPartName("Doclogs - "
						+ StringUtils.join(groupedDateRanges.keySet(), ", "));
				composite.layout();
			}
		});

		progressMonitor.done();
	}

	/**
	 * Given a {@link List} of {@link TimeZoneDateRange}s and a
	 * {@link Doclog} this method returns the earliest and latest dates that
	 * in which {@link DoclogRecord} events occurred.
	 * 
	 * @param doclog
	 * @param dateRanges
	 * @return
	 */
	private TimeZoneDateRange calculateIntersectedDateRange(
			Doclog doclog, List<TimeZoneDateRange> dateRanges) {
		TimeZoneDate earliestDate = null;
		TimeZoneDate latestDate = null;
		for (DoclogRecord doclogRecord : doclog.getDoclogRecords()) {
			boolean intersects = false;
			for (TimeZoneDateRange dateRange : dateRanges) {
				if (dateRange.isIntersected(doclogRecord.getDateRange())) {
					intersects = true;
					break;
				}
			}

			if (intersects) {
				if (earliestDate == null
						|| earliestDate.after(doclogRecord.getDateRange()
								.getStartDate())) {
					earliestDate = doclogRecord.getDateRange().getStartDate();
				}
				if (latestDate == null
						|| latestDate.before(doclogRecord.getDateRange()
								.getEndDate())) {
					latestDate = doclogRecord.getDateRange().getEndDate();
				}
			}
		}
		return new TimeZoneDateRange(earliestDate, latestDate);
	}

	private Set<Object> getTimelineKeys() {
		final Set<Object> keys = new HashSet<Object>();
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				for (Control control : composite.getChildren()) {
					if (!control.isDisposed()
							&& control instanceof DoclogTimeline) {
						keys.add(control.getData());
					}
				}
			}
		});
		return keys;
	}

	public DoclogTimeline getTimeline(final Object key) {
		Assert.isNotNull(key);
		final AtomicReference<DoclogTimeline> timelineReference = new AtomicReference<DoclogTimeline>();
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				for (Control control : composite.getChildren()) {
					if (!control.isDisposed()
							&& control instanceof DoclogTimeline) {
						DoclogTimeline timeline = (DoclogTimeline) control;
						if (key.equals(timeline.getData())) {
							timelineReference.set(timeline);
							return;
						}
					}
				}
			}
		});
		return timelineReference.get();
	}

	/**
	 * Prepares already instantiated {@link DoclogTimeline}s in the following
	 * way:
	 * <ol>
	 * <li>{@link DoclogTimeline}s already associated with a given key stay
	 * untouched
	 * <li>the other {@link DoclogTimeline} are associated with a new key
	 * <li>{@link DoclogTimeline}s that are not needed anymore become disposed
	 * <li>all newly assigned keys and unassigned keys are returned
	 * </ol>
	 * 
	 * @param usedTimelineKeys
	 * @return keys that were not associated to an existing
	 *         {@link DoclogTimeline} or were associated with a
	 *         {@link DoclogTimeline} that before was responsible for another
	 *         key
	 */
	private Object[] prepareExistingTimelines(Set<Object> usedTimelineKeys_) {
		List<Object> usedTimelineKeys = new LinkedList<Object>(
				usedTimelineKeys_);
		List<Object> existingTimelines = new LinkedList<Object>(
				getTimelineKeys());
		List<?> preparedTimelines = ListUtils.intersection(existingTimelines,
				usedTimelineKeys);
		final List<?> unpreparedTimelines = ListUtils.subtract(
				usedTimelineKeys, preparedTimelines);
		final List<?> freeTimelines = ListUtils.subtract(existingTimelines,
				preparedTimelines);
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				int i = 0;
				while (freeTimelines.size() > 0
						&& unpreparedTimelines.size() > i) {
					DoclogTimeline timeline = getTimeline(freeTimelines
							.remove(0));
					timeline.setData(unpreparedTimelines.get(i));
				}
			}
		});
		disposeTimelines(freeTimelines.toArray());
		return unpreparedTimelines.toArray();
	}

	private void disposeTimelines(final Object... timelineKeys) {
		for (final Object key : timelineKeys) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					Timeline doclogTimeline = getTimeline(key);
					if (doclogTimeline != null && !doclogTimeline.isDisposed())
						doclogTimeline.dispose();
				}
			});
		}
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				composite.layout();
			}
		});
	}

	protected void clear() {
		for (Control control : composite.getChildren()) {
			if (!control.isDisposed())
				control.dispose();
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout(SWT.VERTICAL));
		this.composite = parent;

		// TODO getSite().setSelectionProvider(null);
	}

	@Override
	public void setFocus() {
	}

}
