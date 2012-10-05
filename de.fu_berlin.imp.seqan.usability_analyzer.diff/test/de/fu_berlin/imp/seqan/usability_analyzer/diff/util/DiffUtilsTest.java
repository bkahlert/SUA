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
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.Diff;
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
		final Diff diff = context.mock(Diff.class);
		final DiffRecord diffRecord = context.mock(DiffRecord.class);

		context.checking(new Expectations() {
			{
				allowing(diff).getID();
				will(returnValue(new ID("theID")));

				allowing(diff).getRevision();
				will(returnValue(27837l));

				allowing(diffRecord).getDiffFile();
				will(returnValue(diff));

				allowing(diffRecord).getFilename();
				will(returnValue("this/is/the/path/to/the/file.cpp"));
			}
		});

		ISourceStore diffUtils = new SourceCache(cachedSourcesDirectory);

		// make sure a file really exists
		@SuppressWarnings("unused")
		File tmp = diffUtils.getSourceFile(diffRecord.getDiffFile().getID(),
				diffRecord.getDiffFile().getRevision(),
				diffRecord.getFilename());
		// tmp.createNewFile();
		// diffUtils.setSourceFile(diffRecord.getDiffFile().getID(), diffRecord
		// .getDiffFile().getRevision(), diffRecord.getFilename(), tmp);

		// TODO testen; geht nicht, weil man nur eine temporäre Kopie der Datei
		// erhält und nicht den tatsächlichen Ort; so wird verhindert, das der
		// Life Cycle der Datenhaltung unbeeinträchtigt bleibt
		// Assert.assertEquals(new File(dataDirectory,
		// "sources/theID/27837/this/is/the/path/to/the/file.cpp"),
		// diffUtils.getCachedSourceFile(diffRecord.getDiffFile(),
		// diffRecord.getFilename()));
	}
}
