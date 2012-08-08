package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.impl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

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

	protected ICode code1;
	protected ICode code2;
	protected ICodeInstance codeInstance1;
	protected ICodeInstance codeInstance2;
	protected ICodeInstance codeInstance3;

	protected ICode[] codes;
	protected ICodeInstance[] codeInstances;

	public CodeStoreHelper() throws URISyntaxException {
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

		code1 = new Code(234233209l, "Code #1", new TimeZoneDate());
		code2 = new Code(9908372l, "Code #2", new TimeZoneDate());

		codes = new ICode[] { code1, code2 };

		codeInstance1 = new CodeInstance(code2, new URI("sua://codeInstance1"),
				new TimeZoneDate("1984-05-15T14:30:00+02:00"));
		codeInstance2 = new CodeInstance(code1, new URI("sua://codeInstance2"),
				new TimeZoneDate("2011-11-11T11:11:11+11:00"));
		codeInstance3 = new CodeInstance(code2, new URI("sua://codeInstance3"),
				new TimeZoneDate("2002-09-23T23:08:01-04:30"));

		codeInstances = new ICodeInstance[] { codeInstance1, codeInstance2,
				codeInstance3 };
	}

	/**
	 * Tests whether the {@link ICodeStore} only contains all given
	 * {@link ICode}s.
	 * 
	 * @param codeStore
	 * @param codes
	 * @throws IOException
	 */
	protected void testCodes(ICodeStore codeStore, ICode[] codes)
			throws IOException {
		List<ICode> loadedCodes = codeStore.getTopLevelCodes();
		Assert.assertEquals(codes.length, loadedCodes.size());

		HashMap<ICode, Boolean> inCodeStore = new HashMap<ICode, Boolean>();
		for (ICode loadedCode : loadedCodes)
			inCodeStore.put(loadedCode, false);

		for (ICode loadedCode : loadedCodes) {
			for (ICode code : codes) {
				if (loadedCode.getId() == code.getId()) {
					Assert.assertEquals(code.getCaption(),
							loadedCode.getCaption());
					inCodeStore.put(code, true);
				}
			}
			Assert.assertTrue(ICode.class.getSimpleName() + " " + loadedCode
					+ " is not in the test set", inCodeStore.get(loadedCode));
		}

		for (ICode code : inCodeStore.keySet()) {
			Assert.assertTrue(ICode.class.getSimpleName() + " " + code
					+ " is not in the " + ICodeStore.class.getSimpleName(),
					inCodeStore.get(code));
		}
	}

	protected void testCodeInstances(ICodeStore actualCodeInstances,
			ICodeInstance[] expectedCodeInstances)
			throws CodeStoreReadException {
		testCodeInstances(actualCodeInstances.loadInstances(),
				expectedCodeInstances);
	}

	protected void testCodeInstances(Set<ICodeInstance> actualCodeInstances,
			ICodeInstance[] expectedCodeInstances)
			throws CodeStoreReadException {
		Assert.assertEquals(expectedCodeInstances.length,
				actualCodeInstances.size());

		HashMap<ICodeInstance, Boolean> inCodeStore = new HashMap<ICodeInstance, Boolean>();
		for (ICodeInstance loadedInstance : actualCodeInstances)
			inCodeStore.put(loadedInstance, false);

		for (ICodeInstance loadedInstance : actualCodeInstances) {
			for (ICodeInstance instance : expectedCodeInstances) {
				if (loadedInstance.getId().equals(instance.getId())) {
					Assert.assertEquals(instance.getCode(),
							loadedInstance.getCode());
					Assert.assertEquals(instance.getId(),
							loadedInstance.getId());
					Assert.assertEquals(instance.getCreation(),
							loadedInstance.getCreation());
					inCodeStore.put(loadedInstance, true);
				}
			}
			Assert.assertTrue(ICodeInstance.class.getSimpleName() + " "
					+ loadedInstance + " is not in the test set",
					inCodeStore.get(loadedInstance));
		}

		for (ICodeInstance instance : inCodeStore.keySet()) {
			Assert.assertTrue(
					ICodeInstance.class.getSimpleName() + " " + instance
							+ " is not in the "
							+ ICodeStore.class.getSimpleName(),
					inCodeStore.get(instance));
		}
	}

	protected ICodeStore getNonExistingCodeStore() throws IOException {
		return CodeStore.load(new File("/I/do/not/exist"));
	}

	protected ICodeStore getEmptyCodeStore() throws IOException {
		return CodeStore.create(getEmptyFile());
	}

	protected ICodeStore getSmallCodeStore() throws IOException {
		return CodeStore.load(getSmallFile());
	}

	protected ICodeStore loadFromCodeStore(ICodeStore codeStore)
			throws IOException {
		codeStore.save();
		return CodeStore.load(((CodeStore) codeStore).getCodeStoreFile());
	}
}
