package de.fu_berlin.imp.seqan.usability_analyzer.diff.viewer;

import java.text.DateFormat;
import java.util.Comparator;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
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
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.nebula.viewer.SortableTreeViewer;
import com.bkahlert.devel.rcp.selectionUtils.retriever.ISelectionRetriever;
import com.bkahlert.devel.rcp.selectionUtils.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.Diff;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffList;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffRecordSegment;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiff;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.ui.ImageManager;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.EpisodeRenderer;

public class DiffListsViewer extends SortableTreeViewer {
	private LocalResourceManager resources;

	public DiffListsViewer(final Composite parent, int style,
			DateFormat dateFormat, String timeDifferenceFormat) {
		super(parent, style);

		this.resources = new LocalResourceManager(
				JFaceResources.getResources(), parent);
		parent.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				resources.dispose();
			}
		});

		initColumns(dateFormat, timeDifferenceFormat);

		final ISelectionRetriever<ICodeable> codeableRetriever = SelectionRetrieverFactory
				.getSelectionRetriever(ICodeable.class);

		int operations = DND.DROP_LINK;
		Transfer[] transferTypes = new Transfer[] { LocalSelectionTransfer
				.getTransfer() };
		this.addDragSupport(operations, transferTypes,
				new DragSourceListener() {
					public void dragStart(DragSourceEvent event) {
						boolean episodeRendererActive = DiffListsViewer.this
								.getControl().getData(
										EpisodeRenderer.CONTROL_DATA_STRING) != null;
						if (!episodeRendererActive
								&& codeableRetriever.getSelection().size() > 0) {
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

					public void dragSetData(DragSourceEvent event) {
						if (LocalSelectionTransfer.getTransfer()
								.isSupportedType(event.dataType)) {
							event.data = LocalSelectionTransfer.getTransfer()
									.getSelection();
						}
					}

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

		final ICodeService codeService = (ICodeService) PlatformUI
				.getWorkbench().getService(ICodeService.class);

		this.createColumn("Date", 350).setLabelProvider(
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						if (element instanceof DiffList) {
							DiffList diffList = (DiffList) element;
							ID id = null;
							if (diffList.size() > 0) {
								id = diffList.get(0).getID();
							}
							return (id != null) ? id.toString() : "";
						}
						if (element instanceof IDiff) {
							Diff diff = (Diff) element;
							TimeZoneDate date = diff.getDateRange()
									.getEndDate();
							return (date != null) ? date.format(dateFormat)
									: "";
						}
						if (element instanceof DiffRecord) {
							DiffRecord diffRecord = (DiffRecord) element;
							String name = diffRecord.getFilename();
							return (name != null) ? name : "";
						}
						return "";
					}

					@Override
					public Image getImage(Object element) {
						if (element instanceof DiffList) {
							return ImageManager.DIFFFILELIST;
						}
						if (element instanceof IDiff) {
							Diff diff = (Diff) element;
							try {
								if (codeService.getCodes(diff).size() > 0) {
									return codeService.isMemo(diff) ? ImageManager.DIFFFILE_CODED_MEMO
											: ImageManager.DIFFFILE_CODED;
								} else {
									for (DiffRecord diffRecord : diff
											.getDiffFileRecords()) {
										if (codeService.getCodes(diffRecord)
												.size() > 0) {
											return codeService.isMemo(diff) ? ImageManager.DIFFFILE_PARTIALLY_CODED_MEMO
													: ImageManager.DIFFFILE_PARTIALLY_CODED;
										}
									}
									return (codeService.isMemo(diff) ? ImageManager.DIFFFILE_MEMO
											: ImageManager.DIFFFILE);
								}
							} catch (CodeServiceException e) {
								return ImageManager.DIFFFILE;
							}
						}
						// TODO: partially coded icon wenn diffrecordsegment
						// existiert
						if (element instanceof DiffRecord) {
							DiffRecord diffRecord = (DiffRecord) element;
							try {
								return (codeService.getCodes(diffRecord).size() > 0) ? (codeService
										.isMemo(diffRecord) ? ImageManager.DIFFFILERECORD_CODED_MEMO
										: ImageManager.DIFFFILERECORD_CODED)
										: (codeService.isMemo(diffRecord) ? ImageManager.DIFFFILERECORD_MEMO
												: ImageManager.DIFFFILERECORD);
							} catch (CodeServiceException e) {
								return ImageManager.DIFFFILERECORD;
							}
						}
						if (element instanceof DiffRecordSegment) {
							DiffRecordSegment diffRecordSegment = (DiffRecordSegment) element;
							try {
								return (codeService.getCodes(diffRecordSegment)
										.size() > 0) ? (codeService
										.isMemo(diffRecordSegment) ? ImageManager.DIFFFILERECORDSEGMENT_CODED_MEMO
										: ImageManager.DIFFFILERECORDSEGMENT_CODED)
										: (codeService
												.isMemo(diffRecordSegment) ? ImageManager.DIFFFILERECORDSEGMENT_MEMO
												: ImageManager.DIFFFILERECORDSEGMENT);
							} catch (CodeServiceException e) {
								return ImageManager.DIFFFILERECORDSEGMENT;
							}
						}
						return super.getImage(element);
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
						if (l1 != null)
							return l1.compareTo(l2);
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
				if (element instanceof DiffRecord) {
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

		this.createColumn("Revision", 65).setLabelProvider(
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						if (element instanceof DiffList) {
							DiffList diffList = (DiffList) element;
							return "# " + diffList.size();
						}
						if (element instanceof IDiff) {
							IDiff diff = (IDiff) element;
							Long revision = diff.getRevision();
							return (revision != null) ? StringUtils.leftPad(
									revision.toString(), 8, '0') : "";
						}
						if (element instanceof DiffRecord) {
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
				} else if (o1 instanceof DiffRecord && o2 instanceof DiffRecord) {
					DiffRecord diffFileRecord1 = (DiffRecord) o1;
					DiffRecord diffFileRecord2 = (DiffRecord) o2;
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
							RGB backgroundRgb = diff.sourcesExist() ? preferenceUtil
									.getColorOk() : preferenceUtil
									.getColorMissing();
							return resources.createColor(backgroundRgb);
						}
						if (element instanceof DiffRecord) {
							DiffRecord diffRecord = (DiffRecord) element;
							RGB backgroundRgb = diffRecord.sourceExists() ? preferenceUtil
									.getColorOk() : preferenceUtil
									.getColorMissing();
							return resources.createColor(backgroundRgb);
						}
						return null;
					}
				});
	}
}
