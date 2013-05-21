package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.net.URISyntaxException;
import java.security.InvalidParameterException;

import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.Rule;
import org.junit.Test;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;

public class CodeStoreSaveTest extends CodeStoreHelper {

	@Rule
	public JUnitRuleMockery context = new JUnitRuleMockery() {
		{
			setThreadingPolicy(new Synchroniser());
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
	public void getInvalidBasenameCodeable() {
		CodeStore.getMemoBasename((ILocatable) null);
	}

	@Test
	public void getBasename() {
		assertEquals("code_234233209", CodeStore.getMemoBasename(this.code1));
		assertEquals("code_9908372", CodeStore.getMemoBasename(this.code2));
		assertEquals("codeInstance_sua%3A%2F%2FcodeInstance1",
				CodeStore.getMemoBasename(this.codeable1));
		assertEquals("codeInstance_sua%3A%2F%2FcodeInstance2",
				CodeStore.getMemoBasename(this.codeable2));
		assertEquals("codeInstance_sua%3A%2F%2FcodeInstance3",
				CodeStore.getMemoBasename(this.codeable3));
		assertEquals("codeInstance_9908372_sua%3A%2F%2FcodeInstance1",
				CodeStore.getMemoBasename(this.codeInstance1));
		assertEquals("codeInstance_234233209_sua%3A%2F%2FcodeInstance2",
				CodeStore.getMemoBasename(this.codeInstance2));
		assertEquals("codeInstance_9908372_sua%3A%2F%2FcodeInstance3",
				CodeStore.getMemoBasename(this.codeInstance3));
	}

	@Test
	public void testSaveMemo() throws Exception {
		CodeStore codeStore = (CodeStore) getSmallCodeStore();
		codeStore.saveMemo(CodeStore.getMemoBasename(this.code1),
				"Code: Lorem ipsum");
		assertEquals("Code: Lorem ipsum",
				codeStore.loadMemo(CodeStore.getMemoBasename(this.code1)));
		codeStore.saveMemo(CodeStore.getMemoBasename(this.codeInstance2),
				"Code Instance:\nLorem ipsum");
		assertEquals("Code Instance:\nLorem ipsum",
				codeStore.loadMemo(CodeStore
						.getMemoBasename(this.codeInstance2)));
		codeStore.saveMemo(CodeStore.getMemoBasename(this.codeable2),
				"Codeable:\nĽốґểм ĭрŝũო");
		assertEquals("Codeable:\nĽốґểм ĭрŝũო",
				codeStore.loadMemo(CodeStore.getMemoBasename(this.codeable2)));

		CodeStore codeStore2 = (CodeStore) loadFromCodeStore(codeStore);
		assertEquals("Code: Lorem ipsum",
				codeStore2.loadMemo(CodeStore.getMemoBasename(this.code1)));
		assertEquals("Code Instance:\nLorem ipsum",
				codeStore2.loadMemo(CodeStore
						.getMemoBasename(this.codeInstance2)));
		assertEquals("Codeable:\nĽốґểм ĭрŝũო",
				codeStore2.loadMemo(CodeStore.getMemoBasename(this.codeable2)));

		codeStore.removeAndSaveCode(code1, true);
		assertNull(codeStore.loadMemo(CodeStore.getMemoBasename(this.code1)));
		assertNull(codeStore.loadMemo(CodeStore
				.getMemoBasename(this.codeInstance2)));
		assertEquals("Codeable:\nĽốґểм ĭрŝũო",
				codeStore2.loadMemo(CodeStore.getMemoBasename(this.codeable2)));

		codeStore.saveMemo(CodeStore.getMemoBasename(this.codeable2), null);
		assertNull(codeStore2.loadMemo(CodeStore
				.getMemoBasename(this.codeable2)));
	}

}
