package de.fu_berlin.imp.seqan.usability_analyzer.diff.viewer;

import java.text.DateFormat;
import java.util.Comparator;
import java.util.Date;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.SortableTreeViewer;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFile;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFileList;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFileRecord;

public class DiffFileListsViewer extends SortableTreeViewer {

	public DiffFileListsViewer(Composite parent, int style,
			DateFormat dateFormat, String timeDifferenceFormat) {
		super(parent, style);
		// TODO Auto-generated constructor stub

		initColumns(dateFormat, timeDifferenceFormat);

		this.sort(0);
	}

	private void initColumns(final DateFormat dateFormat,
			final String timeDifferenceFormat) {
		this.createColumn("Date", 160).setLabelProvider(
				new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						if (element instanceof DiffFileList) {
							DiffFileList diffFileList = (DiffFileList) element;
							ID id = null;
							if (diffFileList.size() > 0) {
								id = diffFileList.get(0).getId();
							}
							return (id != null) ? id.toString() : "";
						}
						if (element instanceof DiffFile) {
							DiffFile diffFile = (DiffFile) element;
							Date date = diffFile.getDate();
							return (date != null) ? dateFormat.format(date)
									: "";
						}
						if (element instanceof DiffFileRecord) {
							DiffFileRecord diffFileRecord = (DiffFileRecord) element;
							String name = diffFileRecord.getFilename();
							return (name != null) ? name : "";
						}
						return "";
					}
				});

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
							Long milliSecondsPassed = diffFile
									.getMillisecondsPassed();
							return (milliSecondsPassed != null) ? DurationFormatUtils
									.formatDuration(milliSecondsPassed,
											timeDifferenceFormat, true) : "";
						}
						if (element instanceof DiffFileRecord) {
							DiffFileRecord diffFileRecord = (DiffFileRecord) element;
							Long milliSecondsPassed = diffFileRecord
									.getDateRange().getDifference();
							return (milliSecondsPassed != null) ? DurationFormatUtils
									.formatDuration(milliSecondsPassed,
											timeDifferenceFormat, true) : "";
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
	}

}
