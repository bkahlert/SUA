package de.fu_berlin.imp.seqan.usability_analyzer.diff.views;

import java.text.DateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExecutableExtensionFactory;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;
import com.bkahlert.devel.rcp.selectionUtils.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.seqan.usability_analyzer.core.extensionPoints.IDateRangeListener;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.SortableTreeViewer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.DateRangeFilter;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFile;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFileList;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.viewer.DiffExplorerContentProvider;

public class DiffExplorerView extends ViewPart implements IDateRangeListener {

	public static final String ID = "de.fu_berlin.imp.seqan.usability_analyzer.diff.views.DiffExplorerView";

	public static class Factory implements IExecutableExtensionFactory {
		@Override
		public Object create() throws CoreException {
			try {
				IViewReference[] allviews = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage()
						.getViewReferences();
				for (IViewReference viewReference : allviews) {
					if (viewReference.getId().equals(ID))
						return viewReference.getView(true);
				}
			} catch (Exception e) {
				return this;
			}
			return null;
		}
	}

	private SUACorePreferenceUtil preferenceUtil = new SUACorePreferenceUtil();
	private SortableTreeViewer sortableTreeViewer;
	private ISelectionListener selectionListener = new ISelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (part.getClass().equals(DiffExplorerView.class)
					|| part.getSite().getId().contains("DoclogExplorerView"))
				return;

			List<DiffFileList> diffFileLists = SelectionRetrieverFactory
					.getSelectionRetriever(DiffFileList.class).getSelection();
			if (sortableTreeViewer != null) {
				sortableTreeViewer.setInput(diffFileLists);
				sortableTreeViewer.expandAll();
			}
		}
	};

	private DateRangeFilter dateRangeFilter = null;

	protected static final DateFormat dateFormat = new SUACorePreferenceUtil()
			.getDateFormat();

	protected static final String timeDifferenceFormat = new SUACorePreferenceUtil()
			.getTimeDifferenceFormat();

	public DiffExplorerView() {
	}

	public String getId() {
		return ID;
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		SelectionUtils.getSelectionService().addSelectionListener(
				selectionListener);
	}

	@Override
	public void dispose() {
		SelectionUtils.getSelectionService().removeSelectionListener(
				selectionListener);
		super.dispose();
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());

		sortableTreeViewer = new SortableTreeViewer(parent, SWT.MULTI
				| SWT.H_SCROLL | SWT.V_SCROLL);
		final Tree tree = sortableTreeViewer.getTree();
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);

		this.sortableTreeViewer
				.setContentProvider(new DiffExplorerContentProvider());

		this.sortableTreeViewer.createColumn("Date", 160).setLabelProvider(
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						if (element instanceof DiffFileList) {
							DiffFileList diffFileList = (DiffFileList) element;
							ID id = null;
							if (diffFileList.size() > 0) {
								id = diffFileList.get(0).getId();
							}
							return (id != null) ? id.toString() : "";
						} else {
							DiffFile diffFile = (DiffFile) element;
							Date date = diffFile.getDate();
							return (date != null) ? dateFormat.format(date)
									: "";
						}
					}
				});

		this.sortableTreeViewer.createColumn("Passed", 40, true,
				new Comparator<Object>() {
					@Override
					public int compare(Object arg0, Object arg1) {
						Long l1 = (Long) arg0;
						Long l2 = (Long) arg1;
						if (l1 != null)
							return l1.compareTo(l2);
						return 0;
					}
				}, new Class<?>[] { Long.class }).setLabelProvider(
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						if (element instanceof DiffFile) {
							DiffFile doclogRecord = (DiffFile) element;
							Long milliSecondsPassed = doclogRecord
									.getMillisecondsPassed();
							return (milliSecondsPassed != null) ? DurationFormatUtils
									.formatDuration(milliSecondsPassed,
											timeDifferenceFormat, true) : "";
						}
						return "";
					}
				});

		this.sortableTreeViewer.createColumn("Revision", 65).setLabelProvider(
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						if (element instanceof DiffFileList) {
							DiffFileList diffFileList = (DiffFileList) element;
							return "# " + diffFileList.size();
						} else {
							DiffFile diffFile = (DiffFile) element;
							String revision = diffFile.getRevision();
							return (revision != null) ? revision : "";
						}
					}
				});

		this.sortableTreeViewer.sort(0);
		this.dateRangeChanged(null, preferenceUtil.getDateRange());

		hookContextMenu();
		getSite().setSelectionProvider(sortableTreeViewer);
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {

			}
		});
		Menu menu = menuMgr.createContextMenu(sortableTreeViewer.getControl());
		sortableTreeViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, sortableTreeViewer);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dateRangeChanged(DateRange oldDateRange, DateRange newDateRange) {
		if (this.dateRangeFilter != null)
			this.sortableTreeViewer.removeFilter(this.dateRangeFilter);
		this.dateRangeFilter = new DateRangeFilter(newDateRange);
		this.sortableTreeViewer.addFilter(this.dateRangeFilter);
	}

}
