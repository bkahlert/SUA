package de.fu_berlin.imp.seqan.usability_analyzer.diff.model;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.log4j.Logger;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;

public class DiffFileRecordSegment implements ICodeable {

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
