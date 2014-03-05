package de.fu_berlin.imp.seqan.usability_analyzer.doclog.viewer;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.datetime.CalendarRange;
import com.bkahlert.nebula.utils.DNDUtils;
import com.bkahlert.nebula.utils.DNDUtils.Oracle;
import com.bkahlert.nebula.utils.DistributionUtils.AbsoluteWidth;
import com.bkahlert.nebula.utils.DistributionUtils.RelativeWidth;
import com.bkahlert.nebula.utils.Stylers;
import com.bkahlert.nebula.viewer.SortableTreeViewer;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.ILabelProviderService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.ILocatorService;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.Doclog;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogAction;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogScreenshot.Status;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.ui.DoclogLabelProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.EpisodeRenderer;

public class DoclogViewer extends SortableTreeViewer {
	private final LocalResourceManager resources;
	private static final ILocatorService LOCATOR_SERVICE = (ILocatorService) PlatformUI
			.getWorkbench().getService(ILocatorService.class);

	public DoclogViewer(Composite parent, int style, DateFormat dateFormat,
			String timeDifferenceFormat) {
		super(parent, style);

		this.resources = new LocalResourceManager(
				JFaceResources.getResources(), parent);
		parent.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				DoclogViewer.this.resources.dispose();
			}
		});

		this.setContentProvider(new DoclogContentProvider());

		this.initColumns(dateFormat, timeDifferenceFormat);

		DNDUtils.addLocalDragSupport(this, new Oracle() {
			@Override
			public boolean allowDND() {
				return DoclogViewer.this.getControl().getData(
						EpisodeRenderer.CONTROL_DATA_STRING) == null;
			}
		}, URI.class);

		this.sort(0);
	}

	private void initColumns(final DateFormat dateFormat,
			final String timeDifferenceFormat) {

		this.createColumn("Date", new RelativeWidth(.9, 250)).setLabelProvider(
				new ILabelProviderService.StyledLabelProvider() {

					DoclogLabelProvider doclogLabelProvider = new DoclogLabelProvider();

					@Override
					public StyledString getStyledText(URI uri) throws Exception {
						if (LOCATOR_SERVICE.getType(uri) == Doclog.class) {
							return this.doclogLabelProvider.getStyledText(uri);
						}
						if (LOCATOR_SERVICE.getType(uri) == DoclogRecord.class) {
							DoclogRecord doclogRecord = LOCATOR_SERVICE
									.resolve(uri, DoclogRecord.class, null)
									.get();
							TimeZoneDate date = doclogRecord.getDateRange()
									.getStartDate();
							StyledString s = new StyledString(
									(date != null) ? date.format(dateFormat)
											: "");

							String url = doclogRecord.getUrl();
							s.append(
									"   "
											+ ((url != null) ? "... "
													+ url.substring((int) (url
															.length() / 3.0))
													: "") + "",
									Stylers.MINOR_STYLER);

							return s;
						}
						return new StyledString();
					}

					@Override
					public String getToolTipText(URI uri) throws Exception {
						if (LOCATOR_SERVICE.getType(uri) == DoclogRecord.class) {
							DoclogRecord doclogRecord = LOCATOR_SERVICE
									.resolve(uri, DoclogRecord.class, null)
									.get();

							DoclogAction action = doclogRecord.getAction();
							String actionParameter = doclogRecord
									.getActionParameter();
							if (action != null && actionParameter != null) {
								return action.toString() + " :: "
										+ actionParameter;
							} else if (action != null
									&& actionParameter == null) {
								return action.toString();
							} else if (action == null
									&& actionParameter != null) {
								return "??? :: " + actionParameter;
							} else {
								return "";
							}
						}
						return null;
					}

					@Override
					public Image getImage(URI uri) throws Exception {
						return this.doclogLabelProvider.getImage(uri);
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
						if (LOCATOR_SERVICE.getType(uri) == DoclogRecord.class) {
							DoclogRecord doclogRecord = LOCATOR_SERVICE
									.resolve(uri, DoclogRecord.class, null)
									.get();
							TimeZoneDateRange range = doclogRecord
									.getDateRange();
							return new StyledString(range != null ? range
									.formatDuration() : "?");
						}
						return new StyledString();
					}
				}));

		this.createColumn("Action", new AbsoluteWidth(0)).setLabelProvider(
				new DelegatingStyledCellLabelProvider(
						new ILabelProviderService.StyledLabelProvider() {
							@Override
							public StyledString getStyledText(URI uri)
									throws Exception {
								if (LOCATOR_SERVICE.getType(uri) == DoclogRecord.class) {
									DoclogRecord doclogRecord = LOCATOR_SERVICE
											.resolve(uri, DoclogRecord.class,
													null).get();
									DoclogAction action = doclogRecord
											.getAction();
									return new StyledString(
											(action != null) ? action
													.toString() : "");
								}
								return new StyledString();
							}
						}));

		this.createColumn("Param", new AbsoluteWidth(0)).setLabelProvider(
				new DelegatingStyledCellLabelProvider(
						new ILabelProviderService.StyledLabelProvider() {
							@Override
							public StyledString getStyledText(URI uri)
									throws Exception {
								if (LOCATOR_SERVICE.getType(uri) == DoclogRecord.class) {
									DoclogRecord doclogRecord = LOCATOR_SERVICE
											.resolve(uri, DoclogRecord.class,
													null).get();
									String actionParameter = doclogRecord
											.getActionParameter();
									if (actionParameter != null) {
										if (actionParameter.length() > 50) {
											return new StyledString(
													actionParameter
															.subSequence(0, 50)
															+ "...");
										}
										return new StyledString(actionParameter);
									}
									return new StyledString();
								}
								return new StyledString();
							}
						}));

		this.createColumn("Width", new AbsoluteWidth(0)).setLabelProvider(
				new DelegatingStyledCellLabelProvider(
						new ILabelProviderService.StyledLabelProvider() {
							@Override
							public StyledString getStyledText(URI uri)
									throws Exception {
								if (LOCATOR_SERVICE.getType(uri) == DoclogRecord.class) {
									DoclogRecord doclogRecord = LOCATOR_SERVICE
											.resolve(uri, DoclogRecord.class,
													null).get();
									Point windowDimensions = doclogRecord
											.getWindowDimensions();
									return new StyledString(
											(windowDimensions != null) ? windowDimensions.x
													+ ""
													: "-");
								}
								return new StyledString();
							}
						}));

		this.createColumn("Height", new AbsoluteWidth(0)).setLabelProvider(
				new DelegatingStyledCellLabelProvider(
						new ILabelProviderService.StyledLabelProvider() {
							@Override
							public StyledString getStyledText(URI uri)
									throws Exception {
								if (LOCATOR_SERVICE.getType(uri) == DoclogRecord.class) {
									DoclogRecord doclogRecord = LOCATOR_SERVICE
											.resolve(uri, DoclogRecord.class,
													null).get();
									Point windowDimensions = doclogRecord
											.getWindowDimensions();
									return new StyledString(
											(windowDimensions != null) ? windowDimensions.y
													+ ""
													: "-");
								}
								return new StyledString();
							}
						}));

		this.createColumn("X", new AbsoluteWidth(0)).setLabelProvider(
				new DelegatingStyledCellLabelProvider(
						new ILabelProviderService.StyledLabelProvider() {
							@Override
							public StyledString getStyledText(URI uri)
									throws Exception {
								if (LOCATOR_SERVICE.getType(uri) == DoclogRecord.class) {
									DoclogRecord doclogRecord = LOCATOR_SERVICE
											.resolve(uri, DoclogRecord.class,
													null).get();
									Point scrollPosition = doclogRecord
											.getScrollPosition();
									return new StyledString(
											(scrollPosition != null) ? scrollPosition.x
													+ ""
													: "-");
								}
								return new StyledString();
							}
						}));

		this.createColumn("Y", new AbsoluteWidth(0)).setLabelProvider(
				new DelegatingStyledCellLabelProvider(
						new ILabelProviderService.StyledLabelProvider() {
							@Override
							public StyledString getStyledText(URI uri)
									throws Exception {
								if (LOCATOR_SERVICE.getType(uri) == DoclogRecord.class) {
									DoclogRecord doclogRecord = LOCATOR_SERVICE
											.resolve(uri, DoclogRecord.class,
													null).get();
									Point scrollPosition = doclogRecord
											.getScrollPosition();
									return new StyledString(
											(scrollPosition != null) ? scrollPosition.y
													+ ""
													: "-");
								}
								return new StyledString();
							}
						}));

		this.createColumn("", new AbsoluteWidth(8), false,
				new Comparator<Object>() {
					@Override
					public int compare(Object o1, Object o2) {
						if (o1 instanceof Doclog && o2 instanceof Doclog) {
							Doclog doclogFile1 = (Doclog) o1;
							Doclog doclogFile2 = (Doclog) o2;
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
				}, new Class<?>[] { Doclog.class, DoclogRecord.class })
				.setLabelProvider(
						new ILabelProviderService.ColumnLabelProvider() {

							@Override
							public String getText(Object element) {
								return "";
							}

							@Override
							public Color getBackground(URI uri)
									throws Exception {
								if (LOCATOR_SERVICE.getType(uri) == Doclog.class) {
									Status worstStatus = LOCATOR_SERVICE
											.resolve(uri, Doclog.class, null)
											.get().getScreenshotStatus();
									RGB backgroundRgb = worstStatus.getRGB();
									return DoclogViewer.this.resources
											.createColor(backgroundRgb);
								}
								if (LOCATOR_SERVICE.getType(uri) == DoclogRecord.class) {
									DoclogRecord doclogRecord = LOCATOR_SERVICE
											.resolve(uri, DoclogRecord.class,
													null).get();
									RGB backgroundRgb = doclogRecord
											.getScreenshot().getStatus()
											.getRGB();
									return DoclogViewer.this.resources
											.createColor(backgroundRgb);
								}
								return null;
							}
						});

	}

	/**
	 * Returns the {@link TreePath}s that describe {@link DoclogRecord}
	 * fulfilling the following criteria:
	 * <ol>
	 * <li>{@link DoclogRecord}'s {@link TimeZoneDateRange} intersects one of
	 * the given {@link TimeZoneDateRange}s
	 * </ol>
	 * 
	 * @param treeItems
	 * @param dataRanges
	 * @return
	 */
	public static List<TreePath> getItemsOfIntersectingDataRanges(
			TreeItem[] treeItems, CalendarRange[] dataRanges) throws Exception {
		List<TreePath> treePaths = new ArrayList<TreePath>();
		for (Item item : com.bkahlert.nebula.utils.ViewerUtils
				.getItemWithDataType(treeItems, URI.class)) {
			DoclogRecord doclogRecord = LOCATOR_SERVICE.resolve(
					(URI) item.getData(), DoclogRecord.class, null).get();
			if (doclogRecord != null) {
				for (CalendarRange dateRange : dataRanges) {
					if (dateRange.isIntersected(doclogRecord.getDateRange()
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
	 * Returns the {@link TreePath}s that describe {@link DoclogRecord}
	 * fulfilling the following criteria:
	 * <ol>
	 * <li>{@link DoclogRecord} belongs to a {@link Doclog} with the given
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
		for (Item item : com.bkahlert.nebula.utils.ViewerUtils
				.getItemWithDataType(treeItems, URI.class)) {
			Doclog doclog = LOCATOR_SERVICE.resolve((URI) item.getData(),
					Doclog.class, null).get();
			if (doclog != null && identifier.equals(doclog.getIdentifier())) {
				List<TreePath> childTreePaths = getItemsOfIntersectingDataRanges(
						((TreeItem) item).getItems(), dataRanges);
				for (TreePath childTreePath : childTreePaths) {
					TreePath treePath = com.bkahlert.nebula.utils.ViewerUtils
							.merge(new TreePath(new Object[] { item.getData() }),
									childTreePath);
					treePaths.add(treePath);
				}
			}
		}
		return treePaths;
	}
}
