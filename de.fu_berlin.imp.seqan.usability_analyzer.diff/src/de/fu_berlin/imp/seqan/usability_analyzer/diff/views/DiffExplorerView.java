package de.fu_berlin.imp.seqan.usability_analyzer.diff.views;

import java.io.FileFilter;
import java.text.DateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExecutableExtensionFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.bkahlert.devel.nebula.utils.ExecutorUtil;
import com.bkahlert.devel.rcp.selectionUtils.ArrayUtils;
import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.extensionPoints.IDateRangeListener;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IWorkSession;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IWorkSessionListener;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IWorkSessionService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.DateRangeFilter;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.Activator;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.editors.DiffFileEditorUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.extensionProviders.IFileFilterListener;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.Diff;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffList;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.preferences.SUADiffPreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.ui.widgets.FileFilterComposite;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.viewer.DiffFileListsContentProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.viewer.DiffListsViewer;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.viewer.filters.DiffFileListsViewerFileFilter;
import de.ralfebert.rcputils.menus.ContextMenu;

public class DiffExplorerView extends ViewPart implements IDateRangeListener,
		IFileFilterListener {

	public static final String ID = "de.fu_berlin.imp.seqan.usability_analyzer.diff.views.DiffExplorerView";
	public static final Logger LOGGER = Logger
			.getLogger(DiffExplorerView.class);

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
	private DiffListsViewer diffListsViewer;
	private IWorkSessionListener workSessionListener = new IWorkSessionListener() {
		@Override
		public void workSessionStarted(IWorkSession workSession) {
			final List<ID> ids = ArrayUtils.getAdaptableObjects(workSession
					.getEntities().toArray(), ID.class);
			open(new HashSet<ID>(ids), null);
		}
	};
	private HashMap<ID, DiffList> openedDiffFileLists = new HashMap<ID, DiffList>();

	private DateRangeFilter dateRangeFilter = null;
	private HashMap<FileFilter, DiffFileListsViewerFileFilter> diffFileListsViewerFileFilters = new HashMap<FileFilter, DiffFileListsViewerFileFilter>();

	protected static final DateFormat dateFormat = new SUACorePreferenceUtil()
			.getDateFormat();

	protected static final String timeDifferenceFormat = new SUACorePreferenceUtil()
			.getTimeDifferenceFormat();

	private IWorkSessionService workSessionService;

	private ExecutorService LOADER_POOL = ExecutorUtil
			.newFixedMultipleOfProcessorsThreadPool(1);

	public DiffExplorerView() {

	}

	public String getId() {
		return ID;
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		this.workSessionService = (IWorkSessionService) PlatformUI
				.getWorkbench().getService(IWorkSessionService.class);
		if (this.workSessionService == null)
			LOGGER.warn("Could not get "
					+ IWorkSessionService.class.getSimpleName());

		if (this.workSessionService != null)
			this.workSessionService.addWorkSessionListener(workSessionListener);
	}

	@Override
	public void dispose() {
		if (this.workSessionService != null)
			this.workSessionService
					.removeWorkSessionListener(workSessionListener);
		super.dispose();
	}

	@Override
	@PostConstruct
	public void createPartControl(Composite parent) {
		parent.setLayout(GridLayoutFactory.fillDefaults().spacing(0, 0)
				.create());

		FileFilterComposite filters = new FileFilterComposite(parent, SWT.NONE);
		filters.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		diffListsViewer = new DiffListsViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL, dateFormat, timeDifferenceFormat);
		final Tree tree = diffListsViewer.getTree();
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tree.setHeaderVisible(true);
		tree.setLinesVisible(false);

		this.diffListsViewer
				.setContentProvider(new DiffFileListsContentProvider());
		this.diffListsViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				List<DiffRecord> diffRecords = SelectionUtils
						.getAdaptableObjects(event.getSelection(),
								DiffRecord.class);
				for (DiffRecord diffRecord : diffRecords) {
					DiffFileEditorUtils.closeCompareEditors(diffRecord);
					DiffFileEditorUtils.openCompareEditor(diffRecord);
				}
			}
		});

		this.dateRangeChanged(null, preferenceUtil.getDateRange());
		for (FileFilter fileFilter : diffPreferenceUtil.getFileFilters()) {
			this.fileFilterAdded(fileFilter);
		}

		new ContextMenu(diffListsViewer, getSite()) {
			@Override
			protected String getDefaultCommandID() {
				return null;
			}
		};
	}

	public DiffListsViewer getDiffFileListsViewer() {
		return diffListsViewer;
	}

	@Override
	public void setFocus() {
		if (this.diffListsViewer != null
				&& this.diffListsViewer.getControl() != null
				&& !this.diffListsViewer.getControl().isDisposed()) {
			this.diffListsViewer.getControl().setFocus();
		}
	}

	/**
	 * Opens the given {@link ID}s in the {@link DiffListsViewer}. If the
	 * corresponding {@link DiffList}s could be successfully opened a caller
	 * defined {@link Runnable} gets executed.
	 * <p>
	 * Note: The {@link Runnable} is executed in the UI thread.
	 * 
	 * @param <T>
	 * 
	 * @param ids
	 * @param success
	 */
	public <T> Future<T> open(final Set<ID> ids, final Callable<T> success) {
		final HashMap<ID, DiffList> newOpenedDiffFileLists = new HashMap<ID, DiffList>();

		// do not load already opened diff file list
		for (ID openedID : openedDiffFileLists.keySet()) {
			if (ids.contains(openedID)) {
				newOpenedDiffFileLists.put(openedID,
						openedDiffFileLists.get(openedID));
				ids.remove(openedID);
			}
		}

		// Case 1: no IDs
		if (ids.size() == 0) {
			if (success != null) {
				return ExecutorUtil.asyncExec(success);
			} else
				return null;
		}

		// Case 2: multiple IDs
		final List<Future<Job>> loaders = ExecutorUtil.nonUIAsyncExec(
				LOADER_POOL, ids,
				new ExecutorUtil.ParametrizedCallable<ID, Job>() {
					@Override
					public Job call(final ID id) throws Exception {
						Job diffFileLoader = new Job("Loading "
								+ Diff.class.getSimpleName() + "s ...") {
							@Override
							protected IStatus run(
									IProgressMonitor progressMonitor) {
								SubMonitor monitor = SubMonitor
										.convert(progressMonitor);
								monitor.beginTask("... for" + id, 1);
								DiffList diffList = Activator.getDefault()
										.getDiffDataContainer()
										.getDiffFiles(id, monitor);
								synchronized (newOpenedDiffFileLists) {
									newOpenedDiffFileLists.put(id, diffList);
								}
								monitor.done();
								return Status.OK_STATUS;
							}
						};
						diffFileLoader.schedule();
						return diffFileLoader;
					}
				});

		return ExecutorUtil.nonUIAsyncExec(new Callable<T>() {
			@Override
			public T call() throws Exception {
				for (Future<Job> loader : loaders) {
					try {
						loader.get().join();
					} catch (InterruptedException e) {
						LOGGER.error("Error loading "
								+ Diff.class.getSimpleName());
					} catch (ExecutionException e) {
						LOGGER.error("Error loading "
								+ Diff.class.getSimpleName());
					}
				}

				if (diffListsViewer != null
						&& diffListsViewer.getTree() != null
						&& !diffListsViewer.getTree().isDisposed()
						&& newOpenedDiffFileLists.size() > 0) {
					openedDiffFileLists = newOpenedDiffFileLists;
					final String partName = "Diffs - "
							+ StringUtils.join(newOpenedDiffFileLists.keySet(),
									", ");
					ExecutorUtil.syncExec(new Runnable() {
						@Override
						public void run() {
							setPartName(partName);
							diffListsViewer.setInput(newOpenedDiffFileLists
									.values());
							diffListsViewer.expandAll();
						}
					});
				}

				if (success != null)
					return success.call();
				else
					return null;
			}
		});
	}

	@Override
	public void dateRangeChanged(TimeZoneDateRange oldDateRange,
			TimeZoneDateRange newDateRange) {
		if (this.dateRangeFilter != null)
			this.diffListsViewer.removeFilter(this.dateRangeFilter);
		this.dateRangeFilter = new DateRangeFilter(newDateRange);
		this.diffListsViewer.addFilter(this.dateRangeFilter);
	}

	@Override
	public void fileFilterAdded(final FileFilter fileFilter) {
		if (this.diffListsViewer != null
				&& this.diffListsViewer.getTree() != null
				&& !this.diffListsViewer.getTree().isDisposed()) {
			if (!diffFileListsViewerFileFilters.containsKey(fileFilter)) {
				diffFileListsViewerFileFilters.put(fileFilter,
						new DiffFileListsViewerFileFilter(fileFilter));
			}
			this.diffListsViewer.addFilter(diffFileListsViewerFileFilters
					.get(fileFilter));
		}
	}

	@Override
	public void fileFilterRemoved(FileFilter fileFilter) {
		if (this.diffListsViewer != null
				&& this.diffListsViewer.getTree() != null
				&& !this.diffListsViewer.getTree().isDisposed()) {
			if (this.diffFileListsViewerFileFilters.containsKey(fileFilter)) {
				this.diffFileListsViewerFileFilters.remove(fileFilter);
				this.diffListsViewer
						.removeFilter(diffFileListsViewerFileFilters
								.get(fileFilter));
			}
		}
	}

}
