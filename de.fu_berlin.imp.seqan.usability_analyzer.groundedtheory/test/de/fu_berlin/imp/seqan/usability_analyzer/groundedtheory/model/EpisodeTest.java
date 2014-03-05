package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model;

import java.net.URISyntaxException;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.IdentifierFactory;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;

public class EpisodeTest {
	private final IIdentifier id = IdentifierFactory.createFrom("IAmAnID");
	private final IIdentifier fingerprint = IdentifierFactory
			.createFrom("!IAmAFingerprint");
	private final TimeZoneDate start = new TimeZoneDate(
			"2000-05-20T15:30:15.200+02:00");
	private final TimeZoneDate end = new TimeZoneDate(
			"2000-05-21T17:35:25+03:00");
	private final TimeZoneDateRange range = new TimeZoneDateRange(this.start,
			this.end);
	private final TimeZoneDateRange rangeNoEnd = new TimeZoneDateRange(
			this.start, null);
	private final TimeZoneDateRange rangeNoStart = new TimeZoneDateRange(null,
			this.end);
	private final TimeZoneDateRange rangeNoStartEnd = new TimeZoneDateRange(
			null, null);

	@Test
	@Ignore
	// TODO now is slightly earlier in milliseconds
	public void testId() throws URISyntaxException {
		String now = new TimeZoneDate().toISO8601();
		Assert.assertEquals(new URI("sua://episode/IAmAnID/" + now),
				new Episode(this.id, this.start, this.end, null).getUri());
		Assert.assertEquals(new URI("sua://episode/!IAmAFingerprint/" + now),
				new Episode(this.fingerprint, null, this.end, null).getUri());
		Assert.assertEquals(new URI("sua://episode/IAmAnID/" + now),
				new Episode(this.id, this.start, null, null).getUri());
		Assert.assertEquals(new URI("sua://episode/!IAmAFingerprint/" + now),
				new Episode(this.fingerprint, null, null, null).getUri());

		Assert.assertEquals(new URI("sua://episode/!IAmAFingerprint/" + now),
				new Episode(this.fingerprint, this.range, null).getUri());
		Assert.assertEquals(new URI("sua://episode/IAmAnID/" + now),
				new Episode(this.id, this.rangeNoStart, null).getUri());
		Assert.assertEquals(new URI("sua://episode/!IAmAFingerprint/" + now),
				new Episode(this.fingerprint, this.rangeNoEnd, null).getUri());
		Assert.assertEquals(new URI("sua://episode/IAmAnID/" + now),
				new Episode(this.id, this.rangeNoStartEnd, null).getUri());
	}
}
