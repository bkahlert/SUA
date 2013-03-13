package de.fu_berlin.imp.seqan.usability_analyzer.doclog.viewer;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.ColumnLabelProvider;
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
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.Doclog;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogAction;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogScreenshot.Status;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.ui.ImageManager;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;

public class DoclogFilesViewer extends SortableTreeViewer {
	private LocalResourceManager resources;

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

		final ICodeService codeService = (ICodeService) PlatformUI
				.getWorkbench().getService(ICodeService.class);

		this.createColumn("Date", 160).setLabelProvider(
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						if (element instanceof Doclog) {
							Doclog doclog = (Doclog) element;
							return doclog.getIdentifier().toString();
						}
						if (element instanceof DoclogRecord) {
							DoclogRecord doclogRecord = (DoclogRecord) element;
							TimeZoneDate date = doclogRecord.getDateRange()
									.getStartDate();
							return (date != null) ? date.format(dateFormat)
									: "";
						}
						return "";
					}

					@Override
					public Image getImage(Object element) {
						if (element instanceof Doclog) {
							Doclog doclog = (Doclog) element;
							try {
								return (codeService.getCodes(doclog).size() > 0) ? (codeService
										.isMemo(doclog) ? ImageManager.DOCLOGFILE_CODED_MEMO
										: ImageManager.DOCLOGFILE_CODED)
										: (codeService.isMemo(doclog) ? ImageManager.DOCLOGFILE_MEMO
												: ImageManager.DOCLOGFILE);
							} catch (CodeServiceException e) {
								return ImageManager.DOCLOGRECORD;
							}
						}
						if (element instanceof DoclogRecord) {
							DoclogRecord doclogRecord = (DoclogRecord) element;
							try {
								return (codeService.getCodes(doclogRecord)
										.size() > 0) ? (codeService
										.isMemo(doclogRecord) ? ImageManager.DOCLOGRECORD_CODED_MEMO
										: ImageManager.DOCLOGRECORD_CODED)
										: (codeService.isMemo(doclogRecord) ? ImageManager.DOCLOGRECORD_MEMO
												: ImageManager.DOCLOGRECORD);
							} catch (CodeServiceException e) {
								return ImageManager.DOCLOGRECORD;
							}
						}
						return super.getImage(element);
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
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						if (element instanceof DoclogRecord) {
							DoclogRecord doclogRecord = (DoclogRecord) element;
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
				.setLabelProvider(new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						return "";
					}

					@Override
					public Color getBackground(Object element) {
						if (element instanceof Doclog) {
							Status worstStatus = ((Doclog) element)
									.getScreenshotStatus();
							RGB backgroundRgb = worstStatus.getRGB();
							return DoclogFilesViewer.this.resources
									.createColor(backgroundRgb);
						}
						if (element instanceof DoclogRecord) {
							DoclogRecord doclogRecord = (DoclogRecord) element;
							RGB backgroundRgb = doclogRecord.getScreenshot()
									.getStatus().getRGB();
							return DoclogFilesViewer.this.resources
									.createColor(backgroundRgb);
						}
						return null;
					}
				});

		this.createColumn("URL", 200).setLabelProvider(
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

		this.createColumn("Action", 60).setLabelProvider(
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

		this.createColumn("Param", 50).setLabelProvider(
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						if (element instanceof DoclogRecord) {
							DoclogRecord doclogRecord = (DoclogRecord) element;
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

		this.createColumn("Height", 40).setLabelProvider(
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

		this.createColumn("X", 40).setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof DoclogRecord) {
					DoclogRecord doclogRecord = (DoclogRecord) element;
					Point scrollPosition = doclogRecord.getScrollPosition();
					return (scrollPosition != null) ? scrollPosition.x + ""
							: "-";
				}
				return "";
			}
		});

		this.createColumn("Y", 40).setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof DoclogRecord) {
					DoclogRecord doclogRecord = (DoclogRecord) element;
					Point scrollPosition = doclogRecord.getScrollPosition();
					return (scrollPosition != null) ? scrollPosition.y + ""
							: "-";
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
			TreeItem[] treeItems, TimeZoneDateRange[] dataRanges) {
		List<TreePath> treePaths = new ArrayList<TreePath>();
		for (Item item : com.bkahlert.devel.nebula.utils.ViewerUtils
				.getItemWithDataType(treeItems, DoclogRecord.class)) {
			DoclogRecord doclogRecord = (DoclogRecord) item.getData();
			for (TimeZoneDateRange dateRange : dataRanges) {
				if (dateRange.isIntersected(doclogRecord.getDateRange())) {
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
			TimeZoneDateRange[] dataRanges) {
		List<TreePath> treePaths = new ArrayList<TreePath>();
		for (Item item : com.bkahlert.devel.nebula.utils.ViewerUtils
				.getItemWithDataType(treeItems, Doclog.class)) {
			Doclog doclog = (Doclog) item.getData();
			if (identifier.equals(doclog.getIdentifier())) {
				List<TreePath> childTreePaths = DoclogFilesViewer
						.getItemsOfIntersectingDataRanges(
								((TreeItem) item).getItems(), dataRanges);
				for (TreePath childTreePath : childTreePaths) {
					TreePath treePath = com.bkahlert.devel.nebula.utils.ViewerUtils
							.merge(new TreePath(new Object[] { doclog }),
									childTreePath);
					treePaths.add(treePath);
				}
			}
		}
		return treePaths;
	}
}
