package de.fu_berlin.imp.seqan.usability_analyzer.doclog.views;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExecutableExtensionFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.bkahlert.devel.nebula.utils.ExecutorUtil;
import com.bkahlert.devel.rcp.selectionUtils.ArrayUtils;

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
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.Activator;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.Doclog;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.viewer.DoclogFilesViewer;
import de.ralfebert.rcputils.menus.ContextMenu;

public class DoclogExplorerView extends ViewPart implements IDateRangeListener {

	private static final Logger LOGGER = Logger
			.getLogger(DoclogExplorerView.class);
	public static final String ID = "de.fu_berlin.imp.seqan.usability_analyzer.doclog.views.DoclogExplorerView";

	public static class Factory implements IExecutableExtensionFactory {
		@Override
		public Object create() throws CoreException {
			try {
				IViewReference[] allviews = ExecutorUtil
						.syncExec(new Callable<IViewReference[]>() {
							@Override
							public IViewReference[] call() throws Exception {
								return PlatformUI.getWorkbench()
										.getActiveWorkbenchWindow()
										.getActivePage().getViewReferences();
							}
						});
				for (IViewReference viewReference : allviews) {
					if (viewReference.getId().equals(ID)) {
						return viewReference.getView(true);
					}
				}
			} catch (Exception e) {
				LOGGER.error(e);
			}
			return null;
		}
	}

	private Map<Object, Doclog> openedDoclogFiles = new HashMap<Object, Doclog>();

	private IWorkSessionService workSessionService;
	private IWorkSessionListener workSessionListener = new IWorkSessionListener() {
		@Override
		public void workSessionStarted(IWorkSession workSession) {
			final Set<IIdentifier> identifiers = new HashSet<IIdentifier>();
			identifiers.addAll(ArrayUtils.getAdaptableObjects(workSession
					.getEntities().toArray(), IIdentifier.class));
			DoclogExplorerView.this.open(identifiers, null);
		}
	};

	private IHighlightService highlightService;
	private IHighlightServiceListener highlightServiceListener = new IHighlightServiceListener() {
		@Override
		public void highlight(Object sender, TimeZoneDateRange[] ranges) {
			if (DoclogExplorerView.this.loadedIdentifiers == null
					|| DoclogExplorerView.this.loadedIdentifiers.size() == 0) {
				return;
			}
			Map<IIdentifier, TimeZoneDateRange[]> groupedRanges = new HashMap<IIdentifier, TimeZoneDateRange[]>();
			for (IIdentifier loadedIdentifier : DoclogExplorerView.this.loadedIdentifiers) {
				groupedRanges.put(loadedIdentifier, ranges);
			}
			this.highlight(sender, groupedRanges);
		}

		@Override
		public void highlight(Object sender,
				final Map<IIdentifier, TimeZoneDateRange[]> groupedRanges) {
			if (sender == DoclogExplorerView.this) {
				return;
			}

			ExecutorUtil.syncExec(new Runnable() {
				@Override
				public void run() {
					List<TreePath> treePaths = new ArrayList<TreePath>();
					for (IIdentifier identifier : groupedRanges.keySet()) {
						TimeZoneDateRange[] dataRanges = groupedRanges
								.get(identifier);
						TreeItem[] treeItems = DoclogExplorerView.this.treeViewer
								.getTree().getItems();

						List<TreePath> idIntersectingDoclogRecords;
						if (com.bkahlert.devel.nebula.utils.ViewerUtils
								.getItemWithDataType(treeItems, Doclog.class)
								.size() == 0) {
							idIntersectingDoclogRecords = DoclogFilesViewer
									.getItemsOfIntersectingDataRanges(
											treeItems, dataRanges);
						} else {
							idIntersectingDoclogRecords = DoclogFilesViewer
									.getItemsOfIdIntersectingDataRanges(
											treeItems, identifier, dataRanges);
						}
						treePaths.addAll(idIntersectingDoclogRecords);
					}

					TreeSelection treeSelection = new TreeSelection(treePaths
							.toArray(new TreePath[0]));
					DoclogExplorerView.this.treeViewer
							.setSelection(treeSelection);
				}
			});
		}
	};

	private SUACorePreferenceUtil preferenceUtil = new SUACorePreferenceUtil();
	private TreeViewer treeViewer;

	private DateRangeFilter dateRangeFilter = null;

	public static final DateFormat dateFormat = new SUACorePreferenceUtil()
			.getDateFormat();
	public static final String timeDifferenceFormat = new SUACorePreferenceUtil()
			.getTimeDifferenceFormat();

	private ExecutorService LOADER_POOL = ExecutorUtil
			.newFixedMultipleOfProcessorsThreadPool(1);
	private Set<IIdentifier> loadedIdentifiers;

