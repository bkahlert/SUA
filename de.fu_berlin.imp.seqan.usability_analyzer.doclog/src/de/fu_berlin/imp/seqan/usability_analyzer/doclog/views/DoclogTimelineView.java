package de.fu_berlin.imp.seqan.usability_analyzer.doclog.views;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
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
import org.javatuples.Pair;

import com.bkahlert.devel.nebula.widgets.timeline.Timeline;
import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;
import com.bkahlert.devel.rcp.selectionUtils.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.FingerprintDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.IdDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogFile;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.util.DoclogCache;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.widgets.DoclogTimeline;

public class DoclogTimelineView extends ViewPart {

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

			Job job = new Job("Preparing " + Timeline.class.getName()) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					if (groupedDateRanges.size() > 0) {
						refresh(groupedDateRanges, monitor);
					}
					return Status.OK_STATUS;
				}
			};
			job.schedule();
		}
	};

	private ISelectionListener postSelectionListener = new ISelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (!part.getClass().equals(DoclogExplorerView.class)
					&& !part.getSite().getId().contains("DiffExplorerView")) {

				final Set<Object> keys = new HashSet<Object>();
				keys.addAll(SelectionRetrieverFactory.getSelectionRetriever(
						ID.class).getSelection());
				keys.addAll(SelectionRetrieverFactory.getSelectionRetriever(
						Fingerprint.class).getSelection());

				Job job = new Job("Preparing " + Timeline.class.getSimpleName()) {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						init(keys, monitor);
						return Status.OK_STATUS;
					}
				};
				job.schedule();
			}
		}
	};

	private Composite composite;
	private Map<Object, DoclogTimeline> doclogTimelines = new HashMap<Object, DoclogTimeline>();

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

	public void init(Set<Object> keys, IProgressMonitor progressMonitor) {
		progressMonitor.beginTask(
				"Preparing " + Timeline.class.getName() + "s", keys.size() + 2);

		disposeUnusedDoclogTimelines(keys);

		progressMonitor.worked(2);

		for (Object key : keys) {
			createDoclogTimeline(key,
					new SubProgressMonitor(progressMonitor, 1));
		}

		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				composite.layout();
			}
		});

		progressMonitor.done();
	}

	public void refresh(Map<Object, List<TimeZoneDateRange>> groupedDateRanges,
			IProgressMonitor progressMonitor) {
		progressMonitor.beginTask("Upading " + Timeline.class.getName() + "s",
				groupedDateRanges.keySet().size() + 2);

		disposeUnusedDoclogTimelines(groupedDateRanges.keySet());

		progressMonitor.worked(2);

		for (Object key : groupedDateRanges.keySet()) {
			Pair<DoclogFile, DoclogTimeline> doclog = createDoclogTimeline(key,
					new NullProgressMonitor());
			DoclogFile doclogFile = doclog.getValue0();
			final DoclogTimeline doclogTimeline = doclog.getValue1();

			final List<TimeZoneDateRange> dateRanges = groupedDateRanges
					.get(key);
			final TimeZoneDateRange minMaxDateRange = calculateIntersectedDateRange(
					doclogFile, dateRanges);
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					if (minMaxDateRange.getStartDate() != null)
						doclogTimeline.setCenterVisibleDate(minMaxDateRange
								.getStartDate().toISO8601());
					else if (minMaxDateRange.getEndDate() != null)
						doclogTimeline.setCenterVisibleDate(minMaxDateRange
								.getEndDate().toISO8601());
					doclogTimeline.highlight(dateRanges);
				}
			});
		}

		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				composite.layout();
			}
		});

		progressMonitor.done();
	}

	protected Pair<DoclogFile, DoclogTimeline> createDoclogTimeline(Object key,
			IProgressMonitor progressMonitor) {
		progressMonitor.beginTask("Creating " + Timeline.class.getSimpleName(),
				3);

		DoclogFile doclogFile = DoclogCache.getInstance().getPayload(key,
				new SubProgressMonitor(progressMonitor, 1));
		if (doclogFile == null)
			return null; // DoclogFile is ID based

		String title;
		if (key instanceof ID) {
			title = "ID: " + key.toString();
		} else if (key instanceof Fingerprint) {
			title = "Fingerprint: " + key.toString();
		} else {
			progressMonitor.done();
			throw new InvalidParameterException(key + " was not of valid type");
		}

		Pair<DoclogFile, DoclogTimeline> rt = new Pair<DoclogFile, DoclogTimeline>(
				doclogFile, createDoclogTimeline(key, doclogFile, title));
		progressMonitor.worked(2);
		progressMonitor.done();
		return rt;
	}

	private DoclogTimeline createDoclogTimeline(final Object key,
			final DoclogFile doclogFile, final String title) {
		final AtomicReference<DoclogTimeline> doclogTimeline = new AtomicReference<DoclogTimeline>(
				this.doclogTimelines.get(key));
		if (doclogTimeline.get() == null) {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					doclogTimeline.set(new DoclogTimeline(composite, SWT.NONE,
							title));
					doclogTimelines.put(key, doclogTimeline.get());
					doclogTimeline.get().show(doclogFile);
				}
			});
		}
		return doclogTimeline.get();
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

	private void disposeUnusedDoclogTimelines(Set<Object> doclogTimelinesToKeep) {
		for (Object key : this.doclogTimelines.keySet()) {
			if (!doclogTimelinesToKeep.contains(key)) {
				final Timeline doclogTimeline = this.doclogTimelines.get(key);
				this.doclogTimelines.remove(key);
				if (doclogTimeline != null && !doclogTimeline.isDisposed()) {
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							doclogTimeline.dispose();
						}
					});
				}
			}
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

		// getSite().setSelectionProvider(null);
	}

	@Override
	public void setFocus() {
	}

}
