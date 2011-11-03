package de.fu_berlin.imp.seqan.usability_analyzer.doclog.views;

import java.util.ArrayList;
import java.util.List;

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

import de.fu_berlin.imp.seqan.usability_analyzer.doclog.Activator;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.preferences.PreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.widgets.DoclogScreenshotDisplay;

public class DoclogScreenshotsView extends ViewPart {

	private ISelectionListener selectionListener = new ISelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			List<DoclogRecord> doclogRecords = SelectionRetrieverFactory
					.getSelectionRetriever(DoclogRecord.class).getSelection();
			if (doclogRecords.size() > 0) {
				// refresh(doclogRecords); TODO
				List<DoclogRecord> oneDoclogRecord = new ArrayList<DoclogRecord>();
				oneDoclogRecord.add(doclogRecords.get(0));
				refresh(oneDoclogRecord);
			}
		}
	};

	private IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			if (new PreferenceUtil().screenshotWidthChanged(event)) {
				// refresh(); // TODO
			}
		}
	};

	private PreferenceUtil preferenceUtil = new PreferenceUtil();

	private ScrolledComposite scrolledComposite;
	private Composite composite;

	private List<DoclogRecord> doclogRecords;

	public DoclogScreenshotsView() {
	}

	protected void refresh() {
		this.clear();

		int screenshotWidth = preferenceUtil.getScreenshotWidth();
		for (DoclogRecord doclogRecord : doclogRecords) {
			DoclogScreenshotDisplay screenshotDisplay = new DoclogScreenshotDisplay(
					composite, SWT.BORDER);
			screenshotDisplay.setLayoutData(GridDataFactory.fillDefaults()
					.grab(true, false).create());
			screenshotDisplay.setScreenshot(doclogRecord.getScreenshot(),
					screenshotWidth);
		}

		composite.layout();

		int verticalBarWidth = (scrolledComposite.getVerticalBar() != null) ? scrolledComposite
				.getVerticalBar().getSize().x : 0;
		int widthHint = Math.max(composite.getSize().x - 50 + verticalBarWidth,
				composite.getClientArea().width - 50);

		scrolledComposite.setMinSize(composite.computeSize(widthHint,
				SWT.DEFAULT));
	}

	protected void refresh(List<DoclogRecord> doclogRecords) {
		this.doclogRecords = doclogRecords;
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
