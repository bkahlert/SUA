package de.fu_berlin.imp.seqan.usability_analyzer.doclog.views;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
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
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.Activator;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.DoclogManager;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogFile;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.widgets.DoclogTimeline;

public class DoclogTimelineView extends ViewPart {

	private ISelectionListener selectionListener = new ISelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			Map<ID, List<TimeZoneDateRange>> idDateRanges = IdDateRange
					.group(SelectionRetrieverFactory.getSelectionRetriever(
							IdDateRange.class).getSelection());

			Map<Fingerprint, List<TimeZoneDateRange>> fingerprintDateRanges = FingerprintDateRange
					.group(SelectionRetrieverFactory.getSelectionRetriever(
							FingerprintDateRange.class).getSelection());

			Map<Object, List<TimeZoneDateRange>> groupedDateRanges = new HashMap<Object, List<TimeZoneDateRange>>();
			groupedDateRanges.putAll(idDateRanges);
			groupedDateRanges.putAll(fingerprintDateRanges);

			if (groupedDateRanges.size() > 0) {
				refresh(groupedDateRanges);
			}
		}
	};

	private ISelectionListener postSelectionListener = new ISelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (!part.getClass().equals(DoclogExplorerView.class)
					&& !part.getSite().getId().contains("DiffExplorerView")) {

				List<DoclogFile> doclogFiles = SelectionRetrieverFactory
						.getSelectionRetriever(DoclogFile.class).getSelection();
				if (doclogFiles.size() > 0) {
					init(doclogFiles);
				}
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

	public void init(List<DoclogFile> doclogFiles) {
		disposeUnusedDoclogTimelines(doclogFiles);

		for (DoclogFile doclogFile : doclogFiles) {
			createDoclogTimeline(doclogFile);
		}

		composite.layout();
	}

	public void refresh(Map<Object, List<TimeZoneDateRange>> groupedDateRanges) {
		disposeUnusedDoclogTimelines(groupedDateRanges.keySet());

		for (Object key : groupedDateRanges.keySet()) {
			Pair<DoclogFile, DoclogTimeline> doclog = createDoclogTimeline(key);
			DoclogFile doclogFile = doclog.getValue0();
			DoclogTimeline doclogTimeline = doclog.getValue1();

			List<TimeZoneDateRange> dateRanges = groupedDateRanges.get(key);
			TimeZoneDateRange minMaxDateRange = calculateIntersectedDateRange(
					doclogFile, dateRanges);
			if (minMaxDateRange.getStartDate() != null)
				doclogTimeline.setCenterVisibleDate(minMaxDateRange
						.getStartDate().toISO8601());
			else if (minMaxDateRange.getEndDate() != null)
				doclogTimeline.setCenterVisibleDate(minMaxDateRange
						.getEndDate().toISO8601());
			doclogTimeline.highlight(dateRanges);
		}

		composite.layout();
	}

	protected DoclogTimeline createDoclogTimeline(DoclogFile doclogFile) {
		if (doclogFile.getId() != null) {
			return createDoclogTimeline(doclogFile.getId()).getValue1();
		} else {
			return createDoclogTimeline(doclogFile.getFingerprint())
					.getValue1();
		}
	}

	protected Pair<DoclogFile, DoclogTimeline> createDoclogTimeline(Object key) {
		if (key instanceof ID)
			return createDoclogTimeline((ID) key);
		if (key instanceof Fingerprint)
			return createDoclogTimeline((Fingerprint) key);
		throw new InvalidParameterException(key + " was not of valid type");
	}

	protected Pair<DoclogFile, DoclogTimeline> createDoclogTimeline(ID id) {
		DoclogManager doclogManager = Activator.getDefault().getDoclogManager();
		DoclogFile doclogFile = doclogManager.getDoclogFile(id);
		String title = "ID: " + id.toString();
		return new Pair<DoclogFile, DoclogTimeline>(doclogFile,
				createDoclogTimeline(id, doclogFile, title));
	}

	protected Pair<DoclogFile, DoclogTimeline> createDoclogTimeline(
			Fingerprint fingerprint) {
		DoclogManager doclogManager = Activator.getDefault().getDoclogManager();
		DoclogFile doclogFile = doclogManager.getDoclogFile(fingerprint);
		String title = "Fingerprint: " + fingerprint.toString();
		return new Pair<DoclogFile, DoclogTimeline>(doclogFile,
				createDoclogTimeline(fingerprint, doclogFile, title));
	}

	private DoclogTimeline createDoclogTimeline(Object key,
			DoclogFile doclogFile, String title) {
		DoclogTimeline doclogTimeline = this.doclogTimelines.get(key);
		if (doclogTimeline == null) {
			doclogTimeline = new DoclogTimeline(composite, SWT.NONE, title);
			this.doclogTimelines.put(key, doclogTimeline);
			doclogTimeline.show(doclogFile);
		}
		return doclogTimeline;
	}

	/**
	 * Given a {@link List} of {@link TimeZoneDateRange}s and a {@link DoclogFile}
	 * this method returns the earliest and latest dates that in which
	 * {@link DoclogRecord} events occurred.
	 * 
	 * @param doclogFile
	 * @param dateRanges
	 * @return
	 */
	private TimeZoneDateRange calculateIntersectedDateRange(DoclogFile doclogFile,
			List<TimeZoneDateRange> dateRanges) {
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

	private void disposeUnusedDoclogTimelines(List<DoclogFile> doclogFilesToKeep) {
		HashSet<Object> usedDoclogTimelines = new HashSet<Object>();
		for (DoclogFile doclogFile : doclogFilesToKeep) {
			if (doclogFile.getId() != null)
				usedDoclogTimelines.add(doclogFile.getId());
			else if (doclogFile.getFingerprint() != null)
				usedDoclogTimelines.add(doclogFile.getFingerprint());
			else
				throw new InvalidParameterException(
						DoclogFile.class.getSimpleName() + " has no valid "
								+ ID.class.getSimpleName() + " or "
								+ Fingerprint.class.getSimpleName());
		}
		disposeUnusedDoclogTimelines(usedDoclogTimelines);
	}

	private void disposeUnusedDoclogTimelines(Set<Object> doclogTimelinesToKeep) {
		for (Object key : this.doclogTimelines.keySet()) {
			if (!doclogTimelinesToKeep.contains(key)) {
				Timeline doclogTimeline = this.doclogTimelines.get(key);
				this.doclogTimelines.remove(key);
				if (doclogTimeline != null && !doclogTimeline.isDisposed())
					doclogTimeline.dispose();
			}
		}
		this.composite.layout();
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
