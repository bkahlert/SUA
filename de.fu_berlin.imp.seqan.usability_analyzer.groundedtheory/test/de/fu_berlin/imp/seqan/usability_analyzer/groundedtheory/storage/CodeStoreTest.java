package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage;

import java.io.IOException;
import java.security.InvalidParameterException;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;

import org.apache.commons.lang.ArrayUtils;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.Rule;
import org.junit.Test;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.Code;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.CodeInstanceID;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeInstanceID;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeDoesNotExistException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeInstanceDoesNotExistException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeStoreFullException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeStoreReadException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeStoreWriteAbandonedCodeInstancesException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeStoreWriteException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.impl.CodeStoreHelper;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.impl.DuplicateCodeInstanceException;

public class CodeStoreTest extends CodeStoreHelper {

	@Rule
	public JUnitRuleMockery context = new JUnitRuleMockery() {
		{
			setThreadingPolicy(new Synchroniser());
		}
	};

	public CodeStoreTest() throws IOException {
		super();
	}

	@Test(expected = CodeStoreReadException.class)
	public void testNonExistingLoadCodes() throws IOException,
			SAXParseException {
		getNonExistingCodeStore().loadCodes();
	}

	@Test(expected = CodeStoreReadException.class)
	public void testNonExistingLoadCodeInstances() throws IOException,
			SAXParseException {
		getNonExistingCodeStore().loadCodeInstances();
	}

	@Test
	public void testEmptyLoadCodes() throws IOException, SAXParseException {
		ICode[] loadedCodes = getEmptyCodeStore().loadCodes();
		Assert.assertEquals(0, loadedCodes.length);
	}

	@Test
	public void testEmptyLoadCodeInstances() throws IOException,
			SAXParseException {
		ICodeInstance[] loadedCodeInstances = getEmptyCodeStore()
				.loadCodeInstances();
		Assert.assertEquals(0, loadedCodeInstances.length);
	}

	@Test
	public void testLoadCodes() throws IOException {
		testCodes(getSmallCodeStore(), codes);
	}

	@Test
	public void testLoadCodeInstances() throws IOException, SAXParseException {
		testCodeInstances(getSmallCodeStore(), codeInstances);
	}

	@Test
	public void testNewFileSave() throws IOException, SAXException,
			ParserConfigurationException {
		ICodeStore newCodeStore = getEmptyCodeStore();
		newCodeStore.save(codes, codeInstances);
		testCodes(newCodeStore, codes);
		testCodeInstances(newCodeStore, codeInstances);
	}

	@Test
	public void testNewFileSaveCodes() throws IOException, SAXException,
			ParserConfigurationException {
		ICodeStore newCodeStore = getEmptyCodeStore();
		newCodeStore.saveCodes(codes);
		testCodes(newCodeStore, codes);
	}

	@Test
	public void testNewFileSaveCodeInstances() throws IOException {
		ICodeStore newCodeStore = getEmptyCodeStore();
		newCodeStore.saveCodes(new ICode[] { code2 });
		newCodeStore.saveCodeInstances(new ICodeInstance[] { codeInstance1,
				codeInstance3 });

		Assert.assertEquals(2, newCodeStore.loadCodeInstances().length);
		testCodeInstances(newCodeStore, new ICodeInstance[] { codeInstance1,
				codeInstance3 });
	}

	@Test(expected = CodeStoreWriteException.class)
	public void testNewFileSaveCodeInstancesWithoutCodes() throws IOException,
			SAXException, ParserConfigurationException {
		ICodeStore newCodeStore = getEmptyCodeStore();
		newCodeStore.saveCodeInstances(new ICodeInstance[] { codeInstance1,
				codeInstance3, codeInstance2 });
	}

