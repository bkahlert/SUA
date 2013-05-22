package de.fu_berlin.imp.seqan.usability_analyzer.diff.viewer;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
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

import com.bkahlert.devel.nebula.viewer.SortableTreeViewer;
import com.bkahlert.devel.rcp.selectionUtils.retriever.ISelectionRetriever;
import com.bkahlert.devel.rcp.selectionUtils.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiff;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffs;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.impl.Diff;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.impl.DiffRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.impl.Diffs;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.ui.DiffLabelProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.EpisodeRenderer;

public class DiffListsViewer extends SortableTreeViewer {
	private LocalResourceManager resources;

	private SUACorePreferenceUtil preferenceUtil = new SUACorePreferenceUtil();

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

		final ISelectionRetriever<ILocatable> locatableRetriever = SelectionRetrieverFactory
				.getSelectionRetriever(ILocatable.class);

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
								&& locatableRetriever.getSelection().size() > 0) {
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
				new ColumnLabelProvider() {
					private DiffLabelProvider diffLabelProvider = new DiffLabelProvider();

					@Override
					public String getText(Object element) {
						// we do not use diffLabelProvider here because we want
						// to provide more detailed information
						if (element instanceof IDiffs) {
							IDiffs diffList = (IDiffs) element;
							IIdentifier identifier = null;
							if (diffList.length() > 0) {
								identifier = diffList.get(0).getIdentifier();
							}
							return (identifier != null) ? identifier.toString()
									: "";
						}
						if (element instanceof IDiff) {
							Diff diff = (Diff) element;
							TimeZoneDate date = diff.getDateRange()
									.getEndDate();
							return (date != null) ? date.format(dateFormat)
									: "";
						}
						if (element instanceof IDiffRecord) {
							IDiffRecord diffRecord = (IDiffRecord) element;
							String name = diffRecord.getFilename();
							return (name != null) ? name : "";
						}
						return "";
					}

					@Override
					public Image getImage(Object element) {
						return this.diffLabelProvider.getImage(element);
					}
				});

		TreeViewerColumn episodeColumn = this.createColumn("", 12);
		episodeColumn.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
			}
		});
		new EpisodeRenderer(this, episodeColumn, 1).activateRendering();

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
		passedColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IDiff) {
					Diff diff = (Diff) element;
					Long milliSecondsPassed = diff.getDateRange()
							.getDifference();
					return (milliSecondsPassed != null) ? DurationFormatUtils
							.formatDuration(milliSecondsPassed,
									timeDifferenceFormat, true) : "unknown";
				}
				if (element instanceof IDiffRecord) {
					DiffRecord diffRecord = (DiffRecord) element;
					Long milliSecondsPassed = diffRecord.getDateRange()
							.getDifference();
					return (milliSecondsPassed != null) ? DurationFormatUtils
							.formatDuration(milliSecondsPassed,
									timeDifferenceFormat, true) : "unknown";
				}
				return "";
			}
		});

		TreeViewerColumn revisionColumn = this.createColumn("Revision", 65);
		revisionColumn.getColumn().setAlignment(SWT.RIGHT);
		revisionColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IDiffs) {
					IDiffs diffList = (IDiffs) element;
					return "# " + diffList.length();
				}
				if (element instanceof IDiff) {
					IDiff diff = (IDiff) element;
					Long revision = diff.getRevision();
					return (revision != null) ? revision.toString() : "";
				}
				if (element instanceof IDiffRecord) {
					return "";
				}
				return "";
			}
		});

		this.createColumn("", 10, false, new Comparator<Object>() {
			@Override
			public int compare(Object o1, Object o2) {
				if (o1 instanceof IDiff && o2 instanceof IDiff) {
					IDiff diffFile1 = (IDiff) o1;
					IDiff diffFile2 = (IDiff) o2;
					Long revision1 = diffFile1.getRevision();
					Long revision2 = diffFile2.getRevision();
					return revision1.compareTo(revision2);
				} else if (o1 instanceof IDiffRecord
						&& o2 instanceof IDiffRecord) {
					IDiffRecord diffFileRecord1 = (IDiffRecord) o1;
					IDiffRecord diffFileRecord2 = (IDiffRecord) o2;
					Boolean sourceExists1 = diffFileRecord1.sourceExists();
					Boolean sourceExists2 = diffFileRecord2.sourceExists();
					return sourceExists1.compareTo(sourceExists2);
				}
				return 0;
			}
		}, new Class<?>[] { Diff.class, DiffRecord.class }).setLabelProvider(
				new ColumnLabelProvider() {
					SUACorePreferenceUtil preferenceUtil = new SUACorePreferenceUtil();

					@Override
					public String getText(Object element) {
						return "";
					}

					@Override
					public Color getBackground(Object element) {
						if (element instanceof IDiff) {
							IDiff diff = (IDiff) element;
							RGB backgroundRgb = diff.sourcesExist() ? this.preferenceUtil
									.getColorOk() : this.preferenceUtil
									.getColorMissing();
							return DiffListsViewer.this.resources
									.createColor(backgroundRgb);
						}
						if (element instanceof IDiffRecord) {
							IDiffRecord diffRecord = (IDiffRecord) element;
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
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						TimeZoneDateRange range = null;
						if (element instanceof IDiffs) {
							Diffs diffs = (Diffs) element;
							range = diffs.getDateRange();
						}
						if (element instanceof IDiff) {
							Diff diff = (Diff) element;
							range = diff.getDateRange();
						}
						if (element instanceof IDiffRecord) {
							DiffRecord diffRecord = (DiffRecord) element;
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
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						TimeZoneDateRange range = null;
						if (element instanceof IDiffs) {
							Diffs diffs = (Diffs) element;
							range = diffs.getDateRange();
						}
						if (element instanceof IDiff) {
							Diff diff = (Diff) element;
							range = diff.getDateRange();
						}
						if (element instanceof IDiffRecord) {
							DiffRecord diffRecord = (DiffRecord) element;
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
			TreeItem[] treeItems, TimeZoneDateRange[] dataRanges) {
		List<TreePath> treePaths = new ArrayList<TreePath>();
		for (Item item : com.bkahlert.devel.nebula.utils.ViewerUtils
				.getItemWithDataType(treeItems, IDiffRecord.class)) {
			IDiffRecord diffRecord = (IDiffRecord) item.getData();
			for (TimeZoneDateRange dateRange : dataRanges) {
				if (dateRange.isIntersected(diffRecord.getDateRange())) {
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
			TimeZoneDateRange[] dataRanges) {
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
