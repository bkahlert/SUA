package de.fu_berlin.imp.seqan.usability_analyzer.doclog.viewer;

import java.net.URI;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.TreePath;
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

import com.bkahlert.devel.nebula.viewer.SortableTreeViewer;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.ILabelProviderService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.ILocatorService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.URIUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.Doclog;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogAction;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogScreenshot.Status;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.ui.DoclogLabelProvider;

public class DoclogFilesViewer extends SortableTreeViewer {
	private final LocalResourceManager resources;

	public DoclogFilesViewer(Composite parent, int style,
			DateFormat dateFormat, String timeDifferenceFormat) {
		super(parent, style);

		this.resources = new LocalResourceManager(
				JFaceResources.getResources(), parent);
		parent.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				DoclogFilesViewer.this.resources.dispose();
			}
		});

		this.setContentProvider(new DoclogContentProvider());

		this.initColumns(dateFormat, timeDifferenceFormat);

		this.sort(0);
	}

	private void initColumns(final DateFormat dateFormat,
			final String timeDifferenceFormat) {

		final ILocatorService locatorService = (ILocatorService) PlatformUI
				.getWorkbench().getService(ILocatorService.class);

		this.createColumn("Date", 160).setLabelProvider(
				new ILabelProviderService.StyledColumnLabelProvider() {
					DoclogLabelProvider doclogLabelProvider = new DoclogLabelProvider();

					@Override
					public String getText(URI uri) throws Exception {
						if (locatorService.getType(uri) == Doclog.class) {
							return this.doclogLabelProvider.getText(uri);
						}
						if (locatorService.getType(uri) == DoclogRecord.class) {
							DoclogRecord doclogRecord = locatorService.resolve(
									uri, DoclogRecord.class, null).get();
							TimeZoneDate date = doclogRecord.getDateRange()
									.getStartDate();
							return (date != null) ? date.format(dateFormat)
									: "";
						}
						return "";
					}

					@Override
					public Image getImage(URI uri) throws Exception {
						return this.doclogLabelProvider.getImage(uri);
					}
				});

		this.createColumn("Passed", 90, true, new Comparator<Object>() {
			@Override
			public int compare(Object arg0, Object arg1) {
				Long l1 = (Long) arg0;
				Long l2 = (Long) arg1;
				if (l1 != null) {
					return l1.compareTo(l2);
				}
				return 0;
			}
		}, new Class<?>[] { Long.class }).setLabelProvider(
				new ILabelProviderService.StyledColumnLabelProvider() {
					@Override
					public String getText(URI uri) throws Exception {
						if (locatorService.getType(uri) == DoclogRecord.class) {
							DoclogRecord doclogRecord = locatorService.resolve(
									uri, DoclogRecord.class, null).get();
							Long milliSecondsPassed = doclogRecord
									.getDateRange().getDifference();
							return (milliSecondsPassed != null) ? DurationFormatUtils
									.formatDuration(milliSecondsPassed,
											timeDifferenceFormat, true) : "";
						}
						return "";
					}
				});

		this.createColumn("", 10, false, new Comparator<Object>() {
			@Override
			public int compare(Object o1, Object o2) {
				if (o1 instanceof Doclog && o2 instanceof Doclog) {
					Doclog doclogFile1 = (Doclog) o1;
					Doclog doclogFile2 = (Doclog) o2;
					Status status1 = doclogFile1.getScreenshotStatus();
					Status status2 = doclogFile2.getScreenshotStatus();
					return Integer.valueOf(status1.ordinal()).compareTo(
							status2.ordinal());
				} else if (o1 instanceof DoclogRecord
						&& o2 instanceof DoclogRecord) {
					DoclogRecord doclogRecord1 = (DoclogRecord) o1;
					DoclogRecord doclogRecord2 = (DoclogRecord) o2;
					Status status1 = doclogRecord1.getScreenshot().getStatus();
					Status status2 = doclogRecord2.getScreenshot().getStatus();
					return Integer.valueOf(status1.ordinal()).compareTo(
							status2.ordinal());
				}
				return 0;
			}
		}, new Class<?>[] { Doclog.class, DoclogRecord.class })
				.setLabelProvider(
						new ILabelProviderService.StyledColumnLabelProvider() {
							@Override
							public String getText(URI uri) throws Exception {
								return "";
							}

							@Override
							public Color getBackground(URI uri)
									throws Exception {
								if (locatorService.getType(uri) == Doclog.class) {
									Status worstStatus = locatorService
											.resolve(uri, Doclog.class, null)
											.get().getScreenshotStatus();
									RGB backgroundRgb = worstStatus.getRGB();
									return DoclogFilesViewer.this.resources
											.createColor(backgroundRgb);
								}
								if (locatorService.getType(uri) == DoclogRecord.class) {
									DoclogRecord doclogRecord = locatorService
											.resolve(uri, DoclogRecord.class,
													null).get();
									RGB backgroundRgb = doclogRecord
											.getScreenshot().getStatus()
											.getRGB();
									return DoclogFilesViewer.this.resources
											.createColor(backgroundRgb);
								}
								return null;
							}
						});

		this.createColumn("URL", 200).setLabelProvider(
				new ILabelProviderService.StyledColumnLabelProvider() {
					@Override
					public String getText(URI uri) throws Exception {
						if (locatorService.getType(uri) == DoclogRecord.class) {
							DoclogRecord doclogRecord = locatorService.resolve(
									uri, DoclogRecord.class, null).get();
							String url = doclogRecord.getUrl();
							return (url != null) ? url : "";
						}
						return "";
					}
				});

		this.createColumn("Action", 60).setLabelProvider(
				new ILabelProviderService.StyledColumnLabelProvider() {
					@Override
					public String getText(URI uri) throws Exception {
						if (locatorService.getType(uri) == DoclogRecord.class) {
							DoclogRecord doclogRecord = locatorService.resolve(
									uri, DoclogRecord.class, null).get();
							DoclogAction action = doclogRecord.getAction();
							return (action != null) ? action.toString() : "";
						}
						return "";
					}
				});

		this.createColumn("Param", 50).setLabelProvider(
				new ILabelProviderService.StyledColumnLabelProvider() {
					@Override
					public String getText(URI uri) throws Exception {
						if (locatorService.getType(uri) == DoclogRecord.class) {
							DoclogRecord doclogRecord = locatorService.resolve(
									uri, DoclogRecord.class, null).get();
							String actionParameter = doclogRecord
									.getActionParameter();
							if (actionParameter != null) {
								if (actionParameter.length() > 50) {
									return actionParameter.subSequence(0, 50)
											+ "...";
								}
								return actionParameter;
							}
							return null;
						}
						return "";
					}
				});

		this.createColumn("Width", 40).setLabelProvider(
				new ILabelProviderService.StyledColumnLabelProvider() {
					@Override
					public String getText(URI uri) throws Exception {
						if (locatorService.getType(uri) == DoclogRecord.class) {
							DoclogRecord doclogRecord = locatorService.resolve(
									uri, DoclogRecord.class, null).get();
							Point windowDimensions = doclogRecord
									.getWindowDimensions();
							return (windowDimensions != null) ? windowDimensions.x
									+ ""
									: "-";
						}
						return "";
					}
				});

		this.createColumn("Height", 40).setLabelProvider(
				new ILabelProviderService.StyledColumnLabelProvider() {
					@Override
					public String getText(URI uri) throws Exception {
						if (locatorService.getType(uri) == DoclogRecord.class) {
							DoclogRecord doclogRecord = locatorService.resolve(
									uri, DoclogRecord.class, null).get();
							Point windowDimensions = doclogRecord
									.getWindowDimensions();
							return (windowDimensions != null) ? windowDimensions.y
									+ ""
									: "-";
						}
						return "";
					}
				});

		this.createColumn("X", 40).setLabelProvider(
				new ILabelProviderService.StyledColumnLabelProvider() {
					@Override
					public String getText(URI uri) throws Exception {
						if (locatorService.getType(uri) == DoclogRecord.class) {
							DoclogRecord doclogRecord = locatorService.resolve(
									uri, DoclogRecord.class, null).get();
							Point scrollPosition = doclogRecord
									.getScrollPosition();
							return (scrollPosition != null) ? scrollPosition.x
									+ "" : "-";
						}
						return "";
					}
				});

		this.createColumn("Y", 40).setLabelProvider(
				new ILabelProviderService.StyledColumnLabelProvider() {
					@Override
					public String getText(URI uri) throws Exception {
						if (locatorService.getType(uri) == DoclogRecord.class) {
							DoclogRecord doclogRecord = locatorService.resolve(
									uri, DoclogRecord.class, null).get();
							Point scrollPosition = doclogRecord
									.getScrollPosition();
							return (scrollPosition != null) ? scrollPosition.y
									+ "" : "-";
						}
						return "";
					}
				});
	}

	/**
	 * Returns the {@link TreePath}s that describe {@link DoclogRecord}
	 * fulfilling the following criteria:
	 * <ol>
	 * <li>{@link DoclocRecord}'s {@link TimeZoneDateRange} intersects one of
	 * the given {@link TimeZoneDateRange}s
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
				.getItemWithDataType(treeItems, DoclogRecord.class)) {
			DoclogRecord doclogRecord = (DoclogRecord) item.getData();
			for (CalendarRange dateRange : dataRanges) {
				if (dateRange.isIntersected(doclogRecord.getDateRange()
						.getCalendarRange())) {
					treePaths.add(new TreePath(new Object[] { doclogRecord }));
					break;
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
			CalendarRange[] dataRanges) {
		List<TreePath> treePaths = new ArrayList<TreePath>();
		for (Item item : com.bkahlert.devel.nebula.utils.ViewerUtils
				.getItemWithDataType(treeItems, Doclog.class)) {
			URI uri = (URI) item.getData();
			if (identifier.equals(URIUtils.getIdentifier(uri))) {
				List<TreePath> childTreePaths = DoclogFilesViewer
						.getItemsOfIntersectingDataRanges(
								((TreeItem) item).getItems(), dataRanges);
				for (TreePath childTreePath : childTreePaths) {
					TreePath treePath = com.bkahlert.devel.nebula.utils.ViewerUtils
							.merge(new TreePath(new URI[] { uri }),
									childTreePath);
					treePaths.add(treePath);
				}
			}
		}
		return treePaths;
	}
}