	@Test
	public void testNewFileAddAndSaveCodeInstances() throws IOException {
		ICodeStore newCodeStore = getEmptyCodeStore();
		newCodeStore.addAndSaveCode(code2);
		newCodeStore.addAndSaveCodeInstance(codeInstance1);
		newCodeStore.addAndSaveCodeInstance(codeInstance3);

		Assert.assertEquals(2, newCodeStore.loadCodeInstances().length);
		testCodeInstances(newCodeStore, new ICodeInstance[] { codeInstance1,
				codeInstance3 });

		newCodeStore.addAndSaveCode(code2);
		Assert.assertEquals(2, newCodeStore.loadCodeInstances().length);
		testCodeInstances(newCodeStore, new ICodeInstance[] { codeInstance1,
				codeInstance3 });
	}

	@Test
	public void testSmallFileSaveCodes() throws IOException {
		ICodeStore codeStore = getSmallCodeStore();

		testCodes(codeStore, codes);
		testCodeInstances(codeStore, codeInstances);

		ICode code3 = new Code(42l, "solution");

		codeStore.addAndSaveCode(code3);
		Assert.assertEquals(3, codeStore.loadCodes().length);
		testCodes(codeStore, new ICode[] { code1, code2, code3 });
		Assert.assertEquals(3, codeStore.loadCodeInstances().length);
		testCodeInstances(codeStore, codeInstances);

		codeStore.saveCodes(new ICode[] { code1, code2 });
		Assert.assertEquals(2, codeStore.loadCodes().length);
		testCodes(codeStore, new ICode[] { code1, code2 });
		Assert.assertEquals(3, codeStore.loadCodeInstances().length);
		testCodeInstances(codeStore, codeInstances);
	}

	@Test(expected = CodeStoreWriteAbandonedCodeInstancesException.class)
	public void testSmallFileSaveCodesMakingInstancesInvalid()
			throws IOException {
		ICodeStore codeStore = getSmallCodeStore();
		testCodes(codeStore, codes);
		testCodeInstances(codeStore, codeInstances);

		codeStore.saveCodes(new ICode[] { code2 });
	}

	@Test
	public void testSmallFileSaveCodeInstances() throws IOException {
		ICodeStore codeStore = getSmallCodeStore();
		testCodes(codeStore, codes);
		testCodeInstances(codeStore, codeInstances);

		codeStore.saveCodeInstances(new ICodeInstance[] { codeInstance1,
				codeInstance3 });
		Assert.assertEquals(2, codeStore.loadCodes().length);
		testCodes(codeStore, codes);
		Assert.assertEquals(2, codeStore.loadCodeInstances().length);
		testCodeInstances(codeStore, new ICodeInstance[] { codeInstance1,
				codeInstance3 });

		codeStore.saveCodes(new ICode[] { code2 });
		Assert.assertEquals(1, codeStore.loadCodes().length);
		testCodes(codeStore, new ICode[] { code2 });
		Assert.assertEquals(2, codeStore.loadCodeInstances().length);
		testCodeInstances(codeStore, new ICodeInstance[] { codeInstance1,
				codeInstance3 });

		codeStore.saveCodeInstances(new ICodeInstance[] { codeInstance1 });
		Assert.assertEquals(1, codeStore.loadCodes().length);
		testCodes(codeStore, new ICode[] { code2 });
		Assert.assertEquals(1, codeStore.loadCodeInstances().length);
		testCodeInstances(codeStore, new ICodeInstance[] { codeInstance1 });

		codeStore.addAndSaveCodeInstance(codeInstance3);
		Assert.assertEquals(1, codeStore.loadCodes().length);
		testCodes(codeStore, new ICode[] { code2 });
		Assert.assertEquals(2, codeStore.loadCodeInstances().length);
		testCodeInstances(codeStore, new ICodeInstance[] { codeInstance1,
				codeInstance3 });
	}

