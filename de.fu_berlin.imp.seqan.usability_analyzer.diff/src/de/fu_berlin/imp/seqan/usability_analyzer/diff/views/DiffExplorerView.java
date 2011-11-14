package de.fu_berlin.imp.seqan.usability_analyzer.diff.views;

import java.io.ByteArrayInputStream;
import java.io.FileFilter;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.IModificationDate;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExecutableExtensionFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;
import com.bkahlert.devel.rcp.selectionUtils.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.seqan.usability_analyzer.core.extensionPoints.IDateRangeListener;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.SortableTreeViewer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.DateRangeFilter;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.editors.DiffFileRecordEditor;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.editors.DiffFileRecordEditorInput;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.editors.DiffFileRecordStorage;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.editors.DiffFileRecordStorageEditorInput;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.extensionProviders.IFileFilterListener;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFileList;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFileRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.viewer.DiffFileListsContentProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.viewer.DiffFileListsViewer;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.viewer.filters.DiffFileListsViewerFileFilter;

public class DiffExplorerView extends ViewPart implements IDateRangeListener,
		IFileFilterListener {

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
	private SortableTreeViewer treeViewer;
	private ISelectionListener selectionListener = new ISelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (part.getClass().equals(DiffExplorerView.class)
					|| part.getSite().getId().contains("DoclogExplorerView"))
				return;

			List<DiffFileList> diffFileLists = SelectionRetrieverFactory
					.getSelectionRetriever(DiffFileList.class).getSelection();
			if (treeViewer != null) {
				treeViewer.setInput(diffFileLists);
				treeViewer.expandAll();
			}
		}
	};

	private DateRangeFilter dateRangeFilter = null;
	private HashMap<FileFilter, DiffFileListsViewerFileFilter> fileFilters = new HashMap<FileFilter, DiffFileListsViewerFileFilter>();

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

	class CompareInput extends CompareEditorInput {
		DiffFileRecord diffFileRecord;

		public CompareInput(DiffFileRecord diffFileRecord) {
			super(new CompareConfiguration());
			this.diffFileRecord = diffFileRecord;
		}

		protected Object prepareInput(IProgressMonitor pm) {
			DiffFileRecord predecessorDiffFileRecord = diffFileRecord
					.getPredecessor();

			CompareItem left = new CompareItem(predecessorDiffFileRecord
					.getDiffFile().getRevision(),
					predecessorDiffFileRecord.getSource(), new Date().getTime());
			CompareItem right = new CompareItem(diffFileRecord.getDiffFile()
					.getRevision(), diffFileRecord.getSource(),
					new Date().getTime());

			return new DiffNode(null, Differencer.ADDITION | Differencer.CHANGE
					| Differencer.DELETION, null, left, right);
		}

		@Override
		public String getName() {
			return diffFileRecord.getFilename();
		}
	}

	class CompareItem implements ITypedElement, IModificationDate,
			IStreamContentAccessor {
		private String contents, name;
		private long time;

		CompareItem(String name, String contents, long time) {
			this.name = name;
			this.contents = contents;
			this.time = time;
		}

		public String getName() {
			return name;
		}

		public Image getImage() {
			return null;
		}

		public String getType() {
			return "cpp";
		}

		public long getModificationDate() {
			return time;
		}

		public InputStream getContents() throws CoreException {
			return new ByteArrayInputStream(contents.getBytes());
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());

		treeViewer = new DiffFileListsViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL, dateFormat, timeDifferenceFormat);
		final Tree tree = treeViewer.getTree();
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);

		this.treeViewer.setContentProvider(new DiffFileListsContentProvider());
		this.treeViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				List<DiffFileRecord> diffFileRecords = SelectionUtils
						.getAdaptableObjects(event.getSelection(),
								DiffFileRecord.class);
				for (DiffFileRecord diffFileRecord : diffFileRecords) {
					IWorkbenchPage page = getSite().getPage();
					DiffFileRecordEditorInput input = new DiffFileRecordEditorInput(
							diffFileRecord);
					try {
						page.openEditor(input, DiffFileRecordEditor.ID);
						page.openEditor(new DiffFileRecordStorageEditorInput(
								new DiffFileRecordStorage(diffFileRecord)),
								"org.eclipse.ui.DefaultTextEditor");

						CompareUI.openCompareEditor(new CompareInput(
								diffFileRecord));
					} catch (PartInitException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});

		this.dateRangeChanged(null, preferenceUtil.getDateRange());

		hookContextMenu();
		getSite().setSelectionProvider(treeViewer);
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {

			}
		});
		Menu menu = menuMgr.createContextMenu(treeViewer.getControl());
		treeViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, treeViewer);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dateRangeChanged(DateRange oldDateRange, DateRange newDateRange) {
		if (this.dateRangeFilter != null)
			this.treeViewer.removeFilter(this.dateRangeFilter);
		this.dateRangeFilter = new DateRangeFilter(newDateRange);
		this.treeViewer.addFilter(this.dateRangeFilter);
	}

	@Override
	public void fileFilterAdded(final FileFilter fileFilter) {
		if (this.treeViewer != null && this.treeViewer.getTree() != null
				&& !this.treeViewer.getTree().isDisposed()) {
			if (!fileFilters.containsKey(fileFilter)) {
				fileFilters.put(fileFilter, new DiffFileListsViewerFileFilter(
						fileFilter));
			}
			this.treeViewer.addFilter(fileFilters.get(fileFilter));
		}
	}

	@Override
	public void fileFilterRemoved(FileFilter fileFilter) {
		if (this.treeViewer != null && this.treeViewer.getTree() != null
				&& !this.treeViewer.getTree().isDisposed()) {
			if (fileFilters.containsKey(fileFilter)) {
				this.treeViewer.removeFilter(fileFilters.get(fileFilter));
			}
		}
	}

}
