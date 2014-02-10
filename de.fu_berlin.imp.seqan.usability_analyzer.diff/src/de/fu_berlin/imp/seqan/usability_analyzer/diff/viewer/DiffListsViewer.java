package de.fu_berlin.imp.seqan.usability_analyzer.diff.viewer;

import java.net.URI;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.nebula.utils.StringUtils;
import com.bkahlert.devel.nebula.viewer.SortableTreeViewer;
import com.bkahlert.devel.rcp.selectionUtils.retriever.SelectionRetrieverFactory;
import com.bkahlert.nebula.datetime.CalendarRange;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.ILabelProviderService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.ILocatorService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.URIUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiff;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecord.FLAGS;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffs;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.impl.Diff;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.impl.DiffRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.ui.DiffLabelProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.EpisodeRenderer;

public class DiffListsViewer extends SortableTreeViewer {
	private static final Logger LOGGER = Logger
			.getLogger(DiffListsViewer.class);

	private final LocalResourceManager resources;

	private final SUACorePreferenceUtil preferenceUtil = new SUACorePreferenceUtil();

	private final ILocatorService locatorService = (ILocatorService) PlatformUI
			.getWorkbench().getService(ILocatorService.class);

	public DiffListsViewer(final Composite parent, int style,
			DateFormat dateFormat, String timeDifferenceFormat) {
		super(parent, style);

		this.resources = new LocalResourceManager(
				JFaceResources.getResources(), parent);
		parent.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				DiffListsViewer.this.resources.dispose();
			}
		});

		this.initColumns(dateFormat, timeDifferenceFormat);

		int operations = DND.DROP_LINK;
		Transfer[] transferTypes = new Transfer[] { LocalSelectionTransfer
				.getTransfer() };
		this.addDragSupport(operations, transferTypes,
				new DragSourceListener() {
					@Override
					public void dragStart(DragSourceEvent event) {
						boolean episodeRendererActive = DiffListsViewer.this
								.getControl().getData(
										EpisodeRenderer.CONTROL_DATA_STRING) != null;
						if (!episodeRendererActive
								&& SelectionRetrieverFactory
										.getSelectionRetriever(URI.class)
										.getSelection().size() > 0) {
							LocalSelectionTransfer.getTransfer().setSelection(
									DiffListsViewer.this.getSelection());
							LocalSelectionTransfer.getTransfer()
									.setSelectionSetTime(
											event.time & 0xFFFFFFFFL);
							event.doit = true;
						} else {
							event.doit = false;
						}
					};

					@Override
					public void dragSetData(DragSourceEvent event) {
						if (LocalSelectionTransfer.getTransfer()
								.isSupportedType(event.dataType)) {
							event.data = LocalSelectionTransfer.getTransfer()
									.getSelection();
						}
					}

					@Override
					public void dragFinished(DragSourceEvent event) {
						LocalSelectionTransfer.getTransfer().setSelection(null);
						LocalSelectionTransfer.getTransfer()
								.setSelectionSetTime(0);
					}
				});

		this.sort(3);
	}

	private void initColumns(final DateFormat dateFormat,
			final String timeDifferenceFormat) {

		this.createColumn("Date", 350).setLabelProvider(
				new ILabelProviderService.StyledColumnLabelProvider() {
					private final DiffLabelProvider diffLabelProvider = new DiffLabelProvider();

					@Override
					public String getText(URI uri) throws Exception {
						Class<? extends ILocatable> type = DiffListsViewer.this.locatorService
								.getType(uri);

						// we do not use diffLabelProvider here because we want
						// to provide more detailed information
						if (type == IDiffs.class) {
							return URIUtils.getIdentifier(uri).toString();
						}
						if (type == IDiff.class) {
							IDiff diff = DiffListsViewer.this.locatorService
									.resolve(uri, IDiff.class, null).get();
							TimeZoneDate date = diff.getDateRange()
									.getEndDate();
							return (date != null) ? date.format(dateFormat)
									: "";
						}
						if (type == IDiffRecord.class) {
							IDiffRecord diffRecord = DiffListsViewer.this.locatorService
									.resolve(uri, IDiffRecord.class, null)
									.get();
							String name = diffRecord.getFilename();
							return (name != null) ? name : "";
						}
						return "ERROR";
					}

					@Override
					public Image getImage(URI uri) throws Exception {
						return this.diffLabelProvider.getImage(uri);
					}
				});

		TreeViewerColumn episodeColumn = this.createColumn("", 12);
		episodeColumn.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
			}
		});
		new EpisodeRenderer(this, episodeColumn, 1).activateRendering();

		this.createColumn("IDE", 30).setLabelProvider(
				new ILabelProviderService.StyledColumnLabelProvider() {
					@Override
					public String getText(URI uri) throws Exception {
						Class<? extends ILocatable> type = DiffListsViewer.this.locatorService
								.getType(uri);

						if (type == IDiff.class) {
							IDiff diff = DiffListsViewer.this.locatorService
									.resolve(uri, IDiff.class, null).get();
							return diff.getLocationHash();
						}
						if (type == IDiffRecord.class) {
							IDiffRecord diffRecord = DiffListsViewer.this.locatorService
									.resolve(uri, IDiffRecord.class, null)
									.get();
							return diffRecord.getDiffFile().getLocationHash();
						}

						return "";
					}
				});

		this.createColumn("Flags", 30).setLabelProvider(
				new ILabelProviderService.StyledColumnLabelProvider() {
					@Override
					public String getText(URI uri) throws Exception {
						Class<? extends ILocatable> type = DiffListsViewer.this.locatorService
								.getType(uri);

						if (type == IDiffRecord.class) {
							IDiffRecord diffRecord = DiffListsViewer.this.locatorService
									.resolve(uri, IDiffRecord.class, null)
									.get();
							List<String> flags = new LinkedList<String>();
							if (diffRecord.hasFlag(FLAGS.RESTORED)) {
								flags.add("R");
							}
							return StringUtils.join(flags, " ");
						}

						return "";
					}
				});

		TreeViewerColumn passedColumn = this.createColumn("Passed", 90, true,
				new Comparator<Object>() {
					@Override
					public int compare(Object arg0, Object arg1) {
						Long l1 = (Long) arg0;
						Long l2 = (Long) arg1;
						if (l1 != null) {
							return l1.compareTo(l2);
						}
						return 0;
					}
				}, new Class<?>[] { Long.class });
		passedColumn.getColumn().setAlignment(SWT.RIGHT);
		passedColumn
				.setLabelProvider(new ILabelProviderService.StyledColumnLabelProvider() {
					@Override
					public String getText(URI uri) throws Exception {
						Class<? extends ILocatable> type = DiffListsViewer.this.locatorService
								.getType(uri);

						if (type == IDiff.class) {
							IDiff diff = DiffListsViewer.this.locatorService
									.resolve(uri, IDiff.class, null).get();
							Long milliSecondsPassed = diff.getDateRange()
									.getDifference();
							return (milliSecondsPassed != null) ? DurationFormatUtils
									.formatDuration(milliSecondsPassed,
											timeDifferenceFormat, true)
									: "unknown";
						}
						if (type == IDiffRecord.class) {
							IDiffRecord diffRecord = DiffListsViewer.this.locatorService
									.resolve(uri, IDiffRecord.class, null)
									.get();
							Long milliSecondsPassed = diffRecord.getDateRange()
									.getDifference();
							return (milliSecondsPassed != null) ? DurationFormatUtils
									.formatDuration(milliSecondsPassed,
											timeDifferenceFormat, true)
									: "unknown";
						}

						return "";
					}
				});

		TreeViewerColumn revisionColumn = this.createColumn("Revision", 65);
		revisionColumn.getColumn().setAlignment(SWT.RIGHT);
		revisionColumn
				.setLabelProvider(new ILabelProviderService.StyledColumnLabelProvider() {
					@Override
					public String getText(URI uri) throws Exception {
						Class<? extends ILocatable> type = DiffListsViewer.this.locatorService
								.getType(uri);

						if (type == IDiffs.class) {
							IDiffs diffs = DiffListsViewer.this.locatorService
									.resolve(uri, IDiffs.class, null).get();
							return "#" + diffs.length();
						}
						if (type == IDiff.class) {
							IDiff diff = DiffListsViewer.this.locatorService
									.resolve(uri, IDiff.class, null).get();
							return diff.getCalculatedRevision() + "";
						}

						return "";
					}
				});

		this.createColumn("", 10, false, new Comparator<Object>() {
			@Override
			public int compare(Object o1, Object o2) {
				if (!(o1 instanceof URI) || !(o2 instanceof URI)) {
					return 0;
				}

				URI uri1 = (URI) o1;
				URI uri2 = (URI) o2;
				Class<? extends ILocatable> type1 = DiffListsViewer.this.locatorService
						.getType(uri1);
				Class<? extends ILocatable> type2 = DiffListsViewer.this.locatorService
						.getType(uri2);

				if (type1 == IDiff.class && type2 == IDiff.class) {
					String revision1 = URIUtils.getTrail(uri1).get(0);
					String revision2 = URIUtils.getTrail(uri2).get(0);
					return revision1.compareTo(revision2);
				} else if (type1 == IDiffRecord.class
						&& type2 == IDiffRecord.class) {
					try {
						IDiffRecord diffRecord1 = DiffListsViewer.this.locatorService
								.resolve(uri1, IDiffRecord.class, null).get();
						IDiffRecord diffRecord2 = DiffListsViewer.this.locatorService
								.resolve(uri2, IDiffRecord.class, null).get();
						Boolean sourceExists1 = diffRecord1.sourceExists();
						Boolean sourceExists2 = diffRecord2.sourceExists();
						return sourceExists1.compareTo(sourceExists2);
					} catch (Exception e) {
						LOGGER.error(
								"Error comparing " + uri1 + " and " + uri2, e);
					}
				}

				return 0;
			}
		}, new Class<?>[] { Diff.class, DiffRecord.class }).setLabelProvider(
				new ILabelProviderService.StyledColumnLabelProvider() {
					SUACorePreferenceUtil preferenceUtil = new SUACorePreferenceUtil();

					@Override
					public String getText(URI uri) throws Exception {
						return "";
					}

					@Override
					public Color getBackground(URI uri) throws Exception {
						Class<? extends ILocatable> type = DiffListsViewer.this.locatorService
								.getType(uri);

						if (type == IDiff.class) {
							IDiff diff = DiffListsViewer.this.locatorService
									.resolve(uri, IDiff.class, null).get();
							RGB backgroundRgb = diff.sourcesExist() ? this.preferenceUtil
									.getColorOk() : this.preferenceUtil
									.getColorMissing();
							return DiffListsViewer.this.resources
									.createColor(backgroundRgb);
						}
						if (type == IDiffRecord.class) {
							IDiffRecord diffRecord = DiffListsViewer.this.locatorService
									.resolve(uri, IDiffRecord.class, null)
									.get();
							RGB backgroundRgb = diffRecord.sourceExists() ? this.preferenceUtil
									.getColorOk() : this.preferenceUtil
									.getColorMissing();
							return DiffListsViewer.this.resources
									.createColor(backgroundRgb);
						}

						return null;
					}
				});

		this.createColumn("Start", 175).setLabelProvider(
				new ILabelProviderService.StyledColumnLabelProvider() {
					@Override
					public String getText(URI uri) throws Exception {
						Class<? extends ILocatable> type = DiffListsViewer.this.locatorService
								.getType(uri);

						TimeZoneDateRange range = null;
						if (type == IDiffs.class) {
							IDiffs diffs = DiffListsViewer.this.locatorService
									.resolve(uri, IDiffs.class, null).get();
							range = diffs.getDateRange();
						}
						if (type == IDiff.class) {
							IDiff diff = DiffListsViewer.this.locatorService
									.resolve(uri, IDiff.class, null).get();
							range = diff.getDateRange();
						}
						if (type == IDiffRecord.class) {
							IDiffRecord diffRecord = DiffListsViewer.this.locatorService
									.resolve(uri, IDiffRecord.class, null)
									.get();
							range = diffRecord.getDateRange();
						}

						TimeZoneDate date = range != null ? range
								.getStartDate() : null;
						return date != null ? date
								.format(DiffListsViewer.this.preferenceUtil
										.getDateFormat()) : "";
					}
				});
		this.createColumn("End", 175).setLabelProvider(
				new ILabelProviderService.StyledColumnLabelProvider() {
					@Override
					public String getText(URI uri) throws Exception {
						Class<? extends ILocatable> type = DiffListsViewer.this.locatorService
								.getType(uri);

						TimeZoneDateRange range = null;
						if (type == IDiffs.class) {
							IDiffs diffs = DiffListsViewer.this.locatorService
									.resolve(uri, IDiffs.class, null).get();
							range = diffs.getDateRange();
						}
						if (type == IDiff.class) {
							IDiff diff = DiffListsViewer.this.locatorService
									.resolve(uri, IDiff.class, null).get();
							range = diff.getDateRange();
						}
						if (type == IDiffRecord.class) {
							IDiffRecord diffRecord = DiffListsViewer.this.locatorService
									.resolve(uri, IDiffRecord.class, null)
									.get();
							range = diffRecord.getDateRange();
						}

						TimeZoneDate date = range != null ? range.getEndDate()
								: null;
						return date != null ? date
								.format(DiffListsViewer.this.preferenceUtil
										.getDateFormat()) : "";
					}
				});
	}

	/**
	 * Returns the {@link TreePath}s that describe {@link IDiffRecord}
	 * fulfilling the following criteria:
	 * <ol>
	 * <li>{@link IDiffRecord}'s {@link TimeZoneDateRange} intersects one of the
	 * given {@link TimeZoneDateRange}s
	 * </ol>
	 * 
	 * @param treeItems
	 * @param dataRanges
	 * @return
	 */
	public static List<TreePath> getItemsOfIntersectingDataRanges(
			TreeItem[] treeItems, CalendarRange[] dataRanges) {
		List<TreePath> treePaths = new ArrayList<TreePath>();
		for (Item item : com.bkahlert.devel.nebula.utils.ViewerUtils
				.getItemWithDataType(treeItems, IDiffRecord.class)) {
			IDiffRecord diffRecord = (IDiffRecord) item.getData();
			for (CalendarRange dateRange : dataRanges) {
				if (dateRange.isIntersected(diffRecord.getDateRange()
						.getCalendarRange())) {
					treePaths.add(new TreePath(new Object[] { diffRecord }));
					break;
				}
			}
		}
		return treePaths;
	}

	/**
	 * Returns the {@link TreePath}s that describe {@link IDiffRecord}
	 * fulfilling the following criteria:
	 * <ol>
	 * <li>{@link IDiffRecord} belongs to a {@link IDiffs} with the given
	 * {@link IIdentifier}
	 * <li>{@link DoclocRecord}'s {@link TimeZoneDateRange} intersects one of
	 * the given {@link TimeZoneDateRange}s
	 * </ol>
	 * 
	 * @param treeItems
	 * @param identifier
	 * @param dataRanges
	 * @return
	 */
	public static List<TreePath> getItemsOfIdIntersectingDataRanges(
			TreeItem[] treeItems, IIdentifier identifier,
			CalendarRange[] dataRanges) {
		List<TreePath> treePaths = new ArrayList<TreePath>();
		for (Item item : com.bkahlert.devel.nebula.utils.ViewerUtils
				.getItemWithDataType(treeItems, IDiffs.class)) {
			IDiffs diffs = (IDiffs) item.getData();
			if (identifier.equals(diffs.getIdentifier())) {
				List<TreePath> childTreePaths = DiffListsViewer
						.getItemsOfIntersectingDataRanges(
								((TreeItem) item).getItems(), dataRanges);
				for (TreePath childTreePath : childTreePaths) {
					TreePath treePath = com.bkahlert.devel.nebula.utils.ViewerUtils
							.merge(new TreePath(new Object[] { diffs }),
									childTreePath);
					treePaths.add(treePath);
				}
			}
		}
		return treePaths;
	}

}
