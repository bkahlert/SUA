package de.fu_berlin.imp.apiua.groundedtheory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import de.fu_berlin.imp.apiua.core.model.IdentifierFactory;
import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.apiua.core.services.location.URIUtils;

public class URIUtilsTest {
	@SuppressWarnings("serial")
	public static List<URI> URIS = new ArrayList<URI>() {
		{
			try {
				this.add(new URI("apiua://diff/2gh/aaa/lll"));
				this.add(new URI("abc://xyz/!hhh/aaa/lll"));
				this.add(new URI("apiua:///jkl/aaa/lll"));
				this.add(new URI("abc:///!iuz/aaa/lll"));
				this.add(new URI("sksjkskjsklskljlk://resource"));
				this.add(new URI("apiua://resource/!hhh/aaa/lll#hash"));
				this.add(new URI(
						"apiua://doclog/0meio6dzt3eo1wj7/2011-09-10T10-20-59%2B02%3A00%09ready%09http%3A%2F%2Fwww.seqan.de%2F%0985.179.79.188%09-%090%090%091263%09607"));
			} catch (Exception e) {
				System.err.println("ERROR");
			}
		}
	};

	@Test
	public void testGetResource() {
		assertEquals("diff", URIUtils.getResource(URIS.get(0)));
		assertEquals("xyz", URIUtils.getResource(URIS.get(1)));
		assertEquals(null, URIUtils.getResource(URIS.get(2)));
		assertEquals(null, URIUtils.getResource(URIS.get(3)));
		assertEquals("resource", URIUtils.getResource(URIS.get(4)));
		assertEquals("resource", URIUtils.getResource(URIS.get(5)));
		assertEquals("doclog", URIUtils.getResource(URIS.get(6)));

		assertEquals(null, URIUtils.getResource(null));
	}

	@Test
	public void testGetResources() {
		Set<String> resources = URIUtils.getResources(URIS.toArray(new URI[0]));
		assertEquals(4, resources.size());
		assertTrue(resources.contains("diff"));
		assertTrue(resources.contains("xyz"));
		assertTrue(resources.contains("resource"));
		assertTrue(resources.contains("doclog"));

		assertEquals(new HashSet<String>(), URIUtils.getResources(null));
	}

	@Test
	public void testGetIdentifier() {
		assertEquals(IdentifierFactory.createFrom("2gh"),
				URIUtils.getIdentifier(URIS.get(0)));
		assertEquals(IdentifierFactory.createFrom("!hhh"),
				URIUtils.getIdentifier(URIS.get(1)));
		assertEquals(IdentifierFactory.createFrom("jkl"),
				URIUtils.getIdentifier(URIS.get(2)));
		assertEquals(IdentifierFactory.createFrom("!iuz"),
				URIUtils.getIdentifier(URIS.get(3)));
		assertEquals(null, URIUtils.getIdentifier(URIS.get(4)));
		assertEquals(IdentifierFactory.createFrom("!hhh"),
				URIUtils.getIdentifier(URIS.get(5)));
		assertEquals(IdentifierFactory.createFrom("0meio6dzt3eo1wj7"),
				URIUtils.getIdentifier(URIS.get(6)));

		assertEquals(null, URIUtils.getIdentifier(null));
	}

	@Test
	public void testGetIdentifiers() {
		Set<IIdentifier> identifiers = URIUtils.getIdentifiers(URIS
				.toArray(new URI[0]));
		assertEquals(5, identifiers.size());
		assertTrue(identifiers.contains(IdentifierFactory.createFrom("jkl")));
		assertTrue(identifiers.contains(IdentifierFactory.createFrom("2gh")));
		assertTrue(identifiers.contains(IdentifierFactory.createFrom("!iuz")));
		assertTrue(identifiers.contains(IdentifierFactory.createFrom("!hhh")));
		assertTrue(identifiers.contains(IdentifierFactory
				.createFrom("0meio6dzt3eo1wj7")));

		assertEquals(new HashSet<IIdentifier>(), URIUtils.getIdentifiers(null));
	}

	@Test
	public void testGetTrail() {
		assertEquals(Arrays.asList("aaa", "lll"),
				URIUtils.getTrail(URIS.get(0)));
		assertEquals(Arrays.asList("aaa", "lll"),
				URIUtils.getTrail(URIS.get(1)));
		assertEquals(Arrays.asList("aaa", "lll"),
				URIUtils.getTrail(URIS.get(2)));
		assertEquals(Arrays.asList("aaa", "lll"),
				URIUtils.getTrail(URIS.get(3)));
		assertEquals(Arrays.asList(), URIUtils.getTrail(URIS.get(4)));
		assertEquals(Arrays.asList("aaa", "lll"),
				URIUtils.getTrail(URIS.get(5)));
		assertEquals(
				Arrays.asList("2011-09-10T10-20-59%2B02%3A00%09ready%09http%3A%2F%2Fwww.seqan.de%2F%0985.179.79.188%09-%090%090%091263%09607"),
				URIUtils.getTrail(URIS.get(6)));

		assertEquals(Arrays.asList(), URIUtils.getTrail(null));
	}

	@Test
	public void testGetTrails() {
		Set<List<String>> trails = URIUtils.getTrails(URIS.toArray(new URI[0]));
		assertEquals(2, trails.size());
		assertTrue(trails.contains(Arrays.asList("aaa", "lll")));
		assertTrue(trails
				.contains(Arrays
						.asList("2011-09-10T10-20-59%2B02%3A00%09ready%09http%3A%2F%2Fwww.seqan.de%2F%0985.179.79.188%09-%090%090%091263%09607")));

		assertEquals(new HashSet<List<String>>(), URIUtils.getTrails(null));
	}

	@Test
	public void testFilterByResource() {
		{
			URI[] uris = URIUtils.filterByResource(URIS.toArray(new URI[0]),
					"diff");
			assertEquals(1, uris.length);
			assertEquals(URIS.get(0), uris[0]);
		}
		{
			List<URI> uris = URIUtils.filterByResource(URIS, "xyz");
			assertEquals(1, uris.size());
			assertEquals(URIS.get(1), uris.get(0));
		}
		{
			URI[] uris = URIUtils.filterByResource(URIS.toArray(new URI[0]),
					new String[] { "diff", "xyz" });
			assertEquals(2, uris.length);
			assertEquals(URIS.get(0), uris[0]);
			assertEquals(URIS.get(1), uris[1]);
		}
		{
			List<URI> uris = URIUtils.filterByResource(URIS, "");
			assertEquals(2, uris.size());
			assertEquals(URIS.get(2), uris.get(0));
			assertEquals(URIS.get(3), uris.get(1));
		}

		{
			URI[] uris = URIUtils.filterByResource((URI[]) null, "");
			assertEquals(0, uris.length);
		}
	}

}
