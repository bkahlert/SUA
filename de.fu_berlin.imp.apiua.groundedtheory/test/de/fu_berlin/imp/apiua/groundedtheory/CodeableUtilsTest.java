package de.fu_berlin.imp.apiua.groundedtheory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.IdentifierFactory;
import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.apiua.groundedtheory.CodeableUtils;

public class CodeableUtilsTest {
	@SuppressWarnings("serial")
	public static List<ILocatable> codeables = new ArrayList<ILocatable>() {
		{
			this.add(new ILocatable() {
				@Override
				public URI getUri() {
					return new URI("apiua://diff/2gh/aaa/lll");
				}
			});
			this.add(new ILocatable() {
				@Override
				public URI getUri() {
					return new URI("abc://xyz/!hhh/aaa/lll");
				}
			});
			this.add(new ILocatable() {
				@Override
				public URI getUri() {
					return new URI("apiua:///jkl/aaa/lll");
				}
			});
			this.add(new ILocatable() {
				@Override
				public URI getUri() {
					return new URI("abc:///!iuz/aaa/lll");
				}
			});
			this.add(new ILocatable() {
				@Override
				public URI getUri() {
					return new URI("sksjkskjsklskljlk://resource");
				}
			});
		}
	};

	@Test
	public void testGetIdentifiers() {
		Set<IIdentifier> identifiers = CodeableUtils.getIdentifiers(codeables);
		assertEquals(4, identifiers.size());
		assertTrue(identifiers.contains(IdentifierFactory.createFrom("jkl")));
		assertTrue(identifiers.contains(IdentifierFactory.createFrom("2gh")));
		assertTrue(identifiers.contains(IdentifierFactory.createFrom("!iuz")));
		assertTrue(identifiers.contains(IdentifierFactory.createFrom("!hhh")));
	}
}
