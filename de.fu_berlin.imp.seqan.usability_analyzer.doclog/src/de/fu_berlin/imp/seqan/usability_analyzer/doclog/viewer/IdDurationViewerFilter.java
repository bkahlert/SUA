package de.fu_berlin.imp.seqan.usability_analyzer.doclog.viewer;

import java.util.List;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.DateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogRecord;

public class IdDurationViewerFilter extends ViewerFilter {
	private ID id;
	private List<DateRange> dateRanges;

	public IdDurationViewerFilter(ID id, List<DateRange> dateRanges) {
		this.id = id;
		this.dateRanges = dateRanges;
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (!(element instanceof DoclogRecord))
			return true;
		DoclogRecord doclogRecord = (DoclogRecord) element;
		ID doclogFileId = doclogRecord.getDoclogPath().getId();
		for (DateRange duration : dateRanges) {
			if (duration.isInRange(doclogRecord.getDate())
					&& id.equals(doclogFileId))
				return true;
		}
		return false;
	}
}
