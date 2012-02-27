package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.impl;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.Code;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceTest;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeStore;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions.CodeStoreReadException;

public class CodeStoreHelper {
	private static File getTempFile() throws IOException {
		File temp = File.createTempFile(CodeServiceTest.class.getSimpleName(),
				".xml");
		temp.deleteOnExit();
		return temp;
	}

	private File empty;
	private File small;

	protected File getEmptyFile() throws IOException {
		File file = getTempFile();
		FileUtils.copyFile(empty, file);
		return file;
	}

	protected File getSmallFile() throws IOException {
		File file = getTempFile();
		FileUtils.copyFile(small, file);
		return file;
	}

	protected ICode code1 = new Code(234233209l, "Code #1");
	protected ICode code2 = new Code(9908372l, "Code #2");
	protected ICodeInstance codeInstance1 = new CodeInstance(code2,
			"dslkjsdjk278sdi", new TimeZoneDate("1984-05-15T14:30:00+02:00"));
	protected ICodeInstance codeInstance2 = new CodeInstance(code1,
			"äk,dskllsödj", new TimeZoneDate("2011-11-11T11:11:11+11:00"));
	protected ICodeInstance codeInstance3 = new CodeInstance(code2, "-20",
			new TimeZoneDate("2002-09-23T23:08:01-04:30"));

	protected ICode[] codes = new ICode[] { code1, code2 };
	protected ICodeInstance[] codeInstances = new ICodeInstance[] {
			codeInstance1, codeInstance2, codeInstance3 };

	public CodeStoreHelper() {
		empty = new File(
				CodeServiceTest.class
						.getResource(
								"/de/fu_berlin/imp/seqan/usability_analyzer/groundedtheory/data/CodeStore.empty.xml")
						.getFile());

		small = new File(
				CodeServiceTest.class
						.getResource(
								"/de/fu_berlin/imp/seqan/usability_analyzer/groundedtheory/data/CodeStore.small.xml")
						.getFile());
	}

	protected void testCodes(ICodeStore codeStore, ICode[] codes)
			throws IOException {
		ICode[] loadedCodes = codeStore.loadCodes();
		Assert.assertEquals(codes.length, loadedCodes.length);

		for (int i = 0, m = codes.length; i < m; i++) {
			Assert.assertEquals(codes[i].getId(), loadedCodes[i].getId());
			Assert.assertEquals(codes[i].getCaption(),
					loadedCodes[i].getCaption());
		}
	}

	protected void testCodeInstances(ICodeStore codeStore,
			ICodeInstance[] codeInstances) throws CodeStoreReadException {
		ICodeInstance[] loadedCodeInstances = codeStore.loadCodeInstances();
		Assert.assertEquals(codeInstances.length, loadedCodeInstances.length);

		for (int i = 0, m = codeInstances.length; i < m; i++) {
			Assert.assertEquals(codeInstances[i].getCode(),
					loadedCodeInstances[i].getCode());
			Assert.assertEquals(codeInstances[i].getId(),
					loadedCodeInstances[i].getId());
			Assert.assertEquals(codeInstances[i].getCreation(),
					loadedCodeInstances[i].getCreation());
		}
	}

	protected ICodeStore getNonExistingCodeStore() throws IOException {
		return new CodeStore(new File("/I/do/not/exist"));
	}

	protected ICodeStore getEmptyCodeStore() throws IOException {
		return new CodeStore(getEmptyFile());
	}

	protected ICodeStore getSmallCodeStore() throws IOException {
		return new CodeStore(getSmallFile());
	}
}
