package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.log4j.Logger;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.HasID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;

public class DiffFileRecordSegment implements ICodeable, HasDateRange, HasID {

	private static final long serialVersionUID = 3956746799123197525L;

	private static final Logger LOGGER = Logger
			.getLogger(DiffFileRecordSegment.class);

	private DiffFileRecord diffFileRecord;
	private long segmentStart;
	private long segmentEnd;

	public DiffFileRecordSegment(DiffFileRecord diffFileRecord,
			long segmentStart, long segmentEnd) {
		assert diffFileRecord != null;
		this.diffFileRecord = diffFileRecord;
		this.segmentStart = segmentStart;
		this.segmentEnd = segmentEnd;
	}

	@Override
	public ID getID() {
		return this.diffFileRecord.getID();
	}

	@Override
	public TimeZoneDateRange getDateRange() {
		return this.diffFileRecord.getDateRange();
	}

	@Override
	public URI getCodeInstanceID() {
		try {
			return new URI(diffFileRecord.getCodeInstanceID().toString() + "#"
					+ segmentStart + "+" + segmentEnd);
		} catch (URISyntaxException e) {
			LOGGER.fatal("Could not create ID for a "
					+ DiffFileRecordSegment.class.getSimpleName(), e);
			return null;
		}
	}

	public DiffFileRecord getDiffFileRecord() {
		return diffFileRecord;
	}

	public long getSegmentStart() {
		return segmentStart;
	}

	public long getSegmentLength() {
		return segmentEnd;
	}

}
