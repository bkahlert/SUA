package de.fu_berlin.imp.seqan.usability_analyzer.doclog.views;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExecutableExtensionFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.bkahlert.devel.rcp.selectionUtils.ArrayUtils;
import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;
import com.bkahlert.devel.rcp.selectionUtils.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.seqan.usability_analyzer.core.extensionPoints.IDateRangeListener;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.IdDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IWorkSession;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IWorkSessionListener;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IWorkSessionService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.DateRangeFilter;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.ExecutorUtil;
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
					if (viewReference.getId().equals(ID))
						return viewReference.getView(true);
				}
			} catch (Exception e) {
				LOGGER.error(e);
			}
			return null;
		}
	}

	private Map<Object, Doclog> openedDoclogFiles = new HashMap<Object, Doclog>();
	private Map<Object, Job> doclogLoaders = new HashMap<Object, Job>();

	private IWorkSessionService workSessionService;
	private IWorkSessionListener workSessionListener = new IWorkSessionListener() {
		@Override
		public void IWorkSessionStarted(IWorkSession workSession) {
			final Set<Object> keys = new HashSet<Object>();
			keys.addAll(ArrayUtils.getAdaptableObjects(workSession
					.getEntities().toArray(), ID.class));
			keys.addAll(ArrayUtils.getAdaptableObjects(workSession
					.getEntities().toArray(), Fingerprint.class));
			open(keys, null);
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
				if (com.bkahlert.devel.nebula.utils.ViewerUtils
						.getItemWithDataType(treeItems, Doclog.class)
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

	private ExecutorService pool = ExecutorUtil
			.newFixedMultipleOfProcessorsThreadPool(1);

	public DoclogExplorerView() {
		this.workSessionService = (IWorkSessionService) PlatformUI
				.getWorkbench().getService(IWorkSessionService.class);
		if (this.workSessionService == null)
			LOGGER.warn("Could not get "
					+ IWorkSessionService.class.getSimpleName());
	}

	public String getId() {
		return ID;
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		if (this.workSessionService != null)
			this.workSessionService.addWorkSessionListener(workSessionListener);
		SelectionUtils.getSelectionService().addPostSelectionListener(
				dateRangePostSelectionListener);
	}

	@Override
	public void dispose() {
		SelectionUtils.getSelectionService().removePostSelectionListener(
				dateRangePostSelectionListener);
		if (this.workSessionService != null)
			this.workSessionService
					.removeWorkSessionListener(workSessionListener);
		super.dispose();
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());

		this.treeViewer = new DoclogFilesViewer(parent, SWT.MULTI
				| SWT.H_SCROLL | SWT.V_SCROLL, dateFormat, timeDifferenceFormat);
		final Tree table = treeViewer.getTree();
		table.setHeaderVisible(true);
		table.setLinesVisible(false);

		this.dateRangeChanged(null, preferenceUtil.getDateRange());

		new ContextMenu(treeViewer, getSite()) {
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
				if (dateRangeFilter != null)
					treeViewer.removeFilter(dateRangeFilter);
				dateRangeFilter = new DateRangeFilter(newDateRange);
				treeViewer.addFilter(dateRangeFilter);
			}
		});
	}

	/**
	 * Opens the given {@link ID}s and {@link Fingerprint}s in the
	 * {@link DoclogFilesViewer}. If the corresponding {@link Doclog}s could
	 * be successfully opened a caller defined {@link Runnable} gets executed.
	 * <p>
	 * Note: The {@link Runnable} is executed in the UI thread.
	 * 
	 * @param keys
	 * @param success
	 */
	public <T> Future<T> open(final Set<Object> keys, final Callable<T> success) {
		for (Object key : keys)
			assert key instanceof ID || key instanceof Fingerprint;

		final HashMap<Object, Doclog> newOpenedDoclogFiles = new HashMap<Object, Doclog>();

		// do not load already opened doclog files
		for (Object openedKey : openedDoclogFiles.keySet()) {
			if (keys.contains(openedKey)) {
				newOpenedDoclogFiles.put(openedKey,
						openedDoclogFiles.get(openedKey));
				keys.remove(openedKey);
			}
		}

		// keys only contains not yet loaded keys
		final int alreadyLoaded = newOpenedDoclogFiles.size();

		// Case 1: no IDs
		if (keys.size() == 0) {
			if (success != null) {
				return ExecutorUtil.asyncExec(success);
			} else
				return null;
		}

		// Case 2: multiple IDs
		return pool.submit(new Callable<T>() {
			@Override
			public T call() throws Exception {
				final AtomicReference<T> r = new AtomicReference<T>();
				final Semaphore mutex = new Semaphore(0);

				// load not yet loaded doclog files
				for (final Object key : keys) {
					if (doclogLoaders.containsKey(key))
						continue;

					Job doclogFileLoader = new Job("Loading "
							+ Doclog.class.getSimpleName() + "s") {
						@Override
						protected IStatus run(IProgressMonitor monitor) {
							Doclog doclog = Activator
									.getDefault()
									.getDoclogDataDirectory()
									.getDoclogFile(key,
											new SubProgressMonitor(monitor, 1));
							synchronized (newOpenedDoclogFiles) {
								if (doclog != null)
									newOpenedDoclogFiles.put(key, doclog);
								else
									keys.remove(key);
							}
							return Status.OK_STATUS;
						};
					};
					doclogFileLoader
							.addJobChangeListener(new JobChangeAdapter() {
								@Override
								public void done(IJobChangeEvent event) {
									boolean jobsFinished;
									synchronized (newOpenedDoclogFiles) {
										jobsFinished = newOpenedDoclogFiles
												.size() == keys.size()
												+ alreadyLoaded;
									}
									if (jobsFinished
											&& event.getResult() == Status.OK_STATUS) {
										try {
											ExecutorUtil
													.syncExec(new Callable<T>() {
														@Override
														public T call()
																throws Exception {
															openedDoclogFiles = newOpenedDoclogFiles;
															setPartName("Doclogs - "
																	+ StringUtils
																			.join(newOpenedDoclogFiles
																					.keySet(),
																					", "));
															if (treeViewer != null
																	&& !treeViewer
																			.getTree()
																			.isDisposed()
																	&& newOpenedDoclogFiles
																			.size() > 0) {
																treeViewer
																		.setInput(newOpenedDoclogFiles
																				.values());
																treeViewer
																		.expandAll();
																if (success != null)
																	return success
																			.call();
															}
															return null;
														}
													});
										} catch (Exception e) {
											LOGGER.error(e);
										}
										mutex.release();
									}
									doclogLoaders.remove(key);
								}
							});
					doclogLoaders.put(key, doclogFileLoader);
					doclogFileLoader.schedule();
				}

				mutex.acquire();
				return r.get();
			}
		});
	}
}