	@Test(expected = CodeStoreWriteAbandonedCodeInstancesException.class)
	public void testSmallFileSaveCodeInstancesMakingInstancesInvalid()
			throws IOException {
		ICodeStore codeStore = getSmallCodeStore();
		testCodes(codeStore, codes);
		testCodeInstances(codeStore, codeInstances);

		codeStore.saveCodeInstances(new ICodeInstance[] { codeInstance1,
				codeInstance3 });
		Assert.assertEquals(2, codeStore.loadCodes().length);
		testCodes(codeStore, codes);
		Assert.assertEquals(2, codeStore.loadCodeInstances().length);
		testCodeInstances(codeStore, new ICodeInstance[] { codeInstance1,
				codeInstance3 });

		codeStore.saveCodes(new ICode[] { code2 });
		Assert.assertEquals(1, codeStore.loadCodes().length);
		testCodes(codeStore, new ICode[] { code2 });
		Assert.assertEquals(2, codeStore.loadCodeInstances().length);
		testCodeInstances(codeStore, new ICodeInstance[] { codeInstance1,
				codeInstance3 });

		codeStore.saveCodeInstances(new ICodeInstance[] { codeInstance1,
				codeInstance2, codeInstance3 });
	}

	@Test
	public void testCreateCode() throws IOException, CodeStoreFullException {
		ICodeStore codeStore = getEmptyCodeStore();
		Assert.assertEquals(0, codeStore.loadCodes().length);
		Assert.assertEquals(0, codeStore.loadCodeInstances().length);

		ICode code = codeStore.createCode("Code #1");
		Assert.assertEquals(Long.MIN_VALUE, code.getId());
		Assert.assertEquals("Code #1", code.getCaption());
	}

	@Test
	public void testNonExistingCreateCode() throws IOException,
			CodeStoreFullException {
		ICodeStore codeStore = getEmptyCodeStore();
		Assert.assertEquals(0, codeStore.loadCodes().length);
		Assert.assertEquals(0, codeStore.loadCodeInstances().length);

		Assert.assertEquals(new Code(Long.MIN_VALUE, "Code #1"),
				codeStore.createCode("Code #1"));
		Assert.assertEquals(new Code(Long.MIN_VALUE + 1, "Code #2"),
				codeStore.createCode("Code #2"));
		codeStore.saveCodes(new ICode[] { new Code(5l, "Code #3") });
		Assert.assertEquals(new Code(6l, "Code #4"),
				codeStore.createCode("Code #4"));
	}

	@Test
	public void testEmptyCreateCode() throws IOException,
			CodeStoreFullException {
		ICodeStore codeStore = getEmptyCodeStore();
		Assert.assertEquals(0, codeStore.loadCodes().length);
		Assert.assertEquals(0, codeStore.loadCodeInstances().length);

		Assert.assertEquals(new Code(Long.MIN_VALUE, "Code #1"),
				codeStore.createCode("Code #1"));
		Assert.assertEquals(new Code(Long.MIN_VALUE + 1, "Code #2"),
				codeStore.createCode("Code #2"));
		codeStore.saveCodes(new ICode[] { new Code(5l, "Code #3") });
		Assert.assertEquals(new Code(6l, "Code #4"),
				codeStore.createCode("Code #4"));
	}

	@Test
	public void testSmallCreateCode() throws IOException,
			CodeStoreFullException {
		ICodeStore codeStore = getSmallCodeStore();
		testCodes(codeStore, codes);
		testCodeInstances(codeStore, codeInstances);

		Assert.assertEquals(new Code(234233209l + 1l, "Code #1"),
				codeStore.createCode("Code #1"));
		Assert.assertEquals(new Code(234233209l + 2l, "Code #2"),
				codeStore.createCode("Code #2"));
		codeStore.saveCodes((ICode[]) ArrayUtils.add(codeStore.loadCodes(),
				new Code(300000000l, "Code #3")));
		Assert.assertEquals(new Code(300000001l, "Code #4"),
				codeStore.createCode("Code #4"));
	}

