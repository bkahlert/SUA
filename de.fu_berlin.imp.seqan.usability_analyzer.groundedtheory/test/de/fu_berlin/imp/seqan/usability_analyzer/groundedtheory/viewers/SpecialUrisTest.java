package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.junit.Test;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer.SpecialUris;

public class SpecialUrisTest {

	@Test
	public void testPayload() throws URISyntaxException {
		for (String uristr : Arrays.asList("http://www.bkahlert.com/",
				"sua://www.bkahlert.com", "//abc/def#hash")) {
			URI uri = new URI(uristr);
			URI uriWithPayload = SpecialUris.setPayload(SpecialUris.CODES, uri);

			assertTrue(SpecialUris.isSpecial(uriWithPayload));
			assertEquals(SpecialUris.CODES,
					SpecialUris.getSpecialUri(uriWithPayload));

			URI payload = SpecialUris.getPayload(uriWithPayload);
			assertEquals(uri, payload);
		}
	}
}
