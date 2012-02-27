package de.fu_berlin.imp.seqan.usability_analyzer.doclog.views;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExecutableExtensionFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;
import com.bkahlert.devel.rcp.selectionUtils.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.seqan.usability_analyzer.core.extensionPoints.IDateRangeListener;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.IdDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.DateRangeFilter;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.ViewerUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogFile;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.util.DoclogCache;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.viewer.DoclogFilesViewer;

public class DoclogExplorerView extends ViewPart implements IDateRangeListener {

	public static final String ID = "de.fu_berlin.imp.seqan.usability_analyzer.doclog.views.DoclogExplorerView";

	public static class Factory implements IExecutableExtensionFactory {
		@Override
		public Object create() throws CoreException {
			IViewReference[] allviews = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage()
					.getViewReferences();
			for (IViewReference viewReference : allviews) {
				if (viewReference.getId().equals(ID))
					return viewReference.getView(true);
			}
			return null;
		}
	}

	private Job doclogLoader;

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

				if (doclogLoader != null)
					doclogLoader.cancel();

				final LinkedList<DoclogFile> doclogFiles = new LinkedList<DoclogFile>();
				doclogLoader = new Job("Loading "
						+ DoclogFile.class.getSimpleName() + "s") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						for (Object key : keys) {
							DoclogFile doclogFile = DoclogCache.getInstance()
									.getPayload(key,
											new SubProgressMonitor(monitor, 1));
							if (doclogFile != null)
								doclogFiles.add(doclogFile);
						}
						return Status.OK_STATUS;
					}
				};
				doclogLoader.addJobChangeListener(new JobChangeAdapter() {
					@Override
					public void done(IJobChangeEvent event) {
						if (event.getResult() == Status.OK_STATUS) {
							Display.getDefault().asyncExec(new Runnable() {
								@Override
								public void run() {
									if (treeViewer != null
											&& !treeViewer.getTree()
													.isDisposed()
											&& doclogFiles.size() > 0) {
										treeViewer.setInput(doclogFiles);
										treeViewer.expandAll();
									}
								}
							});
						}
					}
				});
				doclogLoader.schedule();
			}
		}
	};

	private ISelectionListener dateRangePostSelectionListener = new ISelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (part.getClass() == DoclogExplorerView.class)
				return;

			List<IdDateRange> idDateRanges = SelectionRetrieverFactory
					.getSelectionRetriever(IdDateRange.class).getSelection();

			if (idDateRanges.size() == 0)
				return;

			Map<ID, List<TimeZoneDateRange>> groupedDateRanges = IdDateRange
					.group(idDateRanges);

			List<TreePath> treePaths = new ArrayList<TreePath>();
			for (ID id : groupedDateRanges.keySet()) {
				List<TimeZoneDateRange> dataRanges = groupedDateRanges.get(id);
				TreeItem[] treeItems = treeViewer.getTree().getItems();

				List<TreePath> idIntersectingDoclogRecords;
				if (ViewerUtils
						.getItemWithDataType(treeItems, DoclogFile.class)
						.size() == 0) {
					idIntersectingDoclogRecords = DoclogFilesViewer
							.getItemsOfIntersectingDataRanges(treeItems,
									dataRanges);
				} else {
					idIntersectingDoclogRecords = DoclogFilesViewer
							.getItemsOfIdIntersectingDataRanges(treeItems, id,
									dataRanges);
				}
				treePaths.addAll(idIntersectingDoclogRecords);
			}

			TreeSelection treeSelection = new TreeSelection(
					treePaths.toArray(new TreePath[0]));
			treeViewer.setSelection(treeSelection);
		}
	};

	private SUACorePreferenceUtil preferenceUtil = new SUACorePreferenceUtil();
	private TreeViewer treeViewer;

	private DateRangeFilter dateRangeFilter = null;

	public static final DateFormat dateFormat = new SUACorePreferenceUtil()
			.getDateFormat();
	public static final String timeDifferenceFormat = new SUACorePreferenceUtil()
			.getTimeDifferenceFormat();

	public DoclogExplorerView() {

	}

	public String getId() {
		return ID;
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		SelectionUtils.getSelectionService().addPostSelectionListener(
				postSelectionListener);
		SelectionUtils.getSelectionService().addPostSelectionListener(
				dateRangePostSelectionListener);
	}

	@Override
	public void dispose() {
		SelectionUtils.getSelectionService().removePostSelectionListener(
				dateRangePostSelectionListener);
		SelectionUtils.getSelectionService().removePostSelectionListener(
				postSelectionListener);
		super.dispose();
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());

		this.treeViewer = new DoclogFilesViewer(parent, SWT.MULTI
				| SWT.H_SCROLL | SWT.V_SCROLL, dateFormat, timeDifferenceFormat);
		final Tree table = treeViewer.getTree();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		this.dateRangeChanged(null, preferenceUtil.getDateRange());

		hookContextMenu();
		getSite().setSelectionProvider(treeViewer);
	}

	private void hookContextMenu() {
		final MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				menuMgr.add(new GroupMarker(
						IWorkbenchActionConstants.MB_ADDITIONS));
			}
		});
		Menu menu = menuMgr.createContextMenu(treeViewer.getControl());
		treeViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, treeViewer);
	}

	@Override
	public void setFocus() {
	}

	@Override
	public void dateRangeChanged(TimeZoneDateRange oldDateRange,
			TimeZoneDateRange newDateRange) {
		if (this.dateRangeFilter != null)
			this.treeViewer.removeFilter(this.dateRangeFilter);
		this.dateRangeFilter = new DateRangeFilter(newDateRange);
		this.treeViewer.addFilter(this.dateRangeFilter);
	}

}
