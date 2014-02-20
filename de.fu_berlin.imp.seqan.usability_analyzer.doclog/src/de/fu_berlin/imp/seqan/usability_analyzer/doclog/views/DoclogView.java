package de.fu_berlin.imp.seqan.usability_analyzer.doclog.views;

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
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

import com.bkahlert.devel.rcp.selectionUtils.ArrayUtils;
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
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.Activator;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.Doclog;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.viewer.DoclogViewer;
import de.ralfebert.rcputils.menus.ContextMenu;

public class DoclogView extends ViewPart implements IDateRangeListener {

	private static final Logger LOGGER = Logger.getLogger(DoclogView.class);
	public static final String ID = "de.fu_berlin.imp.seqan.usability_analyzer.doclog.views.DoclogView";

	public static class Factory implements IExecutableExtensionFactory {
		@Override
		public Object create() throws CoreException {
			try {
				IViewReference[] allviews = ExecUtils
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

	private final IWorkSessionService workSessionService;
	private final IWorkSessionListener workSessionListener = new IWorkSessionListener() {
		@Override
		public void workSessionStarted(IWorkSession workSession) {
			DoclogView.this.load(workSession);
		}
	};

	private final IHighlightService highlightService;
	private final IHighlightServiceListener highlightServiceListener = new IHighlightServiceListener() {
		@Override
		public void highlight(Object sender, CalendarRange[] ranges,
				boolean moveInsideViewport) {
			if (DoclogView.this.loadedIdentifiers == null
					|| DoclogView.this.loadedIdentifiers.size() == 0) {
				return;
			}
			Map<IIdentifier, CalendarRange[]> groupedRanges = new HashMap<IIdentifier, CalendarRange[]>();
			for (IIdentifier loadedIdentifier : DoclogView.this.loadedIdentifiers) {
				groupedRanges.put(loadedIdentifier, ranges);
			}
			this.highlight(sender, groupedRanges, moveInsideViewport);
		}

		@Override
		public void highlight(Object sender,
				final Map<IIdentifier, CalendarRange[]> groupedRanges,
				boolean moveInsideViewport) {
			if (sender == DoclogView.this || !moveInsideViewport) {
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
									.getData()) == Doclog.class) {
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
						TreeItem[] treeItems = DoclogView.this.treeViewer
								.getTree().getItems();

						try {
							List<TreePath> idIntersectingDoclogRecords;
							if (!this.hasIDiffNodes(treeItems)) {
								idIntersectingDoclogRecords = DoclogViewer
										.getItemsOfIntersectingDataRanges(
												treeItems, dataRanges);
							} else {
								idIntersectingDoclogRecords = DoclogViewer
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
					DoclogView.this.treeViewer.setSelection(treeSelection);
				}
			});
		}
	};

	private final SUACorePreferenceUtil preferenceUtil = new SUACorePreferenceUtil();
	private TreeViewer treeViewer;

	private DateRangeFilter dateRangeFilter = null;

	protected static final DateFormat dateFormat = new SimpleDateFormat(
			SUACorePreferenceInitializer.DEFAULT_SMART_DATETIME);
	public static final String timeDifferenceFormat = new SUACorePreferenceUtil()
			.getTimeDifferenceFormat();

	private Set<IIdentifier> loadedIdentifiers;

	public DoclogView() {
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

		this.treeViewer = new DoclogViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL, dateFormat, timeDifferenceFormat);
		final Tree table = this.treeViewer.getTree();
		table.setHeaderVisible(true);
		table.setLinesVisible(false);
		this.treeViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {
					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						if (DoclogView.this.highlightService != null
								&& DoclogView.this.getSite()
										.getWorkbenchWindow().getActivePage()
										.getActivePart() == DoclogView.this) {
							DoclogView.this.highlightService.highlight(
									DoclogView.this, event.getSelection(),
									false);
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
		if (this.treeViewer != null
				&& !this.treeViewer.getControl().isDisposed()) {
			this.treeViewer.getControl().setFocus();
			this.load(this.workSessionService.getCurrentWorkSession());
		}
	}

	@Override
	public void dateRangeChanged(TimeZoneDateRange oldDateRange,
			final TimeZoneDateRange newDateRange) {
		ExecUtils.asyncExec(new Runnable() {
			@Override
			public void run() {
				if (DoclogView.this.dateRangeFilter != null) {
					DoclogView.this.treeViewer
							.removeFilter(DoclogView.this.dateRangeFilter);
				}
				DoclogView.this.dateRangeFilter = new DateRangeFilter(
						newDateRange);
				DoclogView.this.treeViewer
						.addFilter(DoclogView.this.dateRangeFilter);
			}
		});
	}

	/**
	 * Opens the given {@link IIdentifier}s in the {@link DoclogViewer}. If the
	 * corresponding {@link Doclog}s could be successfully opened a caller
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
				return ExecUtils.asyncExec(success);
			} else {
				return null;
			}
		}

		return ExecUtils.nonUIAsyncExec(new Callable<T>() {
			@Override
			public T call() throws Exception {
				for (NamedJob loader : ExecUtils.nonUIAsyncExecMerged(
						DoclogView.class,
						"Loading " + StringUtils.join(identifiers, ", "),
						identifiers,
						new ExecUtils.ParametrizedCallable<IIdentifier, NamedJob>() {
							@Override
							public NamedJob call(final IIdentifier identifier)
									throws Exception {
								NamedJob doclogFileLoader = new NamedJob(
										DoclogView.class, "Loading "
												+ Doclog.class.getSimpleName()
												+ "s ...") {
									@Override
									protected IStatus runNamed(
											IProgressMonitor progressMonitor) {
										SubMonitor monitor = SubMonitor
												.convert(progressMonitor);
										monitor.beginTask("... for"
												+ identifier, 1);
										Doclog doclog = Activator
												.getDefault()
												.getDoclogContainer()
												.getDoclogFile(identifier,
														monitor.newChild(1));
										synchronized (newOpenedDoclogFiles) {
											if (doclog != null) {
												newOpenedDoclogFiles.put(
														identifier, doclog);
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
						})) {
					try {
						loader.join();
					} catch (InterruptedException e) {
						LOGGER.error("Error loading "
								+ Doclog.class.getSimpleName());
					}
				}

				DoclogView.this.loadedIdentifiers = identifiers;

				if (DoclogView.this.treeViewer != null
						&& DoclogView.this.treeViewer.getTree() != null
						&& !DoclogView.this.treeViewer.getTree().isDisposed()
						&& newOpenedDoclogFiles.size() > 0) {
					DoclogView.this.openedDoclogFiles = newOpenedDoclogFiles;
					final String partName = "Doclogs - "
							+ StringUtils.join(newOpenedDoclogFiles.keySet(),
									", ");
					ExecUtils.asyncExec(new Runnable() {
						@Override
						public void run() {
							DoclogView.this.setPartName(partName);
							List<URI> uris = new ArrayList<URI>();
							for (Iterator<Doclog> iterator = newOpenedDoclogFiles
									.values().iterator(); iterator.hasNext();) {
								Doclog doclog = iterator.next();
								uris.add(doclog.getUri());
							}
							DoclogView.this.treeViewer.setInput(uris
									.toArray(new URI[uris.size()]));
							DoclogView.this.treeViewer.expandAll();
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

	/**
	 * Loads the {@link Doclog}s from the given {@link IWorkSession}.
	 * 
	 * @param workSession
	 */
	public void load(IWorkSession workSession) {
		if (workSession != null) {
			final Set<IIdentifier> identifiers = new HashSet<IIdentifier>();
			identifiers.addAll(ArrayUtils.getAdaptableObjects(
					workSession.getEntities(), IIdentifier.class));
			DoclogView.this.open(identifiers, null);
		}
	}
}
