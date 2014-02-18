package de.fu_berlin.imp.seqan.usability_analyzer.diff.viewer;

import java.net.URI;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
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
import com.bkahlert.nebula.datetime.CalendarRange;
import com.bkahlert.nebula.utils.DNDUtils;
import com.bkahlert.nebula.utils.DNDUtils.Oracle;
import com.bkahlert.nebula.utils.DistributionUtils.AbsoluteWidth;
import com.bkahlert.nebula.utils.DistributionUtils.RelativeWidth;
import com.bkahlert.nebula.utils.Stylers;

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
import de.fu_berlin.imp.seqan.usability_analyzer.diff.ui.DiffLabelProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.EpisodeRenderer;

public class DiffViewer extends SortableTreeViewer {
	private static final Logger LOGGER = Logger.getLogger(DiffViewer.class);

	private final LocalResourceManager resources;

	private final SUACorePreferenceUtil preferenceUtil = new SUACorePreferenceUtil();

	private final static ILocatorService LOCATOR_SERVICE = (ILocatorService) PlatformUI
			.getWorkbench().getService(ILocatorService.class);

	public DiffViewer(final Composite parent, int style, DateFormat dateFormat,
			String timeDifferenceFormat) {
		super(parent, style);

		this.resources = new LocalResourceManager(
				JFaceResources.getResources(), parent);
		parent.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				DiffViewer.this.resources.dispose();
			}
		});

		this.initColumns(dateFormat, timeDifferenceFormat);

		DNDUtils.addLocalDragSupport(this, new Oracle() {
			@Override
			public boolean allowDND() {
				return DiffViewer.this.getControl().getData(
						EpisodeRenderer.CONTROL_DATA_STRING) == null;
			}
		}, URI.class);

		this.sort(0);
	}

	private void initColumns(final DateFormat dateFormat,
			final String timeDifferenceFormat) {

		this.createColumn("Date", new RelativeWidth(.9, 270)).setLabelProvider(
				new ILabelProviderService.StyledLabelProvider() {
					private final DiffLabelProvider diffLabelProvider = new DiffLabelProvider();

					@Override
					public StyledString getStyledText(URI uri) throws Exception {
						Class<? extends ILocatable> type = LOCATOR_SERVICE
								.getType(uri);

						// we do not use diffLabelProvider here
						// because
						// we want
						// to provide more detailed information
						if (type == IDiffs.class) {
							IDiffs diffs = LOCATOR_SERVICE.resolve(uri,
									IDiffs.class, null).get();

							return new StyledString(diffs.getIdentifier()
									.toString()).append(" (" + diffs.length()
									+ ")", Stylers.COUNTER_STYLER);
						}
						if (type == IDiff.class) {
							IDiff diff = LOCATOR_SERVICE.resolve(uri,
									IDiff.class, null).get();

							TimeZoneDate date = diff.getDateRange()
									.getEndDate();
							return new StyledString((date != null) ? date
									.format(dateFormat) : "").append("   Rev. "
									+ diff.getCalculatedRevision() + " @ "
									+ diff.getLocationHash() + "",
									Stylers.MINOR_STYLER);
						}
						if (type == IDiffRecord.class) {
							String text = this.diffLabelProvider
									.getStyledText(uri).toString()
									.replaceAll("\\s*\\(.*?\\)", "");
							return new StyledString(text);
						}
						return new StyledString("ERROR",
								Stylers.ATTENTION_STYLER);
					}

					@Override
					public Image getImage(URI uri) throws Exception {
						return this.diffLabelProvider.getImage(uri);
					}
				});

		TreeViewerColumn episodeColumn = this.createColumn("",
				new RelativeWidth(.1, 22));
		episodeColumn.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
			}
		});
		new EpisodeRenderer(this, episodeColumn, 1).activateRendering();

		this.createColumn("Flags", new AbsoluteWidth(40)).setLabelProvider(
				new DelegatingStyledCellLabelProvider(
						new ILabelProviderService.StyledLabelProvider() {
							@Override
							public StyledString getStyledText(URI uri)
									throws Exception {
								Class<? extends ILocatable> type = LOCATOR_SERVICE
										.getType(uri);

								if (type == IDiffRecord.class) {
									IDiffRecord diffRecord = LOCATOR_SERVICE
											.resolve(uri, IDiffRecord.class,
													null).get();
									List<String> flags = new LinkedList<String>();
									if (diffRecord.hasFlag(FLAGS.RESTORED)) {
										flags.add("R");
									}
									return new StyledString(StringUtils.join(
											flags, " "));
								}

								return new StyledString();
							}
						}));

		TreeViewerColumn passedColumn = this.createColumn("Passed",
				new AbsoluteWidth(110), true, new Comparator<Object>() {
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
		passedColumn.setLabelProvider(new DelegatingStyledCellLabelProvider(
				new ILabelProviderService.StyledLabelProvider() {
					@Override
					public StyledString getStyledText(URI uri) throws Exception {
						Class<? extends ILocatable> type = LOCATOR_SERVICE
								.getType(uri);

						if (type == IDiff.class) {
							IDiff diff = LOCATOR_SERVICE.resolve(uri,
									IDiff.class, null).get();
							TimeZoneDateRange range = diff.getDateRange();
							return new StyledString(range != null ? range
									.formatDuration() : "?");
						}
						if (type == IDiffRecord.class) {
							IDiffRecord diffRecord = LOCATOR_SERVICE.resolve(
									uri, IDiffRecord.class, null).get();
							TimeZoneDateRange range = diffRecord.getDateRange();
							return new StyledString(range != null ? range
									.formatDuration() : "?");
						}

						return new StyledString();
					}
				}));

		this.createColumn("Start", new AbsoluteWidth(0)).setLabelProvider(
				new DelegatingStyledCellLabelProvider(
						new ILabelProviderService.StyledLabelProvider() {
							@Override
							public StyledString getStyledText(URI uri)
									throws Exception {
								Class<? extends ILocatable> type = LOCATOR_SERVICE
										.getType(uri);

								TimeZoneDateRange range = null;
								if (type == IDiffs.class) {
									IDiffs diffs = LOCATOR_SERVICE.resolve(uri,
											IDiffs.class, null).get();
									range = diffs.getDateRange();
								}
								if (type == IDiff.class) {
									IDiff diff = LOCATOR_SERVICE.resolve(uri,
											IDiff.class, null).get();
									range = diff.getDateRange();
								}
								if (type == IDiffRecord.class) {
									IDiffRecord diffRecord = LOCATOR_SERVICE
											.resolve(uri, IDiffRecord.class,
													null).get();
									range = diffRecord.getDateRange();
								}

								TimeZoneDate date = range != null ? range
										.getStartDate() : null;
								return new StyledString(date != null ? date
										.format(DiffViewer.this.preferenceUtil
												.getDateFormat()) : "");
							}
						}));
		this.createColumn("End", new AbsoluteWidth(0)).setLabelProvider(
				new DelegatingStyledCellLabelProvider(
						new ILabelProviderService.StyledLabelProvider() {
							@Override
							public StyledString getStyledText(URI uri)
									throws Exception {
								Class<? extends ILocatable> type = LOCATOR_SERVICE
										.getType(uri);

								TimeZoneDateRange range = null;
								if (type == IDiffs.class) {
									IDiffs diffs = LOCATOR_SERVICE.resolve(uri,
											IDiffs.class, null).get();
									range = diffs.getDateRange();
								}
								if (type == IDiff.class) {
									IDiff diff = LOCATOR_SERVICE.resolve(uri,
											IDiff.class, null).get();
									range = diff.getDateRange();
								}
								if (type == IDiffRecord.class) {
									IDiffRecord diffRecord = LOCATOR_SERVICE
											.resolve(uri, IDiffRecord.class,
													null).get();
									range = diffRecord.getDateRange();
								}

								TimeZoneDate date = range != null ? range
										.getEndDate() : null;
								return new StyledString(date != null ? date
										.format(DiffViewer.this.preferenceUtil
												.getDateFormat()) : "");
							}
						}));

		this.createColumn("", new AbsoluteWidth(8), false,
				new Comparator<Object>() {
					@Override
					public int compare(Object o1, Object o2) {
						if (!(o1 instanceof URI) || !(o2 instanceof URI)) {
							return 0;
						}

						URI uri1 = (URI) o1;
						URI uri2 = (URI) o2;
						Class<? extends ILocatable> type1 = LOCATOR_SERVICE
								.getType(uri1);
						Class<? extends ILocatable> type2 = LOCATOR_SERVICE
								.getType(uri2);

						if (type1 == IDiff.class && type2 == IDiff.class) {
							String revision1 = URIUtils.getTrail(uri1).get(0);
							String revision2 = URIUtils.getTrail(uri2).get(0);
							return revision1.compareTo(revision2);
						} else if (type1 == IDiffRecord.class
								&& type2 == IDiffRecord.class) {
							try {
								IDiffRecord diffRecord1 = LOCATOR_SERVICE
										.resolve(uri1, IDiffRecord.class, null)
										.get();
								IDiffRecord diffRecord2 = LOCATOR_SERVICE
										.resolve(uri2, IDiffRecord.class, null)
										.get();
								Boolean sourceExists1 = diffRecord1
										.sourceExists();
								Boolean sourceExists2 = diffRecord2
										.sourceExists();
								return sourceExists1.compareTo(sourceExists2);
							} catch (Exception e) {
								LOGGER.error("Error comparing " + uri1
										+ " and " + uri2, e);
							}
						}

						return 0;
					}
				}, new Class<?>[] { IDiff.class, IDiffRecord.class })
				.setLabelProvider(
						new ILabelProviderService.ColumnLabelProvider() {
							@Override
							public String getText(Object element) {
								return "";
							}

							@Override
							public Color getBackground(URI uri)
									throws Exception {
								Class<? extends ILocatable> type = LOCATOR_SERVICE
										.getType(uri);

								if (type == IDiff.class) {
									IDiff diff = LOCATOR_SERVICE.resolve(uri,
											IDiff.class, null).get();
									RGB backgroundRgb = diff.sourcesExist() ? DiffViewer.this.preferenceUtil
											.getColorOk()
											: DiffViewer.this.preferenceUtil
													.getColorMissing();
									return DiffViewer.this.resources
											.createColor(backgroundRgb);
								}
								if (type == IDiffRecord.class) {
									IDiffRecord diffRecord = LOCATOR_SERVICE
											.resolve(uri, IDiffRecord.class,
													null).get();
									RGB backgroundRgb = diffRecord
											.sourceExists() ? DiffViewer.this.preferenceUtil
											.getColorOk()
											: DiffViewer.this.preferenceUtil
													.getColorMissing();
									return DiffViewer.this.resources
											.createColor(backgroundRgb);
								}

								return null;
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
			TreeItem[] treeItems, CalendarRange[] dataRanges) throws Exception {
		List<TreePath> treePaths = new ArrayList<TreePath>();
		for (Item item : com.bkahlert.devel.nebula.utils.ViewerUtils
				.getItemWithDataType(treeItems, URI.class)) {
			IDiffRecord diffRecord = LOCATOR_SERVICE.resolve(
					(URI) item.getData(), IDiffRecord.class, null).get();
			if (diffRecord != null) {
				for (CalendarRange dateRange : dataRanges) {
					if (dateRange.isIntersected(diffRecord.getDateRange()
							.getCalendarRange())) {
						treePaths.add(new TreePath(new Object[] { item
								.getData() }));
						break;
					}
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
			CalendarRange[] dataRanges) throws Exception {
		List<TreePath> treePaths = new ArrayList<TreePath>();
		for (Item item : com.bkahlert.devel.nebula.utils.ViewerUtils
				.getItemWithDataType(treeItems, URI.class)) {
			IDiffs diffs = LOCATOR_SERVICE.resolve((URI) item.getData(),
					IDiffs.class, null).get();
			if (diffs != null && identifier.equals(diffs.getIdentifier())) {
				List<TreePath> childTreePaths = DiffViewer
						.getItemsOfIntersectingDataRanges(
								((TreeItem) item).getItems(), dataRanges);
				for (TreePath childTreePath : childTreePaths) {
					TreePath treePath = com.bkahlert.devel.nebula.utils.ViewerUtils
							.merge(new TreePath(new Object[] { item.getData() }),
									childTreePath);
					treePaths.add(treePath);
				}
			}
		}
		return treePaths;
	}

}
