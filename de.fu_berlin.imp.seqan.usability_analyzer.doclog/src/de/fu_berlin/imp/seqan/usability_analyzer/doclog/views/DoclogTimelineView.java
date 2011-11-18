package de.fu_berlin.imp.seqan.usability_analyzer.doclog.views;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;
import com.bkahlert.devel.rcp.selectionUtils.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.FingerprintDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.IdDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.Activator;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.DoclogManager;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogRecordList;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.preferences.SUADoclogPreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.widgets.DoclogTimeline;

public class DoclogTimelineView extends ViewPart {

	private Logger logger = Logger.getLogger(DoclogTimelineView.class);

	private ISelectionListener selectionListener = new ISelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			Map<ID, List<DateRange>> idDateRanges = IdDateRange
					.group(SelectionRetrieverFactory.getSelectionRetriever(
							IdDateRange.class).getSelection());

			Map<Fingerprint, List<DateRange>> fingerprintDateRanges = FingerprintDateRange
					.group(SelectionRetrieverFactory.getSelectionRetriever(
							FingerprintDateRange.class).getSelection());

			Map<Object, List<DateRange>> groupedDateRanges = new HashMap<Object, List<DateRange>>();
			groupedDateRanges.putAll(idDateRanges);
			groupedDateRanges.putAll(fingerprintDateRanges);

			if (groupedDateRanges.size() > 0) {
				refresh(groupedDateRanges);
			}
		}
	};

	private IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			if (new SUADoclogPreferenceUtil().screenshotWidthChanged(event)) {
				refresh();
			}
		}
	};

	private SUADoclogPreferenceUtil preferenceUtil = new SUADoclogPreferenceUtil();

	private Composite composite;

	private Map<Object, List<DateRange>> cachedGroupedDateRanges;

	public DoclogTimelineView() {
	}

	protected void refresh() {
		this.clear();

		DoclogManager doclogManager = Activator.getDefault().getDoclogManager();

		int screenshotWidth = preferenceUtil.getScreenshotWidth();
		for (Object key : this.cachedGroupedDateRanges.keySet()) {
			DoclogTimeline doclogTimeline = null;
			DoclogRecordList doclogRecords = null;
			if (key instanceof ID) {
				ID id = (ID) key;
				doclogTimeline = new DoclogTimeline(composite, SWT.NONE, "ID: "
						+ id.toString());
				doclogTimeline.show(doclogManager.getDoclogFile(id));
				doclogRecords = doclogManager.getDoclogFile(id)
						.getDoclogRecords();
			} else if (key instanceof Fingerprint) {
				Fingerprint fingerprint = (Fingerprint) key;
				doclogTimeline = new DoclogTimeline(composite, SWT.NONE,
						"Fingerprint: " + fingerprint.toString());
				doclogTimeline.show(doclogManager.getDoclogFile(fingerprint));
				doclogRecords = doclogManager.getDoclogFile(fingerprint)
						.getDoclogRecords();
			} else {
				logger.fatal(DateRange.class.getSimpleName()
						+ " was of unknown source!");
				return;
			}

			List<DateRange> dateRanges = this.cachedGroupedDateRanges.get(key);
			Date earliestDate = null;
			for (DoclogRecord doclogRecord : doclogRecords) {
				boolean intersects = false;
				for (DateRange dateRange : dateRanges) {
					if (dateRange.isIntersected(doclogRecord.getDateRange())) {
						intersects = true;
						break;
					}
				}

				if (intersects) {
					if (earliestDate == null
							|| earliestDate.after(doclogRecord.getDateRange()
									.getStartDate())) {
						earliestDate = doclogRecord.getDateRange()
								.getStartDate();
					}
				}
			}

			// TODO: auf earliestDate zentrieren
			// TODO: keys merken und nur zentrieren, d.h. nicht neu laden
		}

		composite.layout();
	}

	protected void refresh(Map<Object, List<DateRange>> groupedDateRanges) {
		boolean equals = true;

		// TODO: Compare, wenn gleich, dann kein Update und damit kein erneutes
		// Bilderladen
		for (Object idOrFingerprint : groupedDateRanges.keySet()) {
			List<DateRange> dateRanges = groupedDateRanges.get(idOrFingerprint);
			for (DateRange dateRange : dateRanges) {

			}
		}
		this.cachedGroupedDateRanges = groupedDateRanges;
		refresh();
	}

	protected void clear() {
		for (Control control : composite.getChildren()) {
			if (!control.isDisposed())
				control.dispose();
		}
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		SelectionUtils.getSelectionService().addSelectionListener(
				selectionListener);
		Activator.getDefault().getPreferenceStore()
				.addPropertyChangeListener(propertyChangeListener);
	}

	@Override
	public void dispose() {
		Activator.getDefault().getPreferenceStore()
				.removePropertyChangeListener(propertyChangeListener);
		SelectionUtils.getSelectionService().removeSelectionListener(
				selectionListener);
		super.dispose();
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		this.composite = parent;

		getSite().setSelectionProvider(null); // TODO
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
