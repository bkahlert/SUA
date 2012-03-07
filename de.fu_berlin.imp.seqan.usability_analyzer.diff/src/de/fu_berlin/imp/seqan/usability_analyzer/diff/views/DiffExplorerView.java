package de.fu_berlin.imp.seqan.usability_analyzer.diff.views;

import java.io.FileFilter;
import java.text.DateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.compare.CompareUI;
import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExecutableExtensionFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IEditorReference;
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
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.DateRangeFilter;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.Activator;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.editors.DiffFileRecordCompareEditorInput;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.extensionProviders.IFileFilterListener;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFile;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFileList;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFileRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.preferences.SUADiffPreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.ui.widgets.FileFilterComposite;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.viewer.DiffFileListsContentProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.viewer.DiffFileListsViewer;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.viewer.filters.DiffFileListsViewerFileFilter;

@SuppressWarnings("restriction")
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
	private SUADiffPreferenceUtil diffPreferenceUtil = new SUADiffPreferenceUtil();
	private DiffFileListsViewer diffFileListsViewer;
	private Map<ID, Job> diffFileLoaders = Collections
			.synchronizedMap(new HashMap<ID, Job>(2));
	private ISelectionListener selectionListener = new ISelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (part.getClass().equals(DiffExplorerView.class)
					|| part.getSite().getId().contains("DoclogExplorerView"))
				return;

			final List<ID> ids = Collections
					.synchronizedList(SelectionRetrieverFactory
							.getSelectionRetriever(ID.class).getSelection());
			final LinkedList<DiffFileList> diffFileLists = new LinkedList<DiffFileList>();
			for (final ID id : ids) {
				if (diffFileLoaders.containsKey(id))
					continue;

				Job diffFileLoader = new Job("Loading "
						+ DiffFile.class.getSimpleName() + "s") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						DiffFileList diffFileList = Activator.getDefault()
								.getDiffFileDirectory()
								.getDiffFiles(id, monitor);
						synchronized (diffFileLists) {
							diffFileLists.add(diffFileList);
						}
						return Status.OK_STATUS;
					}
				};
				diffFileLoaders.put(id, diffFileLoader);
				diffFileLoader.addJobChangeListener(new JobChangeAdapter() {
					@Override
					public void done(IJobChangeEvent event) {
						boolean jobsFinished;
						synchronized (diffFileLists) {
							jobsFinished = diffFileLists.size() == ids.size();
						}
						if (jobsFinished
								&& event.getResult() == Status.OK_STATUS) {
							Display.getDefault().syncExec(new Runnable() {
								@Override
								public void run() {
									if (diffFileListsViewer != null
											&& !diffFileListsViewer.getTree()
													.isDisposed()
											&& diffFileLists.size() > 0) {
										diffFileListsViewer
												.setInput(diffFileLists);
										diffFileListsViewer.expandAll();
									}
								}
							});
						}
						diffFileLoaders.remove(id);
					}
				});
				diffFileLoaders.put(id, diffFileLoader);
				diffFileLoader.schedule();
			}
		}
	};

	private DateRangeFilter dateRangeFilter = null;
	private HashMap<FileFilter, DiffFileListsViewerFileFilter> diffFileListsViewerFileFilters = new HashMap<FileFilter, DiffFileListsViewerFileFilter>();

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
		parent.setLayout(GridLayoutFactory.fillDefaults().spacing(0, 0)
				.create());

		FileFilterComposite filters = new FileFilterComposite(parent, SWT.NONE);
		filters.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		diffFileListsViewer = new DiffFileListsViewer(parent, SWT.MULTI
				| SWT.H_SCROLL | SWT.V_SCROLL, dateFormat, timeDifferenceFormat);
		final Tree tree = diffFileListsViewer.getTree();
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);

		this.diffFileListsViewer
				.setContentProvider(new DiffFileListsContentProvider());
		this.diffFileListsViewer
				.addDoubleClickListener(new IDoubleClickListener() {
					@Override
					public void doubleClick(DoubleClickEvent event) {
						List<DiffFileRecord> diffFileRecords = SelectionUtils
								.getAdaptableObjects(event.getSelection(),
										DiffFileRecord.class);
						for (DiffFileRecord diffFileRecord : diffFileRecords) {
							closeCompareEditors(diffFileRecord);
							openCompareEditor(diffFileRecord);
						}
					}
				});

		this.dateRangeChanged(null, preferenceUtil.getDateRange());
		for (FileFilter fileFilter : diffPreferenceUtil.getFileFilters()) {
			this.fileFilterAdded(fileFilter);
		}

		hookContextMenu();
		getSite().setSelectionProvider(diffFileListsViewer);
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
		Menu menu = menuMgr.createContextMenu(diffFileListsViewer.getControl());
		diffFileListsViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, diffFileListsViewer);
	}

	public DiffFileListsViewer getDiffFileListsViewer() {
		return diffFileListsViewer;
	}

	@Override
	public void setFocus() {
		if (this.diffFileListsViewer != null
				&& this.diffFileListsViewer.getControl() != null
				&& !this.diffFileListsViewer.getControl().isDisposed()) {
			this.diffFileListsViewer.getControl().setFocus();
		}
	}

	/**
	 * Opens a new {@link CompareEditor} which displays the difference between
	 * the given and its predecessor {@link DiffFileRecord}.
	 * 
	 * @param diffFileRecord
	 */
	private void openCompareEditor(DiffFileRecord diffFileRecord) {
		CompareUI.openCompareEditor(new DiffFileRecordCompareEditorInput(
				diffFileRecord));
	}

	/**
	 * Closes all {@link CompareEditor}s responsible for the given
	 * {@link DiffFileRecord}.
	 * 
	 * @param diffFileRecord
	 */
	private void closeCompareEditors(DiffFileRecord diffFileRecord) {
		IEditorReference[] editorReferences = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage()
				.getEditorReferences();
		for (IEditorReference editorReference : editorReferences) {
			try {
				if (editorReference.getEditorInput() instanceof DiffFileRecordCompareEditorInput) {
					DiffFileRecordCompareEditorInput currentCompareInput = (DiffFileRecordCompareEditorInput) editorReference
							.getEditorInput();
					String currentFilename = currentCompareInput
							.getDiffFileRecord().getFilename();
					if (currentFilename.equals(diffFileRecord.getFilename())) {
						editorReference.getPage().closeEditor(
								editorReference.getEditor(true), false);
					}
				}
			} catch (PartInitException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void dateRangeChanged(TimeZoneDateRange oldDateRange,
			TimeZoneDateRange newDateRange) {
		if (this.dateRangeFilter != null)
			this.diffFileListsViewer.removeFilter(this.dateRangeFilter);
		this.dateRangeFilter = new DateRangeFilter(newDateRange);
		this.diffFileListsViewer.addFilter(this.dateRangeFilter);
	}

	@Override
	public void fileFilterAdded(final FileFilter fileFilter) {
		if (this.diffFileListsViewer != null
				&& this.diffFileListsViewer.getTree() != null
				&& !this.diffFileListsViewer.getTree().isDisposed()) {
			if (!diffFileListsViewerFileFilters.containsKey(fileFilter)) {
				diffFileListsViewerFileFilters.put(fileFilter,
						new DiffFileListsViewerFileFilter(fileFilter));
			}
			this.diffFileListsViewer.addFilter(diffFileListsViewerFileFilters
					.get(fileFilter));
		}
	}

	@Override
	public void fileFilterRemoved(FileFilter fileFilter) {
		if (this.diffFileListsViewer != null
				&& this.diffFileListsViewer.getTree() != null
				&& !this.diffFileListsViewer.getTree().isDisposed()) {
			if (this.diffFileListsViewerFileFilters.containsKey(fileFilter)) {
				this.diffFileListsViewerFileFilters.remove(fileFilter);
				this.diffFileListsViewer
						.removeFilter(diffFileListsViewerFileFilters
								.get(fileFilter));
			}
		}
	}

}
