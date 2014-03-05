package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;
import java.net.URISyntaxException;
import java.security.InvalidParameterException;

import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.Rule;
import org.junit.Test;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;

public class CodeStoreSaveTest extends CodeStoreHelper {

	@Rule
	public JUnitRuleMockery context = new JUnitRuleMockery() {
		{
			this.setThreadingPolicy(new Synchroniser());
		}
	};

	public CodeStoreSaveTest() throws URISyntaxException {
		super();
	}

	@Test(expected = InvalidParameterException.class)
	public void getInvalidBasenameCode() {
		CodeStore.getMemoBasename((ICode) null);
	}

	@Test(expected = InvalidParameterException.class)
	public void getInvalidBasenameCodeInstance() {
		CodeStore.getMemoBasename((ICodeInstance) null);
	}

	@Test(expected = InvalidParameterException.class)
	public void getInvalidBasenameLocatable() {
		CodeStore.getMemoBasename((URI) null);
	}

	@Test
	public void getBasename() {
		assertEquals("code_234233209", CodeStore.getMemoBasename(this.code1));
		assertEquals("code_9908372", CodeStore.getMemoBasename(this.code2));
		assertEquals("codeInstance_sua%3A%2F%2FcodeInstance1",
				CodeStore.getMemoBasename(this.locatable1.getUri()));
		assertEquals("codeInstance_sua%3A%2F%2FcodeInstance2",
				CodeStore.getMemoBasename(this.locatable2.getUri()));
		assertEquals("codeInstance_sua%3A%2F%2FcodeInstance3",
				CodeStore.getMemoBasename(this.locatable3.getUri()));
		assertEquals("codeInstance_9908372_sua%3A%2F%2FcodeInstance1",
				CodeStore.getMemoBasename(this.codeInstance1));
		assertEquals("codeInstance_234233209_sua%3A%2F%2FcodeInstance2",
				CodeStore.getMemoBasename(this.codeInstance2));
		assertEquals("codeInstance_9908372_sua%3A%2F%2FcodeInstance3",
				CodeStore.getMemoBasename(this.codeInstance3));
	}

	@Test
	public void testSaveMemo() throws Exception {
		final String codeMemo = "Code: Lorem ipsum";
		final String codeInstanceMemo = "Code Instance:\nLorem ipsum";
		final String locatableMemo = "Locatable:\nĽốґểм ĭрŝũო";

		CodeStore codeStore = (CodeStore) this.getSmallCodeStore();

		// sanity check
		// assertEquals(codeStore.getCode(this.code1.getId()), this.code1);
		// codeStore.getCodeInstance(this.codeInstance2.getCodeInstanceID());

		/*
		 * store in code store #1
		 */
		// save memo for code
		codeStore.saveMemo(CodeStore.getMemoBasename(this.code1), codeMemo);
		assertEquals(codeMemo,
				codeStore.loadMemo(CodeStore.getMemoBasename(this.code1)));

		// save memo for code instance
		codeStore.saveMemo(CodeStore.getMemoBasename(this.codeInstance2),
				codeInstanceMemo);
		assertEquals(codeInstanceMemo, codeStore.loadMemo(CodeStore
				.getMemoBasename(this.codeInstance2)));

		// save memo for URI
		codeStore.saveMemo(CodeStore.getMemoBasename(this.locatable2.getUri()),
				locatableMemo);
		assertEquals(locatableMemo, codeStore.loadMemo(CodeStore
				.getMemoBasename(this.locatable2.getUri())));

		/*
		 * load code store #1 as a new one (#2)
		 */
		CodeStore codeStore2 = (CodeStore) this.loadFromCodeStore(codeStore);

		// check code memo
		assertEquals(codeMemo,
				codeStore2.loadMemo(CodeStore.getMemoBasename(this.code1)));

		// check code instance memo
		assertEquals(codeInstanceMemo, codeStore2.loadMemo(CodeStore
				.getMemoBasename(this.codeInstance2)));

		// check URI memo
		assertEquals(locatableMemo, codeStore2.loadMemo(CodeStore
				.getMemoBasename(this.locatable2.getUri())));

		/*
		 * remove code from code store #1 and check #2
		 */
		codeStore.removeAndSaveCode(this.code1, true);

		// code memo no more in #1
		assertNull(codeStore.loadMemo(CodeStore.getMemoBasename(this.code1)));

		// code instance memo no more in #1
		assertNull(codeStore.loadMemo(CodeStore
				.getMemoBasename(this.codeInstance2)));

		// URI memo still in #1
		assertEquals(locatableMemo, codeStore2.loadMemo(CodeStore
				.getMemoBasename(this.locatable2.getUri())));

		// manually delete URI memo
		codeStore.saveMemo(CodeStore.getMemoBasename(this.locatable2.getUri()),
				null);
		assertNull(codeStore2.loadMemo(CodeStore
				.getMemoBasename(this.locatable2.getUri())));
	}
}
