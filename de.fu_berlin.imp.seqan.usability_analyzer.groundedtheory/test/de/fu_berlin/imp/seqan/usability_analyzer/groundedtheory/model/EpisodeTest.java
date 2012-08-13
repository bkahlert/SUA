package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model;

import java.net.URI;
import java.net.URISyntaxException;

import junit.framework.Assert;

import org.junit.Test;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;

public class EpisodeTest {
	private final ID id = new ID("IAmAnID");
	private final Fingerprint fingerprint = new Fingerprint("!IAmAFingerprint");
	private final TimeZoneDate start = new TimeZoneDate(
			"2000-05-20T15:30:15.200+02:00");
	private final TimeZoneDate end = new TimeZoneDate(
			"2000-05-21T17:35:25+03:00");
	private final TimeZoneDateRange range = new TimeZoneDateRange(start, end);
	private final TimeZoneDateRange rangeNoEnd = new TimeZoneDateRange(start,
			null);
	private final TimeZoneDateRange rangeNoStart = new TimeZoneDateRange(null,
			end);
	private final TimeZoneDateRange rangeNoStartEnd = new TimeZoneDateRange(
			null, null);

	@Test
	public void testId() throws URISyntaxException {
		Assert.assertEquals(
				new URI(
						"sua://episode/IAmAnID/2000-05-20T15:30:15+02:00/2000-05-21T17:35:25+03:00"),
				new Episode(id, start, end, null, null).getCodeInstanceID());
		Assert.assertEquals(new URI(
				"sua://episode/!IAmAFingerprint//2000-05-21T17:35:25+03:00"),
				new Episode(fingerprint, null, end, null, null)
						.getCodeInstanceID());
		Assert.assertEquals(new URI(
				"sua://episode/IAmAnID/2000-05-20T15:30:15+02:00/"),
				new Episode(id, start, null, null, null).getCodeInstanceID());
		Assert.assertEquals(new URI("sua://episode/!IAmAFingerprint//"),
				new Episode(fingerprint, null, null, null).getCodeInstanceID());

		Assert.assertEquals(
				new URI(
						"sua://episode/!IAmAFingerprint/2000-05-20T15:30:15+02:00/2000-05-21T17:35:25+03:00"),
				new Episode(fingerprint, range, null, null).getCodeInstanceID());
		Assert.assertEquals(new URI(
				"sua://episode/IAmAnID//2000-05-21T17:35:25+03:00"),
				new Episode(id, rangeNoStart, null, null).getCodeInstanceID());
		Assert.assertEquals(new URI(
				"sua://episode/!IAmAFingerprint/2000-05-20T15:30:15+02:00/"),
				new Episode(fingerprint, rangeNoEnd, null, null)
						.getCodeInstanceID());
		Assert.assertEquals(new URI("sua://episode/IAmAnID//"), new Episode(id,
				rangeNoStartEnd, null, null).getCodeInstanceID());
	}
}
