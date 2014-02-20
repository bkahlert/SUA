package de.fu_berlin.imp.seqan.usability_analyzer.diff.views;

import java.io.FileFilter;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
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

import com.bkahlert.devel.rcp.selectionUtils.ArrayUtils;
import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;
import com.bkahlert.nebula.datetime.CalendarRange;
import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.NamedJob;

import de.fu_berlin.imp.seqan.usability_analyzer.core.extensionPoints.IDateRangeListener;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceInitializer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IHighlightService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IHighlightServiceListener;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IWorkSession;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IWorkSessionListener;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IWorkSessionService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.ILocatorService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.DateRangeFilter;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.Activator;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.extensionProviders.IFileFilterListener;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiff;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffs;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.impl.Diff;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.preferences.SUADiffPreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.ui.widgets.FileFilterComposite;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.viewer.DiffContentProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.viewer.DiffViewer;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.viewer.filters.DiffFileListsViewerFileFilter;
import de.ralfebert.rcputils.menus.ContextMenu;

public class DiffView extends ViewPart implements IDateRangeListener,
		IFileFilterListener {

	public static final String ID = "de.fu_berlin.imp.seqan.usability_analyzer.diff.views.DiffView";
	public static final Logger LOGGER = Logger.getLogger(DiffView.class);

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
	private DiffViewer diffViewer;

	private IWorkSessionService workSessionService;
	private final IWorkSessionListener workSessionListener = new IWorkSessionListener() {
		@Override
		public void workSessionStarted(IWorkSession workSession) {
			DiffView.this.load(workSession);
		}
	};

	private IHighlightService highlightService;
	private final IHighlightServiceListener highlightServiceListener = new IHighlightServiceListener() {
		@Override
		public void highlight(Object sender, CalendarRange[] ranges,
				boolean moveInsideViewport) {
			if (DiffView.this.openedDiffs == null
					|| DiffView.this.openedDiffs.keySet().size() == 0
					|| !moveInsideViewport) {
				return;
			}
			Map<IIdentifier, CalendarRange[]> groupedRanges = new HashMap<IIdentifier, CalendarRange[]>();
			for (IIdentifier openedIdentifier : DiffView.this.openedDiffs
					.keySet()) {
				groupedRanges.put(openedIdentifier, ranges);
			}
			this.highlight(sender, groupedRanges, moveInsideViewport);
		}

		@Override
		public void highlight(Object sender,
				final Map<IIdentifier, CalendarRange[]> groupedRanges,
				boolean moveInsideViewport) {
			if (sender == DiffView.this || !moveInsideViewport) {
				return;
			}

			// TODO implement moveInsideViewport support

			ExecUtils.asyncExec(new Runnable() {
				private final ILocatorService locatorService = (ILocatorService) PlatformUI
						.getWorkbench().getService(ILocatorService.class);

				private boolean hasIDiffNodes(TreeItem[] treeItems) {
					for (TreeItem treeItem : treeItems) {
						if (treeItem.getData() instanceof URI) {
							if (this.locatorService.getType((URI) treeItem
									.getData()) == IDiffs.class) {
								return true;
							}
						}
					}
					return false;
				}

				@Override
				public void run() {
					List<TreePath> treePaths = new ArrayList<TreePath>();
					for (IIdentifier identifier : groupedRanges.keySet()) {
						CalendarRange[] dataRanges = groupedRanges
								.get(identifier);
						TreeItem[] treeItems = DiffView.this.diffViewer
								.getTree().getItems();

						try {
							List<TreePath> idIntersectingDoclogRecords;
							if (!this.hasIDiffNodes(treeItems)) {
								idIntersectingDoclogRecords = DiffViewer
										.getItemsOfIntersectingDataRanges(
												treeItems, dataRanges);
							} else {
								idIntersectingDoclogRecords = DiffViewer
										.getItemsOfIdIntersectingDataRanges(
												treeItems, identifier,
												dataRanges);
							}
							treePaths.addAll(idIntersectingDoclogRecords);
						} catch (Exception e) {
							LOGGER.error("Error highlighting " + identifier, e);
						}
					}

					TreeSelection treeSelection = new TreeSelection(treePaths
							.toArray(new TreePath[0]));
					DiffView.this.diffViewer.setSelection(treeSelection);
				}
			});
		}
	};

	private DateRangeFilter dateRangeFilter = null;
	private final HashMap<FileFilter, DiffFileListsViewerFileFilter> diffFileListsViewerFileFilters = new HashMap<FileFilter, DiffFileListsViewerFileFilter>();

	protected static final DateFormat dateFormat = new SimpleDateFormat(
			SUACorePreferenceInitializer.DEFAULT_SMART_DATETIME);

	protected static final String timeDifferenceFormat = new SUACorePreferenceUtil()
			.getTimeDifferenceFormat();

	private HashMap<IIdentifier, IDiffs> openedDiffs = new HashMap<IIdentifier, IDiffs>();

	public DiffView() {

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

		this.diffViewer = new DiffViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL, dateFormat, timeDifferenceFormat);
		final Tree tree = this.diffViewer.getTree();
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tree.setHeaderVisible(true);
		tree.setLinesVisible(false);

		this.diffViewer.setContentProvider(new DiffContentProvider());
		this.diffViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {
					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						if (DiffView.this.highlightService != null
								&& DiffView.this.getSite().getWorkbenchWindow()
										.getActivePage().getActivePart() == DiffView.this) {
							DiffView.this.highlightService.highlight(
									DiffView.this, event.getSelection(), false);
						}
					}
				});
		this.diffViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				List<IDiff> diffs = SelectionUtils.getAdaptableObjects(
						event.getSelection(), IDiff.class);
				if (diffs.size() > 0) {
					for (IDiff diff : diffs) {
						diff.open();
					}
				} else {
					List<IDiffRecord> diffRecords = SelectionUtils
							.getAdaptableObjects(event.getSelection(),
									IDiffRecord.class);
					for (IDiffRecord diffRecord : diffRecords) {
						diffRecord.open();
					}
				}
			}
		});

		this.dateRangeChanged(null, this.preferenceUtil.getDateRange());
		for (FileFilter fileFilter : this.diffPreferenceUtil.getFileFilters()) {
			this.fileFilterAdded(fileFilter);
		}

		new ContextMenu(this.diffViewer, this.getSite()) {
			@Override
			protected String getDefaultCommandID() {
				return null;
			}
		};
	}

	public DiffViewer getDiffFileListsViewer() {
		return this.diffViewer;
	}

	@Override
	public void setFocus() {
		if (this.diffViewer != null && this.diffViewer.getControl() != null
				&& !this.diffViewer.getControl().isDisposed()) {
			this.diffViewer.getControl().setFocus();
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
				return ExecUtils.asyncExec(success);
			} else {
				return null;
			}
		}

		// Case 2: multiple IDs

		return ExecUtils.nonUIAsyncExec(DiffView.class, "Refreshing "
				+ StringUtils.join(ids, ", "), new Callable<T>() {
			@Override
			public T call() throws Exception {
				for (NamedJob loader : ExecUtils.nonUIAsyncExecMerged(
						DiffView.class,
						"Loading " + StringUtils.join(ids, ", "),
						ids,
						new ExecUtils.ParametrizedCallable<IIdentifier, NamedJob>() {
							@Override
							public NamedJob call(final IIdentifier identifier)
									throws Exception {
								NamedJob diffFileLoader = new NamedJob(
										DiffView.class, "Loading "
												+ Diff.class.getSimpleName()
												+ "s ...") {
									@Override
									protected IStatus runNamed(
											IProgressMonitor progressMonitor) {
										SubMonitor monitor = SubMonitor
												.convert(progressMonitor);
										monitor.beginTask("... for"
												+ identifier, 1);
										IDiffs diffs = Activator
												.getDefault()
												.getDiffDataContainer()
												.getDiffFiles(identifier,
														monitor);
										synchronized (newOpenedDiffFileLists) {
											newOpenedDiffFileLists.put(
													identifier, diffs);
										}
										monitor.done();
										return Status.OK_STATUS;
									}
								};
								diffFileLoader.schedule();
								return diffFileLoader;
							}
						})) {
					try {
						loader.join();
					} catch (InterruptedException e) {
						LOGGER.error("Error loading "
								+ Diff.class.getSimpleName());
					}
				}

				if (DiffView.this.diffViewer != null
						&& DiffView.this.diffViewer.getTree() != null
						&& !DiffView.this.diffViewer.getTree().isDisposed()
						&& newOpenedDiffFileLists.size() > 0) {
					DiffView.this.openedDiffs = newOpenedDiffFileLists;
					final String partName = "Diffs - "
							+ StringUtils.join(newOpenedDiffFileLists.keySet(),
									", ");
					ExecUtils.asyncExec(new Runnable() {
						@Override
						public void run() {
							DiffView.this.setPartName(partName);
							List<URI> uris = new ArrayList<URI>();
							for (Iterator<IDiffs> iterator = newOpenedDiffFileLists
									.values().iterator(); iterator.hasNext();) {
								IDiffs diffs = iterator.next();
								uris.add(diffs.getUri());
							}
							DiffView.this.diffViewer.setInput(uris
									.toArray(new URI[uris.size()]));
							DiffView.this.diffViewer.expandAll();
						}
					});
				}

				if (success != null) {
					return ExecUtils.syncExec(new Callable<T>() {
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
			this.diffViewer.removeFilter(this.dateRangeFilter);
		}
		this.dateRangeFilter = new DateRangeFilter(newDateRange);
		this.diffViewer.addFilter(this.dateRangeFilter);
	}

	@Override
	public void fileFilterAdded(final FileFilter fileFilter) {
		if (this.diffViewer != null && this.diffViewer.getTree() != null
				&& !this.diffViewer.getTree().isDisposed()) {
			if (!this.diffFileListsViewerFileFilters.containsKey(fileFilter)) {
				this.diffFileListsViewerFileFilters.put(fileFilter,
						new DiffFileListsViewerFileFilter(fileFilter));
			}
			this.diffViewer.addFilter(this.diffFileListsViewerFileFilters
					.get(fileFilter));
		}
	}

	@Override
	public void fileFilterRemoved(FileFilter fileFilter) {
		if (this.diffViewer != null && this.diffViewer.getTree() != null
				&& !this.diffViewer.getTree().isDisposed()) {
			if (this.diffFileListsViewerFileFilters.containsKey(fileFilter)) {
				this.diffFileListsViewerFileFilters.remove(fileFilter);
				this.diffViewer
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
		DiffView.this.open(new HashSet<IIdentifier>(ids), null);
	}

}
