package de.fu_berlin.imp.seqan.usability_analyzer.diff.views;

import java.io.FileFilter;
import java.text.DateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExecutableExtensionFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

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
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFile;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFileList;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFileRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.preferences.SUADiffPreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.ui.widgets.FileFilterComposite;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.viewer.DiffFileListsContentProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.viewer.DiffFileListsViewer;
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
	private DiffFileListsViewer diffFileListsViewer;
	private Map<ID, Job> diffFileLoaders = Collections
			.synchronizedMap(new HashMap<ID, Job>(2));
	private IWorkSessionListener workSessionListener = new IWorkSessionListener() {
		@Override
		public void IWorkSessionStarted(IWorkSession workSession) {
			final List<ID> ids = ArrayUtils.getAdaptableObjects(workSession
					.getEntities().toArray(), ID.class);
			open(new HashSet<ID>(ids), null);
		}
	};
	private HashMap<ID, DiffFileList> openedDiffFileLists = new HashMap<ID, DiffFileList>();

	private DateRangeFilter dateRangeFilter = null;
	private HashMap<FileFilter, DiffFileListsViewerFileFilter> diffFileListsViewerFileFilters = new HashMap<FileFilter, DiffFileListsViewerFileFilter>();

	protected static final DateFormat dateFormat = new SUACorePreferenceUtil()
			.getDateFormat();

	protected static final String timeDifferenceFormat = new SUACorePreferenceUtil()
			.getTimeDifferenceFormat();

	private IWorkSessionService workSessionService;

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
		tree.setLinesVisible(false);

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
							DiffFileEditorUtils
									.closeCompareEditors(diffFileRecord);
							DiffFileEditorUtils
									.openCompareEditor(diffFileRecord);
						}
					}
				});

		this.dateRangeChanged(null, preferenceUtil.getDateRange());
		for (FileFilter fileFilter : diffPreferenceUtil.getFileFilters()) {
			this.fileFilterAdded(fileFilter);
		}

		new ContextMenu(diffFileListsViewer, getSite()) {
			@Override
			protected String getDefaultCommandID() {
				return null;
			}
		};
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
	 * Opens the given {@link ID}s in the {@link DiffFileListsViewer}. If the
	 * corresponding {@link DiffFileList}s could be successfully opened a caller
	 * defined {@link Runnable} gets executed.
	 * <p>
	 * Note: The {@link Runnable} is executed in the UI thread.
	 * 
	 * @param ids
	 * @param success
	 */
	public void open(final Set<ID> ids, final Runnable success) {
		final HashMap<ID, DiffFileList> newOpenedDiffFileLists = new HashMap<ID, DiffFileList>();

		// do not load already opened diff file list
		for (ID openedID : openedDiffFileLists.keySet()) {
			if (ids.contains(openedID)) {
				newOpenedDiffFileLists.put(openedID,
						openedDiffFileLists.get(openedID));
				ids.remove(openedID);
			}
		}

		// ids only contains not yet loaded ids
		final int alreadyLoaded = newOpenedDiffFileLists.size();

		if (ids.size() == 0 && success != null)
			success.run();

		// load not yet loaded diff file lists
		for (final ID id : ids) {
			if (diffFileLoaders.containsKey(id))
				continue;

			Job diffFileLoader = new Job("Loading "
					+ DiffFile.class.getSimpleName() + "s") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					DiffFileList diffFileList = Activator.getDefault()
							.getDiffFileDirectory().getDiffFiles(id, monitor);
					synchronized (newOpenedDiffFileLists) {
						newOpenedDiffFileLists.put(id, diffFileList);
					}
					return Status.OK_STATUS;
				}
			};
			diffFileLoader.addJobChangeListener(new JobChangeAdapter() {
				@Override
				public void done(IJobChangeEvent event) {
					boolean jobsFinished;
					synchronized (newOpenedDiffFileLists) {
						jobsFinished = newOpenedDiffFileLists.size() == ids
								.size() + alreadyLoaded;
					}
					if (jobsFinished && event.getResult() == Status.OK_STATUS) {
						Display.getDefault().syncExec(new Runnable() {
							@Override
							public void run() {
								openedDiffFileLists = newOpenedDiffFileLists;
								setPartName("Diffs - "
										+ StringUtils.join(
												newOpenedDiffFileLists.keySet(),
												", "));
								if (diffFileListsViewer != null
										&& !diffFileListsViewer.getTree()
												.isDisposed()
										&& newOpenedDiffFileLists.size() > 0) {
									diffFileListsViewer
											.setInput(newOpenedDiffFileLists
													.values());
									diffFileListsViewer.expandAll();
									if (success != null)
										success.run();
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
