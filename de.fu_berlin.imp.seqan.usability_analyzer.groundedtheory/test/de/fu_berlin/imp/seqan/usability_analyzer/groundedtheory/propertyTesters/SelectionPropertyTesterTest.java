package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.propertyTesters;

import static org.junit.Assert.assertEquals;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.junit.Test;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.HasFingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.HasID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;

public class SelectionPropertyTesterTest {

	private static class HasBoth implements HasID, HasFingerprint {

		private ID id;
		private Fingerprint fingerprint;

		public HasBoth(ID id, Fingerprint fingerprint) {
			this.id = id;
			this.fingerprint = fingerprint;
		}

		@Override
		public ID getID() {
			return this.id;
		}

		@Override
		public Fingerprint getFingerprint() {
			return this.fingerprint;
		}

	}

	private static boolean testSelection(Object... elements) {
		SelectionPropertyTester tester = new SelectionPropertyTester();
		ISelection selection = new StructuredSelection(elements);
		return tester.test(selection, "hasSingleKey", null, null);
	}

	private static HasID hasID1 = new HasID() {
		@Override
		public ID getID() {
			return new ID("id1");
		}
	};

	private static HasID hasID2 = new HasID() {
		@Override
		public ID getID() {
			return new ID("id2");
		}
	};

	private static HasFingerprint hasFingerprint1 = new HasFingerprint() {
		@Override
		public Fingerprint getFingerprint() {
			return new Fingerprint("!fingerprint1");
		}
	};

	private static HasFingerprint hasFingerprint2 = new HasFingerprint() {
		@Override
		public Fingerprint getFingerprint() {
			return new Fingerprint("!fingerprint2");
		}
	};

	private static HasBoth hasBoth1 = new HasBoth(hasID1.getID(),
			hasFingerprint1.getFingerprint());

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
		assertEquals(true, testSelection(hasBoth1));
		assertEquals(true, testSelection(hasBoth1, hasID1));
		assertEquals(true, testSelection(hasBoth1, hasFingerprint1));
		assertEquals(false, testSelection(hasBoth1, hasID2));
		assertEquals(false, testSelection(hasBoth1, hasFingerprint2));
	}
}
