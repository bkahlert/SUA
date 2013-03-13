package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.propertyTesters;

import static org.junit.Assert.assertEquals;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.junit.Test;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.HasIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.IdentifierFactory;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;

public class SelectionPropertyTesterTest {

	private static boolean testSelection(Object... elements) {
		SelectionPropertyTester tester = new SelectionPropertyTester();
		ISelection selection = new StructuredSelection(elements);
		return tester.test(selection, "containsSingleKey", null, null);
	}

	private static HasIdentifier hasID1 = new HasIdentifier() {
		@Override
		public IIdentifier getIdentifier() {
			return IdentifierFactory.createFrom("id1");
		}
	};

	private static HasIdentifier hasID2 = new HasIdentifier() {
		@Override
		public IIdentifier getIdentifier() {
			return IdentifierFactory.createFrom("id2");
		}
	};

	private static HasIdentifier hasFingerprint1 = new HasIdentifier() {
		@Override
		public IIdentifier getIdentifier() {
			return IdentifierFactory.createFrom("!fingerprint1");
		}
	};

	private static HasIdentifier hasFingerprint2 = new HasIdentifier() {
		@Override
		public IIdentifier getIdentifier() {
			return IdentifierFactory.createFrom("!fingerprint2");
		}
	};

	@Test
	public void test() {
		assertEquals(false, testSelection());
		assertEquals(false, testSelection("invalid"));
		assertEquals(true, testSelection(hasID1));
		assertEquals(true, testSelection(hasID2));
		assertEquals(false, testSelection(hasID1, hasID2));
		assertEquals(false, testSelection(hasID1, hasFingerprint1));
		assertEquals(false, testSelection(hasID1, hasFingerprint2));
		assertEquals(true, testSelection(hasFingerprint1));
		assertEquals(true, testSelection(hasFingerprint2));
	}
}