	@Test(expected = CodeStoreFullException.class)
	public void testOverflowCreateCode() throws IOException,
			CodeStoreFullException {
		ICodeStore codeStore = getSmallCodeStore();
		testCodes(codeStore, codes);
		testCodeInstances(codeStore, codeInstances);

		codeStore.saveCodes((ICode[]) ArrayUtils.add(codeStore.loadCodes(),
				new Code(Long.MAX_VALUE, "Code #1")));
		codeStore.createCode("Code #2");
	}

	@Test(expected = InvalidParameterException.class)
	public void testNonExistingCreateCodeInstance() throws IOException,
			InvalidParameterException, DuplicateCodeInstanceException {
		final ICode code = context.mock(ICode.class);
		final ICodeable codeable = context.mock(ICodeable.class);
		context.checking(new Expectations() {
			{
				allowing(code).getId();
				will(returnValue(code1.getId()));

				allowing(codeable).getCodeInstanceId();
				will(returnValue("my_id"));
			}
		});

		ICodeStore codeStore = getEmptyCodeStore();
		Assert.assertEquals(0, codeStore.loadCodes().length);
		Assert.assertEquals(0, codeStore.loadCodeInstances().length);

		codeStore.createCodeInstance(code, codeable);
	}

	@Test(expected = InvalidParameterException.class)
	public void testEmptyCreateCodeInstance() throws IOException,
			InvalidParameterException, DuplicateCodeInstanceException {
		final ICode code = context.mock(ICode.class);
		final ICodeable codeable = context.mock(ICodeable.class);
		context.checking(new Expectations() {
			{
				allowing(code).getId();
				will(returnValue(code1.getId()));

				allowing(codeable).getCodeInstanceId();
				will(returnValue("my_id"));
			}
		});

		ICodeStore codeStore = getEmptyCodeStore();
		Assert.assertEquals(0, codeStore.loadCodes().length);
		Assert.assertEquals(0, codeStore.loadCodeInstances().length);

		codeStore.createCodeInstance(code, codeable);
	}

	@Test
	public void testSmallCreateCodeInstance() throws IOException,
			InvalidParameterException, DuplicateCodeInstanceException {
		final ICodeable codeable = context.mock(ICodeable.class);
		context.checking(new Expectations() {
			{
				allowing(codeable).getCodeInstanceId();
				will(returnValue(CodeInstanceID.createRaw("my_id")));
			}
		});

		ICodeStore codeStore = getSmallCodeStore();
		testCodes(codeStore, codes);
		testCodeInstances(codeStore, codeInstances);

		final ICodeInstance codeInstance = codeStore.createCodeInstance(code1,
				codeable);
		codeStore.addAndSaveCodeInstance(codeInstance);
		testCodes(codeStore, codes);
		testCodeInstances(codeStore, new ICodeInstance[] { codeInstance1,
				codeInstance2, codeInstance3, new ICodeInstance() {

					@Override
					public ICode getCode() {
						return codeInstance.getCode();
					}

					@Override
					public ICodeInstanceID getId() {
						return codeInstance.getId();
					}

					@Override
					public TimeZoneDate getCreation() {
						TimeZoneDate creation = codeInstance.getCreation();
						long millisecondPortion = creation.getTime() % 1000l;
						creation.addMilliseconds(-millisecondPortion);
						return creation;
					}
				} });
	}

	@Test(expected = CodeInstanceDoesNotExistException.class)
	public void testSmallDeleteInexistingCodeInstance() throws Exception {
		final ICodeInstance codeInstance = context.mock(ICodeInstance.class);
		context.checking(new Expectations() {
			{
				allowing(codeInstance).getId();
				will(returnValue("my_id"));
			}
		});

		ICodeStore codeStore = getSmallCodeStore();
		testCodes(codeStore, codes);
		testCodeInstances(codeStore, codeInstances);

		codeStore.deleteCodeInstance(codeInstance);
	}

