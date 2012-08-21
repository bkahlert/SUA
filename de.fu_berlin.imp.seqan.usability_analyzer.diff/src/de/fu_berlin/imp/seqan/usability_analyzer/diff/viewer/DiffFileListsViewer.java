package de.fu_berlin.imp.seqan.usability_analyzer.diff.viewer;

import java.text.DateFormat;
import java.util.Comparator;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
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

import com.bkahlert.devel.rcp.selectionUtils.retriever.ISelectionRetriever;
import com.bkahlert.devel.rcp.selectionUtils.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.SortableTreeViewer;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.gt.DiffCodeableProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFile;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFileList;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFileRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.EpisodeRenderer;

public class DiffFileListsViewer extends SortableTreeViewer {
	private LocalResourceManager resources;

	public DiffFileListsViewer(final Composite parent, int style,
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
						boolean episodeRendererActive = DiffFileListsViewer.this
								.getControl().getData(
										EpisodeRenderer.CONTROL_DATA_STRING) != null;
						if (!episodeRendererActive
								&& codeableRetriever.getSelection().size() > 0) {
							LocalSelectionTransfer.getTransfer().setSelection(
									DiffFileListsViewer.this.getSelection());
							LocalSelectionTransfer.getTransfer()
									.setSelectionSetTime(
											event.time & 0xFFFFFFFFL);
							event.doit = true;
						} else {
							event.doit = false;
						}
					};

					public void dragSetData(DragSourceEvent event) {
						boolean episodeRendererActive = DiffFileListsViewer.this
								.getControl().getData(
										EpisodeRenderer.CONTROL_DATA_STRING) != null;
						if (!episodeRendererActive
								&& LocalSelectionTransfer.getTransfer()
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

		this.sort(0);
	}

	private void initColumns(final DateFormat dateFormat,
			final String timeDifferenceFormat) {

		final ILabelProvider imageProvider = new DiffCodeableProvider()
				.getLabelProvider();

		this.createColumn("Date", 350).setLabelProvider(
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						if (element instanceof DiffFileList) {
							DiffFileList diffFileList = (DiffFileList) element;
							ID id = null;
							if (diffFileList.size() > 0) {
								id = diffFileList.get(0).getID();
							}
							return (id != null) ? id.toString() : "";
						}
						if (element instanceof DiffFile) {
							DiffFile diffFile = (DiffFile) element;
							TimeZoneDate date = diffFile.getDateRange()
									.getEndDate();
							return (date != null) ? date.format(dateFormat)
									: "";
						}
						if (element instanceof DiffFileRecord) {
							DiffFileRecord diffFileRecord = (DiffFileRecord) element;
							String name = diffFileRecord.getFilename();
							return (name != null) ? name : "";
						}
						return "";
					}

					@Override
					public Image getImage(Object element) {
						return imageProvider.getImage(element);
					}
				});

		TreeViewerColumn episodeColumn = this.createColumn("", 12);
		episodeColumn.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
			}
		});
		new EpisodeRenderer(this, episodeColumn, 1).activateRendering();

		this.createColumn("Passed", 90, true, new Comparator<Object>() {
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
						if (element instanceof DiffFile) {
							DiffFile diffFile = (DiffFile) element;
							Long milliSecondsPassed = diffFile.getDateRange()
									.getDifference();
							return (milliSecondsPassed != null) ? DurationFormatUtils
									.formatDuration(milliSecondsPassed,
											timeDifferenceFormat, true)
									: "unknown";
						}
						if (element instanceof DiffFileRecord) {
							DiffFileRecord diffFileRecord = (DiffFileRecord) element;
							Long milliSecondsPassed = diffFileRecord
									.getDateRange().getDifference();
							return (milliSecondsPassed != null) ? DurationFormatUtils
									.formatDuration(milliSecondsPassed,
											timeDifferenceFormat, true)
									: "unknown";
						}
						return "";
					}
				});

		this.createColumn("Revision", 65).setLabelProvider(
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						if (element instanceof DiffFileList) {
							DiffFileList diffFileList = (DiffFileList) element;
							return "# " + diffFileList.size();
						}
						if (element instanceof DiffFile) {
							DiffFile diffFile = (DiffFile) element;
							String revision = diffFile.getRevision();
							return (revision != null) ? revision : "";
						}
						if (element instanceof DiffFileRecord) {
							DiffFileRecord diffFileRecord = (DiffFileRecord) element;
							String revision = diffFileRecord.getDiffFile()
									.getRevision();
							return (revision != null) ? revision : "";
						}
						return "";
					}
				});

		this.createColumn("", 10, false, new Comparator<Object>() {
			@Override
			public int compare(Object o1, Object o2) {
				if (o1 instanceof DiffFile && o2 instanceof DiffFile) {
					DiffFile diffFile1 = (DiffFile) o1;
					DiffFile diffFile2 = (DiffFile) o2;
					String revision1 = diffFile1.getRevision();
					String revision2 = diffFile2.getRevision();
					return revision1.compareTo(revision2);
				} else if (o1 instanceof DiffFileRecord
						&& o2 instanceof DiffFileRecord) {
					DiffFileRecord diffFileRecord1 = (DiffFileRecord) o1;
					DiffFileRecord diffFileRecord2 = (DiffFileRecord) o2;
					Boolean sourceExists1 = diffFileRecord1.sourceExists();
					Boolean sourceExists2 = diffFileRecord2.sourceExists();
					return sourceExists1.compareTo(sourceExists2);
				}
				return 0;
			}
		}, new Class<?>[] { DiffFile.class, DiffFileRecord.class })
				.setLabelProvider(new ColumnLabelProvider() {
					SUACorePreferenceUtil preferenceUtil = new SUACorePreferenceUtil();

					@Override
					public String getText(Object element) {
						return "";
					}

					@Override
					public Color getBackground(Object element) {
						if (element instanceof DiffFile) {
							DiffFile diffFile = (DiffFile) element;
							RGB backgroundRgb = diffFile.sourcesExist() ? preferenceUtil
									.getColorOk() : preferenceUtil
									.getColorMissing();
							return resources.createColor(backgroundRgb);
						}
						if (element instanceof DiffFileRecord) {
							DiffFileRecord diffFileRecord = (DiffFileRecord) element;
							RGB backgroundRgb = diffFileRecord.sourceExists() ? preferenceUtil
									.getColorOk() : preferenceUtil
									.getColorMissing();
							return resources.createColor(backgroundRgb);
						}
						return null;
					}
				});
	}
}
