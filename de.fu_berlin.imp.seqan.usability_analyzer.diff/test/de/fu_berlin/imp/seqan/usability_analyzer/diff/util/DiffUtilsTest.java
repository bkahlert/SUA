package de.fu_berlin.imp.seqan.usability_analyzer.diff.util;

import java.io.File;
import java.io.IOException;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Rule;
import org.junit.Test;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.IdentifierFactory;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.impl.FileBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.FileUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.impl.Diff;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.impl.DiffRecord;

public class DiffUtilsTest {

	private static final String root = "/"
			+ DiffUtilsTest.class.getPackage().getName().replace('.', '/')
			+ "/..";

	@Rule
	public JUnitRuleMockery context = new JUnitRuleMockery() {
		{
			this.setImposteriser(ClassImposteriser.INSTANCE);
			this.setThreadingPolicy(new Synchroniser());
		}
	};

	public static final File dataDirectory = FileUtils.getFile(root);
	public static final FileBaseDataContainer cachedSourcesDirectory = new FileBaseDataContainer(
			dataDirectory);

	@Test
	public void testGetSourceFile() throws IOException {
		final Diff diff = this.context.mock(Diff.class);
		final DiffRecord diffRecord = this.context.mock(DiffRecord.class);

		this.context.checking(new Expectations() {
			{
				this.allowing(diff).getIdentifier();
				this.will(returnValue(IdentifierFactory.createFrom("theID")));

				this.allowing(diff).getRevision();
				this.will(returnValue("27837"));

				this.allowing(diffRecord).getDiffFile();
				this.will(returnValue(diff));

				this.allowing(diffRecord).getFilename();
				this.will(returnValue("this/is/the/path/to/the/file.cpp"));
			}
		});

		ISourceStore diffUtils = new SourceCache(cachedSourcesDirectory);

		// make sure a file really exists
		@SuppressWarnings("unused")
		File tmp = diffUtils.getSourceFile(diffRecord.getDiffFile()
				.getIdentifier(), diffRecord.getDiffFile().getRevision(),
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