	@Test
	public void testSmallDeleteExistingCodeInstance() throws Exception {
		ICodeStore codeStore = getSmallCodeStore();

		testCodes(codeStore, new ICode[] { code1, code2 });
		testCodeInstances(codeStore, new ICodeInstance[] { codeInstance1,
				codeInstance2, codeInstance3 });

		codeStore.deleteCodeInstance(codeInstance1);

		testCodes(codeStore, new ICode[] { code1, code2 });
		testCodeInstances(codeStore, new ICodeInstance[] { codeInstance2,
				codeInstance3 });

		codeStore.deleteCodeInstance(codeInstance2);

		testCodes(codeStore, new ICode[] { code1, code2 });
		testCodeInstances(codeStore, new ICodeInstance[] { codeInstance3 });

		codeStore.deleteCodeInstance(codeInstance3);

		testCodes(codeStore, new ICode[] { code1, code2 });
		testCodeInstances(codeStore, new ICodeInstance[] {});
	}

	@Test
	public void testEmptyDeleteCodeInstances() throws IOException,
			InvalidParameterException, DuplicateCodeInstanceException {
		final ICode code = context.mock(ICode.class);
		context.checking(new Expectations() {
			{
				allowing(code).getId();
				will(returnValue(code1.getId()));
			}
		});

		ICodeStore codeStore = getEmptyCodeStore();
		Assert.assertEquals(0, codeStore.loadCodes().length);
		Assert.assertEquals(0, codeStore.loadCodeInstances().length);

		codeStore.deleteCodeInstances(code);

		Assert.assertEquals(0, codeStore.loadCodes().length);
		Assert.assertEquals(0, codeStore.loadCodeInstances().length);
	}

	@Test
	public void testSmallDeleteInexistingCodeInstances() throws IOException,
			InvalidParameterException, DuplicateCodeInstanceException {
		final ICode code = context.mock(ICode.class);
		context.checking(new Expectations() {
			{
				allowing(code).getId();
				will(returnValue(-1l));
			}
		});

		ICodeStore codeStore = getSmallCodeStore();
		testCodes(codeStore, codes);
		testCodeInstances(codeStore, codeInstances);

		codeStore.deleteCodeInstances(code);

		testCodes(codeStore, codes);
		testCodeInstances(codeStore, codeInstances);
	}

	@Test
	public void testSmallDeleteExistingCodeInstances() throws IOException,
			InvalidParameterException, DuplicateCodeInstanceException {
		ICodeStore codeStore = getSmallCodeStore();
		testCodes(codeStore, new ICode[] { code1, code2 });
		testCodeInstances(codeStore, new ICodeInstance[] { codeInstance1,
				codeInstance2, codeInstance3 });

		codeStore.deleteCodeInstances(code2);

		testCodes(codeStore, new ICode[] { code1, code2 });
		testCodeInstances(codeStore, new ICodeInstance[] { codeInstance2 });
	}

	@Test(expected = CodeDoesNotExistException.class)
	public void testEmptyDeleteInexistingCode() throws Exception {
		final ICode code = context.mock(ICode.class);
		context.checking(new Expectations() {
			{
				allowing(code).getId();
				will(returnValue(-1l));
			}
		});

		ICodeStore codeStore = getEmptyCodeStore();
		Assert.assertEquals(0, codeStore.loadCodes().length);
		Assert.assertEquals(0, codeStore.loadCodeInstances().length);

		codeStore.deleteCode(code);
	}

	@Test
	public void testSmallDeleteExistingCode() throws Exception {
		ICodeStore codeStore = getSmallCodeStore();
		testCodes(codeStore, new ICode[] { code1, code2 });
		testCodeInstances(codeStore, new ICodeInstance[] { codeInstance1,
				codeInstance2, codeInstance3 });

		codeStore.deleteCode(code2);

		testCodes(codeStore, new ICode[] { code1 });
		testCodeInstances(codeStore, new ICodeInstance[] { codeInstance2 });
	}
}
