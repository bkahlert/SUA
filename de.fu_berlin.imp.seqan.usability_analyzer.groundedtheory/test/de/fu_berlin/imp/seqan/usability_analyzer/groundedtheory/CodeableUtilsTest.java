package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;

public class CodeableUtilsTest {
	@SuppressWarnings("serial")
	public static List<ICodeable> codeables = new ArrayList<ICodeable>() {
		{
			add(new ICodeable() {
				@Override
				public URI getCodeInstanceID() {
					try {
						return new URI("sua://diff/2gh/aaa/lll");
					} catch (URISyntaxException e) {
						return null;
					}
				}
			});
			add(new ICodeable() {
				@Override
				public URI getCodeInstanceID() {
					try {
						return new URI("abc://xyz/!hhh/aaa/lll");
					} catch (URISyntaxException e) {
						return null;
					}
				}
			});
			add(new ICodeable() {
				@Override
				public URI getCodeInstanceID() {
					try {
						return new URI("sua:///jkl/aaa/lll");
					} catch (URISyntaxException e) {
						return null;
					}
				}
			});
			add(new ICodeable() {
				@Override
				public URI getCodeInstanceID() {
					try {
						return new URI("abc:///!iuz/aaa/lll");
					} catch (URISyntaxException e) {
						return null;
					}
				}
			});
			add(new ICodeable() {
				@Override
				public URI getCodeInstanceID() {
					try {
						return new URI("sksjkskjsklskljlk://resource");
					} catch (URISyntaxException e) {
						return null;
					}
				}
			});
		}
	};

	@Test
	public void testGetIDs() {
		Set<ID> ids = CodeableUtils.getIDs(codeables);
		assertEquals(2, ids.size());
		assertTrue(ids.contains(new ID("jkl")));
		assertTrue(ids.contains(new ID("2gh")));
	}

	@Test
	public void testGetFingerprints() {
		Set<Fingerprint> fingerprints = CodeableUtils
				.getFingerprints(codeables);
		assertEquals(2, fingerprints.size());
		assertTrue(fingerprints.contains(new Fingerprint("!iuz")));
		assertTrue(fingerprints.contains(new Fingerprint("!hhh")));
	}

	@Test
	public void testGetKeys() {
		Set<Object> keys = CodeableUtils.getKeys(codeables);
		assertEquals(4, keys.size());
		assertTrue(keys.contains(new ID("jkl")));
		assertTrue(keys.contains(new ID("2gh")));
		assertTrue(keys.contains(new Fingerprint("!iuz")));
		assertTrue(keys.contains(new Fingerprint("!hhh")));
	}
}
