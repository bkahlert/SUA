package de.fu_berlin.imp.seqan.usability_analyzer.doclog.views;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExecutableExtensionFactory;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;
import com.bkahlert.devel.rcp.selectionUtils.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.seqan.usability_analyzer.core.extensionPoints.IDateRangeListener;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.SortableTreeViewer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.DateRangeFilter;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFile;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFileList;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFileManager;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.views.DiffExplorerView;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogAction;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogFile;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogScreenshot.Status;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.viewer.DoclogExplorerContentProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.viewer.IdDurationViewerFilter;

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

	private ISelectionListener selectionListener = new ISelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (!part.getClass().equals(DoclogExplorerView.class)
					&& !part.getSite().getId().contains("DiffExplorerView")) {

				List<DoclogFile> doclogFiles = SelectionRetrieverFactory
						.getSelectionRetriever(DoclogFile.class).getSelection();
				if (sortableTreeViewer != null) {
					sortableTreeViewer.setInput(doclogFiles);
					sortableTreeViewer.expandAll();
				}
			}
		}
	};

	private ISelectionListener diffSelectionListener = new ISelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			List<DiffFile> diffFiles = SelectionRetrieverFactory
					.getSelectionRetriever(DiffFile.class).getSelection(
							DiffExplorerView.ID);

			Map<ID, DiffFileList> groupedDiffFiles = DiffFileManager
					.group(diffFiles);

			List<ViewerFilter> filters = new ArrayList<ViewerFilter>();
			for (ID id : groupedDiffFiles.keySet()) {
				final List<DateRange> dateRanges = new ArrayList<DateRange>();
				for (DiffFile diffFile : groupedDiffFiles.get(id)) {
					long start = diffFile.getDate().getTime();
					long end = start + diffFile.getMillisecondsPassed();
					dateRanges.add(new DateRange(start, end));
				}
				filters.add(new IdDurationViewerFilter(id, dateRanges));
			}
			sortableTreeViewer.setFilters(filters.toArray(new ViewerFilter[0]));
		}
	};

	private LocalResourceManager resources;
	private SUACorePreferenceUtil preferenceUtil = new SUACorePreferenceUtil();
	private SortableTreeViewer sortableTreeViewer;

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
		SelectionUtils.getSelectionService().addSelectionListener(
				selectionListener);
		try {
			SelectionUtils.getSelectionService().addSelectionListener(
					DiffExplorerView.ID, diffSelectionListener);
		} catch (NoClassDefFoundError e) {
			// no optional diff plugin
		}
	}

	@Override
	public void dispose() {
		resources.dispose();
		try {
			SelectionUtils.getSelectionService().removeSelectionListener(
					DiffExplorerView.ID, diffSelectionListener);
		} catch (NoClassDefFoundError e) {
			// no optional diff plugin
		}
		SelectionUtils.getSelectionService().removeSelectionListener(
				selectionListener);
		super.dispose();
	}

	@Override
	public void createPartControl(Composite parent) {
		this.resources = new LocalResourceManager(
				JFaceResources.getResources(), parent);

		parent.setLayout(new FillLayout());

		this.sortableTreeViewer = new SortableTreeViewer(parent, SWT.MULTI
				| SWT.H_SCROLL | SWT.V_SCROLL);
		final Tree table = sortableTreeViewer.getTree();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		this.sortableTreeViewer
				.setContentProvider(new DoclogExplorerContentProvider());

		this.sortableTreeViewer.createColumn("Date", 160).setLabelProvider(
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						if (element instanceof DoclogFile) {
							DoclogFile doclogFile = (DoclogFile) element;
							if (doclogFile.getId() != null)
								return doclogFile.getId().toString();
							else
								return doclogFile.getFingerprint().toString();
						}
						if (element instanceof DoclogRecord) {
							DoclogRecord doclogRecord = (DoclogRecord) element;
							Date date = doclogRecord.getDate();
							return (date != null) ? dateFormat.format(date)
									: "";
						}
						return "";
					}
				});

		this.sortableTreeViewer.createColumn("Passed", 90, true,
				new Comparator<Object>() {
					@Override
					public int compare(Object arg0, Object arg1) {
						Long l1 = (Long) arg0;
						Long l2 = (Long) arg1;
						if (l1 != null)
							return l1.compareTo(l2);
						return 0;
					}
				}, new Class<?>[] { Long.class }).setLabelProvider(
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						if (element instanceof DoclogRecord) {
							DoclogRecord doclogRecord = (DoclogRecord) element;
							Long milliSecondsPassed = doclogRecord
									.getMillisecondsPassed();
							return (milliSecondsPassed != null) ? DurationFormatUtils
									.formatDuration(milliSecondsPassed,
											timeDifferenceFormat, true) : "";
						}
						return "";
					}
				});

		this.sortableTreeViewer.createColumn("", 10, false,
				new Comparator<Object>() {
					@Override
					public int compare(Object o1, Object o2) {
						if (o1 instanceof DoclogFile
								&& o2 instanceof DoclogFile) {
							DoclogFile doclogFile1 = (DoclogFile) o1;
							DoclogFile doclogFile2 = (DoclogFile) o2;
							Status status1 = doclogFile1.getScreenshotStatus();
							Status status2 = doclogFile2.getScreenshotStatus();
							return Integer.valueOf(status1.ordinal())
									.compareTo(status2.ordinal());
						} else if (o1 instanceof DoclogRecord
								&& o2 instanceof DoclogRecord) {
							DoclogRecord doclogRecord1 = (DoclogRecord) o1;
							DoclogRecord doclogRecord2 = (DoclogRecord) o2;
							Status status1 = doclogRecord1.getScreenshot()
									.getStatus();
							Status status2 = doclogRecord2.getScreenshot()
									.getStatus();
							return Integer.valueOf(status1.ordinal())
									.compareTo(status2.ordinal());
						}
						return 0;
					}
				}, new Class<?>[] { DoclogFile.class, DoclogRecord.class })
				.setLabelProvider(new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						return "";
					}

					@Override
					public Color getBackground(Object element) {
						if (element instanceof DoclogFile) {
							Status worstStatus = ((DoclogFile) element)
									.getScreenshotStatus();
							RGB backgroundRgb = worstStatus.getRGB();
							return resources.createColor(backgroundRgb);
						}
						if (element instanceof DoclogRecord) {
							DoclogRecord doclogRecord = (DoclogRecord) element;
							RGB backgroundRgb = doclogRecord.getScreenshot()
									.getStatus().getRGB();
							return resources.createColor(backgroundRgb);
						}
						return null;
					}
				});

		this.sortableTreeViewer.createColumn("URL", 200).setLabelProvider(
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						if (element instanceof DoclogRecord) {
							DoclogRecord doclogRecord = (DoclogRecord) element;
							String url = doclogRecord.getUrl();
							return (url != null) ? url : "";
						}
						return "";
					}
				});

		this.sortableTreeViewer.createColumn("Action", 60).setLabelProvider(
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						if (element instanceof DoclogRecord) {
							DoclogRecord doclogRecord = (DoclogRecord) element;
							DoclogAction action = doclogRecord.getAction();
							return (action != null) ? action.toString() : "";
						}
						return "";
					}
				});

		this.sortableTreeViewer.createColumn("Param", 50).setLabelProvider(
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						if (element instanceof DoclogRecord) {
							DoclogRecord doclogRecord = (DoclogRecord) element;
							String actionParameter = doclogRecord
									.getActionParameter();
							return (actionParameter != null) ? actionParameter
									: "";
						}
						return "";
					}
				});

		this.sortableTreeViewer.createColumn("Width", 40).setLabelProvider(
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						if (element instanceof DoclogRecord) {
							DoclogRecord doclogRecord = (DoclogRecord) element;
							Point windowDimensions = doclogRecord
									.getWindowDimensions();
							return (windowDimensions != null) ? windowDimensions.x
									+ ""
									: "-";
						}
						return "";
					}
				});

		this.sortableTreeViewer.createColumn("Height", 40).setLabelProvider(
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						if (element instanceof DoclogRecord) {
							DoclogRecord doclogRecord = (DoclogRecord) element;
							Point windowDimensions = doclogRecord
									.getWindowDimensions();
							return (windowDimensions != null) ? windowDimensions.y
									+ ""
									: "-";
						}
						return "";
					}
				});

		this.sortableTreeViewer.createColumn("X", 40).setLabelProvider(
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						if (element instanceof DoclogRecord) {
							DoclogRecord doclogRecord = (DoclogRecord) element;
							Point scrollPosition = doclogRecord
									.getScrollPosition();
							return (scrollPosition != null) ? scrollPosition.x
									+ "" : "-";
						}
						return "";
					}
				});

		this.sortableTreeViewer.createColumn("X", 40).setLabelProvider(
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						if (element instanceof DoclogRecord) {
							DoclogRecord doclogRecord = (DoclogRecord) element;
							Point scrollPosition = doclogRecord
									.getScrollPosition();
							return (scrollPosition != null) ? scrollPosition.y
									+ "" : "-";
						}
						return "";
					}
				});

		this.sortableTreeViewer.sort(0);
		this.dateRangeChanged(null, preferenceUtil.getDateRange());

		hookContextMenu();
		getSite().setSelectionProvider(sortableTreeViewer);
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {

			}
		});
		Menu menu = menuMgr.createContextMenu(sortableTreeViewer.getControl());
		sortableTreeViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, sortableTreeViewer);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dateRangeChanged(DateRange oldDateRange, DateRange newDateRange) {
		if (this.dateRangeFilter != null)
			this.sortableTreeViewer.removeFilter(this.dateRangeFilter);
		this.dateRangeFilter = new DateRangeFilter(newDateRange);
		this.sortableTreeViewer.addFilter(this.dateRangeFilter);
	}

}
