package de.fu_berlin.imp.seqan.usability_analyzer.diff.views;

import java.io.FileFilter;
import java.net.URI;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
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
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.bkahlert.devel.nebula.utils.ExecutorService;
import com.bkahlert.devel.nebula.utils.ExecutorService.ParametrizedCallable;
import com.bkahlert.devel.nebula.utils.ExecutorUtil;
import com.bkahlert.devel.rcp.selectionUtils.ArrayUtils;
import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.extensionPoints.IDateRangeListener;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IHighlightService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IHighlightServiceListener;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IWorkSession;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IWorkSessionListener;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IWorkSessionService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.DateRangeFilter;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.Activator;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.extensionProviders.IFileFilterListener;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiff;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffs;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.impl.Diff;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.impl.DiffRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.preferences.SUADiffPreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.ui.widgets.FileFilterComposite;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.viewer.DiffContentProvider;
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
					if (viewReference.getId().equals(ID)) {
						return viewReference.getView(true);
					}
				}
			} catch (Exception e) {
				return this;
			}
			return null;
		}
	}

	private final SUACorePreferenceUtil preferenceUtil = new SUACorePreferenceUtil();
	private final SUADiffPreferenceUtil diffPreferenceUtil = new SUADiffPreferenceUtil();
	private DiffListsViewer diffListsViewer;

	private IWorkSessionService workSessionService;
	private final IWorkSessionListener workSessionListener = new IWorkSessionListener() {
		@Override
		public void workSessionStarted(IWorkSession workSession) {
			DiffExplorerView.this.load(workSession);
		}
	};

	private IHighlightService highlightService;
	private final IHighlightServiceListener highlightServiceListener = new IHighlightServiceListener() {
		@Override
		public void highlight(Object sender, TimeZoneDateRange[] ranges,
				boolean moveInsideViewport) {
			if (DiffExplorerView.this.openedDiffs == null
					|| DiffExplorerView.this.openedDiffs.keySet().size() == 0) {
				return;
			}
			Map<IIdentifier, TimeZoneDateRange[]> groupedRanges = new HashMap<IIdentifier, TimeZoneDateRange[]>();
			for (IIdentifier openedIdentifier : DiffExplorerView.this.openedDiffs
					.keySet()) {
				groupedRanges.put(openedIdentifier, ranges);
			}
			this.highlight(sender, groupedRanges, moveInsideViewport);
		}

		@Override
		public void highlight(Object sender,
				final Map<IIdentifier, TimeZoneDateRange[]> groupedRanges,
				boolean moveInsideViewport) {
			if (sender == DiffExplorerView.this) {
				return;
			}

			// TODO implement moveInsideViewport support

			ExecutorUtil.syncExec(new Runnable() {
				@Override
				public void run() {
					List<TreePath> treePaths = new ArrayList<TreePath>();
					for (IIdentifier identifier : groupedRanges.keySet()) {
						TimeZoneDateRange[] dataRanges = groupedRanges
								.get(identifier);
						TreeItem[] treeItems = DiffExplorerView.this.diffListsViewer
								.getTree().getItems();

						List<TreePath> idIntersectingDoclogRecords;
						if (com.bkahlert.devel.nebula.utils.ViewerUtils
								.getItemWithDataType(treeItems, IDiffs.class)
								.size() == 0) {
							idIntersectingDoclogRecords = DiffListsViewer
									.getItemsOfIntersectingDataRanges(
											treeItems, dataRanges);
						} else {
							idIntersectingDoclogRecords = DiffListsViewer
									.getItemsOfIdIntersectingDataRanges(
											treeItems, identifier, dataRanges);
						}
						treePaths.addAll(idIntersectingDoclogRecords);
					}

					TreeSelection treeSelection = new TreeSelection(treePaths
							.toArray(new TreePath[0]));
					DiffExplorerView.this.diffListsViewer
							.setSelection(treeSelection);
				}
			});
		}
	};

	private DateRangeFilter dateRangeFilter = null;
	private final HashMap<FileFilter, DiffFileListsViewerFileFilter> diffFileListsViewerFileFilters = new HashMap<FileFilter, DiffFileListsViewerFileFilter>();

	protected static final DateFormat dateFormat = new SUACorePreferenceUtil()
			.getDateFormat();

	protected static final String timeDifferenceFormat = new SUACorePreferenceUtil()
			.getTimeDifferenceFormat();

	private static final ExecutorService EXECUTOR_SERVICE = new ExecutorService(
			DiffExplorerView.class.getSimpleName());
	private HashMap<IIdentifier, IDiffs> openedDiffs = new HashMap<IIdentifier, IDiffs>();

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
		if (this.workSessionService == null) {
			LOGGER.warn("Could not get "
					+ IWorkSessionService.class.getSimpleName());
		}

		this.highlightService = (IHighlightService) PlatformUI.getWorkbench()
				.getService(IHighlightService.class);
		if (this.highlightService == null) {
			LOGGER.warn("Could not get "
					+ IHighlightService.class.getSimpleName());
		}
	}

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		if (this.workSessionService != null) {
			this.workSessionService
					.addWorkSessionListener(this.workSessionListener);
		}
		if (this.highlightService != null) {
			this.highlightService
					.addHighlightServiceListener(this.highlightServiceListener);
		}
	}

	@Override
	public void dispose() {
		if (this.highlightService != null) {
			this.highlightService
					.removeHighlightServiceListener(this.highlightServiceListener);
		}
		if (this.workSessionService != null) {
			this.workSessionService
					.removeWorkSessionListener(this.workSessionListener);
		}
		super.dispose();
	}

	@Override
	@PostConstruct
	public void createPartControl(Composite parent) {
		parent.setLayout(GridLayoutFactory.fillDefaults().spacing(0, 0)
				.create());

		FileFilterComposite filters = new FileFilterComposite(parent, SWT.NONE);
		filters.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		this.diffListsViewer = new DiffListsViewer(parent, SWT.MULTI
				| SWT.H_SCROLL | SWT.V_SCROLL, dateFormat, timeDifferenceFormat);
		final Tree tree = this.diffListsViewer.getTree();
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tree.setHeaderVisible(true);
		tree.setLinesVisible(false);

		this.diffListsViewer.setContentProvider(new DiffContentProvider());
		this.diffListsViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {
					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						if (DiffExplorerView.this.highlightService != null
								&& DiffExplorerView.this.getSite()
										.getWorkbenchWindow().getActivePage()
										.getActivePart() == DiffExplorerView.this) {
							DiffExplorerView.this.highlightService.highlight(
									DiffExplorerView.this,
									event.getSelection(), false);
						}
					}
				});
		this.diffListsViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				List<IDiff> diffs = SelectionUtils.getAdaptableObjects(
						event.getSelection(), IDiff.class);
				if (diffs.size() > 0) {
					for (IDiff diff : diffs) {
						diff.open();
					}
				} else {
					List<DiffRecord> diffRecords = SelectionUtils
							.getAdaptableObjects(event.getSelection(),
									DiffRecord.class);
					for (DiffRecord diffRecord : diffRecords) {
						diffRecord.open();
					}
				}
			}
		});

		this.dateRangeChanged(null, this.preferenceUtil.getDateRange());
		for (FileFilter fileFilter : this.diffPreferenceUtil.getFileFilters()) {
			this.fileFilterAdded(fileFilter);
		}

		new ContextMenu(this.diffListsViewer, this.getSite()) {
			@Override
			protected String getDefaultCommandID() {
				return null;
			}
		};
	}

	public DiffListsViewer getDiffFileListsViewer() {
		return this.diffListsViewer;
	}

	@Override
	public void setFocus() {
		if (this.diffListsViewer != null
				&& this.diffListsViewer.getControl() != null
				&& !this.diffListsViewer.getControl().isDisposed()) {
			this.diffListsViewer.getControl().setFocus();
			this.load(this.workSessionService.getCurrentWorkSession());
		}
	}

	/**
	 * Opens the given {@link IIdentifier}s in the {@link UriViewer}. If the
	 * corresponding {@link IDiff}s could be successfully opened a caller
	 * defined {@link Runnable} gets executed.
	 * <p>
	 * Note: The {@link Runnable} is executed in the UI thread.
	 * 
	 * @param <T>
	 * 
	 * @param ids
	 * @param success
	 */
	public <T> Future<T> open(final Set<IIdentifier> ids,
			final Callable<T> success) {
		final HashMap<IIdentifier, IDiffs> newOpenedDiffFileLists = new HashMap<IIdentifier, IDiffs>();

		// do not load already opened diff file list
		for (IIdentifier openedIdentifier : this.openedDiffs.keySet()) {
			if (ids.contains(openedIdentifier)) {
				newOpenedDiffFileLists.put(openedIdentifier,
						this.openedDiffs.get(openedIdentifier));
				ids.remove(openedIdentifier);
			}
		}

		// Case 1: no IDs
		if (ids.size() == 0) {
			if (success != null) {
				return ExecutorUtil.asyncExec(success);
			} else {
				return null;
			}
		}

		// Case 2: multiple IDs
		final List<Future<Job>> loaders = EXECUTOR_SERVICE.nonUIAsyncExec(ids,
				new ParametrizedCallable<IIdentifier, Job>() {
					@Override
					public Job call(final IIdentifier identifier)
							throws Exception {
						Job diffFileLoader = new Job("Loading "
								+ Diff.class.getSimpleName() + "s ...") {
							@Override
							protected IStatus run(
									IProgressMonitor progressMonitor) {
								SubMonitor monitor = SubMonitor
										.convert(progressMonitor);
								monitor.beginTask("... for" + identifier, 1);
								IDiffs diffs = Activator.getDefault()
										.getDiffDataContainer()
										.getDiffFiles(identifier, monitor);
								synchronized (newOpenedDiffFileLists) {
									newOpenedDiffFileLists.put(identifier,
											diffs);
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

				if (DiffExplorerView.this.diffListsViewer != null
						&& DiffExplorerView.this.diffListsViewer.getTree() != null
						&& !DiffExplorerView.this.diffListsViewer.getTree()
								.isDisposed()
						&& newOpenedDiffFileLists.size() > 0) {
					DiffExplorerView.this.openedDiffs = newOpenedDiffFileLists;
					final String partName = "Diffs - "
							+ StringUtils.join(newOpenedDiffFileLists.keySet(),
									", ");
					ExecutorUtil.syncExec(new Runnable() {
						@Override
						public void run() {
							DiffExplorerView.this.setPartName(partName);
							List<URI> uris = new ArrayList<URI>();
							for (Iterator<IDiffs> iterator = newOpenedDiffFileLists
									.values().iterator(); iterator.hasNext();) {
								IDiffs diffs = iterator.next();
								uris.add(diffs.getUri());
							}
							// DiffExplorerView.this.diffListsViewer.setInput(uris
							// .toArray(new URI[uris.size()]));
							// DiffExplorerView.this.diffListsViewer.expandAll();
						}
					});
				}

				if (success != null) {
					return ExecutorUtil.syncExec(new Callable<T>() {
						@Override
						public T call() throws Exception {
							return success.call();
						}
					});
				} else {
					return null;
				}
			}
		});
	}

	@Override
	public void dateRangeChanged(TimeZoneDateRange oldDateRange,
			TimeZoneDateRange newDateRange) {
		if (this.dateRangeFilter != null) {
			this.diffListsViewer.removeFilter(this.dateRangeFilter);
		}
		this.dateRangeFilter = new DateRangeFilter(newDateRange);
		this.diffListsViewer.addFilter(this.dateRangeFilter);
	}

	@Override
	public void fileFilterAdded(final FileFilter fileFilter) {
		if (this.diffListsViewer != null
				&& this.diffListsViewer.getTree() != null
				&& !this.diffListsViewer.getTree().isDisposed()) {
			if (!this.diffFileListsViewerFileFilters.containsKey(fileFilter)) {
				this.diffFileListsViewerFileFilters.put(fileFilter,
						new DiffFileListsViewerFileFilter(fileFilter));
			}
			this.diffListsViewer.addFilter(this.diffFileListsViewerFileFilters
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
						.removeFilter(this.diffFileListsViewerFileFilters
								.get(fileFilter));
			}
		}
	}

	/**
	 * Loads the {@link Diff}s from the given {@link IWorkSession}.
	 * 
	 * @param workSession
	 */
	public void load(IWorkSession workSession) {
		final List<IIdentifier> ids = workSession != null ? ArrayUtils
				.getAdaptableObjects(workSession.getEntities(),
						IIdentifier.class) : new LinkedList<IIdentifier>();
		DiffExplorerView.this.open(new HashSet<IIdentifier>(ids), null);
	}

}
