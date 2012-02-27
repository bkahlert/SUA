package de.fu_berlin.imp.seqan.usability_analyzer.doclog.views;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.collections.ListUtils;
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
import org.eclipse.ui.part.ViewPart;

import com.bkahlert.devel.nebula.widgets.timeline.Timeline;
import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;
import com.bkahlert.devel.rcp.selectionUtils.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.FingerprintDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.IdDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.Activator;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogFile;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.util.DoclogCache;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.widgets.DoclogTimeline;

public class DoclogTimelineView extends ViewPart {

	public static final String ID = "de.fu_berlin.imp.seqan.usability_analyzer.doclog.views.DoclogTimelineView";
	private static final Logger LOGGER = Logger
			.getLogger(DoclogTimelineView.class);

	private static Set<Object> filterValidKeys(List<?> keys) {
		HashSet<Object> validKeys = new HashSet<Object>();
		for (Object key : keys) {
			if (Activator.getDefault().getDoclogDirectory().getDateRange(key) != null)
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

	/**
	 * This {@link ISelectionListener} is responsible for the initialization of
	 * {@link DoclogTimeline}s.
	 * <p>
	 * Existing {@link DoclogTimeline}s are recycled. New {@link DoclogTimeline}
	 * s will be created if necessary. If free {@link DoclogTimeline}s stay
	 * unused they will be disposed.
	 */
	private ISelectionListener postSelectionListener = new ISelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (!part.getClass().equals(DoclogExplorerView.class)
					&& !part.getSite().getId().contains("DiffExplorerView")) {

				final List<Object> keys = new LinkedList<Object>();
				keys.addAll(filterValidKeys(SelectionRetrieverFactory
						.getSelectionRetriever(ID.class).getSelection()));
				keys.addAll(filterValidKeys(SelectionRetrieverFactory
						.getSelectionRetriever(Fingerprint.class)
						.getSelection()));

				if (keys.size() == 0)
					return;

				if (timelineLoader != null)
					timelineLoader.cancel();

				timelineLoader = new Job("Preparing "
						+ Timeline.class.getSimpleName()) {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						init(keys, monitor);
						return Status.OK_STATUS;
					}
				};
				timelineLoader.schedule();
			}
		}
	};

	private Composite composite;

	public DoclogTimelineView() {
	}

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		SelectionUtils.getSelectionService().addSelectionListener(
				selectionListener);
		SelectionUtils.getSelectionService().addPostSelectionListener(
				postSelectionListener);
	}

	@Override
	public void dispose() {
		SelectionUtils.getSelectionService().removePostSelectionListener(
				postSelectionListener);
		SelectionUtils.getSelectionService().removeSelectionListener(
				selectionListener);
		super.dispose();
	}

	public void init(List<Object> keys, IProgressMonitor progressMonitor) {
		progressMonitor.beginTask("Preparing " + Timeline.class.getSimpleName()
				+ "s", keys.size() * 3 + 2);
		if (progressMonitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		prepareExistingTimelines(keys);
		if (progressMonitor.isCanceled()) {
			throw new OperationCanceledException();
		}

		progressMonitor.worked(2);

		for (final Object key : keys) {
			if (progressMonitor.isCanceled()) {
				throw new OperationCanceledException();
			}

			DoclogFile doclogFile = DoclogCache.getInstance().getPayload(key,
					new SubProgressMonitor(progressMonitor, 1));

			if (doclogFile == null) {
				LOGGER.error(DoclogFile.class.getSimpleName() + " for " + key
						+ " was null. Only valid (= "
						+ DoclogFile.class.getSimpleName()
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
				doclogTimeline.get().show(doclogFile, getTitle(key));
				progressMonitor.worked(1);
			}
			progressMonitor.done();

			if (progressMonitor.isCanceled()) {
				disposeTimelines(key);
				throw new OperationCanceledException();
			}
		}

		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				composite.layout();
			}
		});

		progressMonitor.done();
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
					DoclogCache.getInstance().getPayload(key, progressMonitor),
					dateRanges);
			final DoclogTimeline timeline_ = timeline;
			if (progressMonitor.isCanceled())
				throw new OperationCanceledException();
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					if (minMaxDateRange.getStartDate() != null)
						timeline_.setCenterVisibleDate(minMaxDateRange
								.getStartDate().toISO8601());
					else if (minMaxDateRange.getEndDate() != null)
						timeline_.setCenterVisibleDate(minMaxDateRange
								.getEndDate().toISO8601());
					timeline_.highlight(dateRanges);
				}
			});
			if (progressMonitor.isCanceled())
				throw new OperationCanceledException();
		}

		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				composite.layout();
			}
		});

		progressMonitor.done();
	}

	/**
	 * Given a {@link List} of {@link TimeZoneDateRange}s and a
	 * {@link DoclogFile} this method returns the earliest and latest dates that
	 * in which {@link DoclogRecord} events occurred.
	 * 
	 * @param doclogFile
	 * @param dateRanges
	 * @return
	 */
	private TimeZoneDateRange calculateIntersectedDateRange(
			DoclogFile doclogFile, List<TimeZoneDateRange> dateRanges) {
		TimeZoneDate earliestDate = null;
		TimeZoneDate latestDate = null;
		for (DoclogRecord doclogRecord : doclogFile.getDoclogRecords()) {
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

	private List<Object> getTimelineKeys() {
		final List<Object> keys = new LinkedList<Object>();
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

	private DoclogTimeline getTimeline(final Object key) {
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
	 * <ul>
	 * <li>{@link DoclogTimeline}s already associated with a given key stay
	 * untouched
	 * <li>the other {@link DoclogTimeline} are associated with a new key
	 * <li>{@link DoclogTimeline} that are not needed anymore get disposed
	 * <li>the still not associated keys are returned
	 * </ul>
	 * 
	 * @param usedTimelineKeys
	 * @return keys that were not associated to an existing
	 *         {@link DoclogTimeline}
	 */
	private List<?> prepareExistingTimelines(List<Object> usedTimelineKeys) {
		List<Object> existingTimelines = getTimelineKeys();
		List<?> preparedTimelines = ListUtils.intersection(existingTimelines,
				usedTimelineKeys);
		final List<?> unpreparedTimelines = ListUtils.subtract(
				usedTimelineKeys, preparedTimelines);
		final List<?> freeTimelines = ListUtils.subtract(existingTimelines,
				preparedTimelines);
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				while (freeTimelines.size() > 0
						&& unpreparedTimelines.size() > 0) {
					getTimeline(freeTimelines.remove(0)).setData(
							unpreparedTimelines.remove(0));
				}
			}
		});
		disposeTimelines(freeTimelines.toArray());
		return unpreparedTimelines;
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