	public DoclogExplorerView() {
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

	public String getId() {
		return ID;
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
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
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());

		this.treeViewer = new DoclogFilesViewer(parent, SWT.MULTI
				| SWT.H_SCROLL | SWT.V_SCROLL, dateFormat, timeDifferenceFormat);
		final Tree table = this.treeViewer.getTree();
		table.setHeaderVisible(true);
		table.setLinesVisible(false);
		this.treeViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {
					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						if (DoclogExplorerView.this.highlightService != null
								&& DoclogExplorerView.this.getSite()
										.getWorkbenchWindow().getActivePage()
										.getActivePart() == DoclogExplorerView.this) {
							DoclogExplorerView.this.highlightService.highlight(
									DoclogExplorerView.this,
									event.getSelection());
						}
					}
				});

		this.dateRangeChanged(null, this.preferenceUtil.getDateRange());

		new ContextMenu(this.treeViewer, this.getSite()) {
			@Override
			protected String getDefaultCommandID() {
				return null;
			}
		};
	}

	public TreeViewer getDoclogFilesViewer() {
		return this.treeViewer;
	}

	@Override
	public void setFocus() {
	}

	@Override
	public void dateRangeChanged(TimeZoneDateRange oldDateRange,
			final TimeZoneDateRange newDateRange) {
		ExecutorUtil.syncExec(new Runnable() {
			@Override
			public void run() {
				if (DoclogExplorerView.this.dateRangeFilter != null) {
					DoclogExplorerView.this.treeViewer
							.removeFilter(DoclogExplorerView.this.dateRangeFilter);
				}
				DoclogExplorerView.this.dateRangeFilter = new DateRangeFilter(
						newDateRange);
				DoclogExplorerView.this.treeViewer
						.addFilter(DoclogExplorerView.this.dateRangeFilter);
			}
		});
	}

	/**
	 * Opens the given {@link IIdentifier}s in the {@link DoclogFilesViewer}. If
	 * the corresponding {@link Doclog}s could be successfully opened a caller
	 * defined {@link Runnable} gets executed.
	 * <p>
	 * Note: The {@link Runnable} is executed in the UI thread.
	 * 
	 * @param identifiers
	 * @param success
	 */
	public <T> Future<T> open(final Set<IIdentifier> identifiers,
			final Callable<T> success) {
		for (IIdentifier identifier : identifiers) {
			Assert.isNotNull(identifier);
		}

		final HashMap<Object, Doclog> newOpenedDoclogFiles = new HashMap<Object, Doclog>();

		// do not load already opened doclog files
		for (Object openedKey : this.openedDoclogFiles.keySet()) {
			if (identifiers.contains(openedKey)) {
				newOpenedDoclogFiles.put(openedKey,
						this.openedDoclogFiles.get(openedKey));
				identifiers.remove(openedKey);
			}
		}

		// Case 1: no IDs
		if (identifiers.size() == 0) {
			if (success != null) {
				return ExecutorUtil.asyncExec(success);
			} else {
				return null;
			}
		}

		// Case 2: multiple IDs
		final List<Future<Job>> loaders = ExecutorUtil.nonUIAsyncExec(
				this.LOADER_POOL, identifiers,
				new ExecutorUtil.ParametrizedCallable<IIdentifier, Job>() {
					@Override
					public Job call(final IIdentifier identifier)
							throws Exception {
						Job doclogFileLoader = new Job("Loading "
								+ Doclog.class.getSimpleName() + "s ...") {
							@Override
							protected IStatus run(
									IProgressMonitor progressMonitor) {
								SubMonitor monitor = SubMonitor
										.convert(progressMonitor);
								monitor.beginTask("... for" + identifier, 1);
								Doclog doclog = Activator
										.getDefault()
										.getDoclogContainer()
										.getDoclogFile(identifier,
												monitor.newChild(1));
								synchronized (newOpenedDoclogFiles) {
									if (doclog != null) {
										newOpenedDoclogFiles.put(identifier,
												doclog);
									} else {
										identifiers.remove(identifier);
									}
								}
								monitor.done();
								return Status.OK_STATUS;
							};
						};
						doclogFileLoader.schedule();
						return doclogFileLoader;
					};
				});

		this.loadedIdentifiers = identifiers;

		return ExecutorUtil.nonUIAsyncExec(new Callable<T>() {
			@Override
			public T call() throws Exception {
				for (Future<Job> loader : loaders) {
					try {
						loader.get().join();
					} catch (InterruptedException e) {
						LOGGER.error("Error loading "
								+ Doclog.class.getSimpleName());
					} catch (ExecutionException e) {
						LOGGER.error("Error loading "
								+ Doclog.class.getSimpleName());
					}
				}

				if (DoclogExplorerView.this.treeViewer != null
						&& DoclogExplorerView.this.treeViewer.getTree() != null
						&& !DoclogExplorerView.this.treeViewer.getTree()
								.isDisposed()
						&& newOpenedDoclogFiles.size() > 0) {
					DoclogExplorerView.this.openedDoclogFiles = newOpenedDoclogFiles;
					final String partName = "Doclogs - "
							+ StringUtils.join(newOpenedDoclogFiles.keySet(),
									", ");
					ExecutorUtil.syncExec(new Runnable() {
						@Override
						public void run() {
							DoclogExplorerView.this.setPartName(partName);
							DoclogExplorerView.this.treeViewer
									.setInput(newOpenedDoclogFiles.values());
							DoclogExplorerView.this.treeViewer.expandAll();
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
}
