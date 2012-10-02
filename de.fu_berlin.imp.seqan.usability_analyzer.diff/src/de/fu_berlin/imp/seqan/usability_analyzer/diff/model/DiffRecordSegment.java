package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.log4j.Logger;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.HasID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;

public class DiffRecordSegment implements ICodeable, HasDateRange, HasID {

	private static final long serialVersionUID = 3956746799123197525L;

	private static final Logger LOGGER = Logger
			.getLogger(DiffRecordSegment.class);

	private DiffRecord diffRecord;
	private long segmentStart;
	private long segmentEnd;

	public DiffRecordSegment(DiffRecord diffRecord,
			long segmentStart, long segmentEnd) {
		assert diffRecord != null;
		this.diffRecord = diffRecord;
		this.segmentStart = segmentStart;
		this.segmentEnd = segmentEnd;
	}

	@Override
	public ID getID() {
		return this.diffRecord.getID();
	}

	@Override
	public TimeZoneDateRange getDateRange() {
		return this.diffRecord.getDateRange();
	}

	@Override
	public URI getCodeInstanceID() {
		try {
			return new URI(diffRecord.getCodeInstanceID().toString() + "#"
					+ segmentStart + "+" + segmentEnd);
		} catch (URISyntaxException e) {
			LOGGER.fatal("Could not create ID for a "
					+ DiffRecordSegment.class.getSimpleName(), e);
			return null;
		}
	}

	public DiffRecord getDiffFileRecord() {
		return diffRecord;
	}

	public long getSegmentStart() {
		return segmentStart;
	}

	public long getSegmentLength() {
		return segmentEnd;
	}

}
