package de.fu_berlin.imp.seqan.usability_analyzer.diff.util;

import java.io.File;
import java.io.IOException;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Rule;
import org.junit.Test;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.FileBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.FileUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffDataResource;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffRecord;

public class DiffUtilsTest {

	private static final String root = "/"
			+ DiffUtilsTest.class.getPackage().getName().replace('.', '/')
			+ "/..";

	@Rule
	public JUnitRuleMockery context = new JUnitRuleMockery() {
		{
			setImposteriser(ClassImposteriser.INSTANCE);
			setThreadingPolicy(new Synchroniser());
		}
	};

	public static final File dataDirectory = FileUtils.getFile(root);
	public static final FileBaseDataContainer cachedSourcesDirectory = new FileBaseDataContainer(
			dataDirectory);

	@Test
	public void testGetSourceFile() throws IOException {
		final DiffDataResource diffDataResource = context
				.mock(DiffDataResource.class);
		final DiffRecord diffRecord = context.mock(DiffRecord.class);

		context.checking(new Expectations() {
			{
				allowing(diffDataResource).getID();
				will(returnValue(new ID("theID")));

				allowing(diffDataResource).getRevision();
				will(returnValue("0000027837"));

				allowing(diffRecord).getDiffFile();
				will(returnValue(diffDataResource));

				allowing(diffRecord).getFilename();
				will(returnValue("this/is/the/path/to/the/file.cpp"));
			}
		});

		SourceCache diffUtils = new SourceCache(cachedSourcesDirectory);

		// make sure a file really exists
		File tmp = diffUtils.getCachedSourceFile(diffRecord.getDiffFile(),
				diffRecord.getFilename());
		tmp.createNewFile();
		diffUtils.setCachedSourceFile(diffDataResource,
				diffRecord.getFilename(), tmp);

		// TODO testen; geht nicht, weil man nur eine temporäre Kopie der Datei
		// erhält und nicht den tatsächlichen Ort; so wird verhindert, das der
		// Life Cycle der Datenhaltung unbeeinträchtigt bleibt
		// Assert.assertEquals(new File(dataDirectory,
		// "sources/theID/27837/this/is/the/path/to/the/file.cpp"),
		// diffUtils.getCachedSourceFile(diffRecord.getDiffFile(),
		// diffRecord.getFilename()));
	}
}
