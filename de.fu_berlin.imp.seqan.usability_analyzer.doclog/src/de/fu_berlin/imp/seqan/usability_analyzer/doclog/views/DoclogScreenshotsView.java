package de.fu_berlin.imp.seqan.usability_analyzer.doclog.views;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
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
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.widgets.DoclogScreenshotDisplay;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.widgets.DoclogScreenshotDisplayContainer;

public class DoclogScreenshotsView extends ViewPart {

	private Logger logger = Logger.getLogger(DoclogScreenshotsView.class);

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

	private ScrolledComposite scrolledComposite;
	private Composite composite;

	private Map<Object, List<DateRange>> cachedGroupedDateRanges;

	public DoclogScreenshotsView() {
	}

	protected void refresh() {
		this.clear();

		DoclogManager doclogManager = Activator.getDefault().getDoclogManager();

		int screenshotWidth = preferenceUtil.getScreenshotWidth();
		for (Object key : this.cachedGroupedDateRanges.keySet()) {
			DoclogScreenshotDisplayContainer screenshotDisplayContainer = null;
			DoclogRecordList doclogRecords = null;
			if (key instanceof ID) {
				ID id = (ID) key;
				screenshotDisplayContainer = new DoclogScreenshotDisplayContainer(
						composite, SWT.BORDER, "ID: " + id.toString());
				doclogRecords = doclogManager.getDoclogFile(id)
						.getDoclogRecords();
			} else if (key instanceof Fingerprint) {
				Fingerprint fingerprint = (Fingerprint) key;
				screenshotDisplayContainer = new DoclogScreenshotDisplayContainer(
						composite, SWT.BORDER, "Fingerprint: "
								+ fingerprint.toString());
				doclogRecords = doclogManager.getDoclogFile(fingerprint)
						.getDoclogRecords();
			} else {
				logger.fatal(DateRange.class.getSimpleName()
						+ " was of unknown source!");
				return;
			}

			List<DateRange> dateRanges = this.cachedGroupedDateRanges.get(key);
			for (DoclogRecord doclogRecord : doclogRecords) {
				boolean intersects = false;
				for (DateRange dateRange : dateRanges) {
					if (dateRange.isIntersected(doclogRecord.getDateRange()))
						intersects = true;
				}

				if (intersects) {
					DoclogScreenshotDisplay screenshotDisplay = new DoclogScreenshotDisplay(
							screenshotDisplayContainer, SWT.BORDER);
					screenshotDisplay.setLayoutData(GridDataFactory
							.fillDefaults().grab(true, false).create());
					screenshotDisplay.setScreenshot(
							doclogRecord.getScreenshot(), screenshotWidth);
				}
			}
		}

		composite.layout();

		int verticalBarWidth = (scrolledComposite.getVerticalBar() != null) ? scrolledComposite
				.getVerticalBar().getSize().x : 0;
		int widthHint = Math.max(composite.getSize().x - 50 + verticalBarWidth,
				composite.getClientArea().width - 50);

		scrolledComposite.setMinSize(composite.computeSize(widthHint,
				SWT.DEFAULT));
	}

	protected void refresh(Map<Object, List<DateRange>> groupedDateRanges) {
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
		this.scrolledComposite = new ScrolledComposite(parent, SWT.V_SCROLL);
		this.scrolledComposite.getVerticalBar().setIncrement(
				this.scrolledComposite.getVerticalBar().getIncrement() * 3);
		this.scrolledComposite.setExpandHorizontal(true);
		this.scrolledComposite.setExpandVertical(true);

		this.composite = new Composite(this.scrolledComposite, SWT.NONE);
		this.scrolledComposite.setContent(this.composite);
		this.composite.setLayout(GridLayoutFactory.fillDefaults()
				.margins(10, 10).create());

		getSite().setSelectionProvider(null); // TODO
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
