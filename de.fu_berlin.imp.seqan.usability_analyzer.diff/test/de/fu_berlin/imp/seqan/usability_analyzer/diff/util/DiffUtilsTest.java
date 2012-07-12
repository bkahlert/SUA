package de.fu_berlin.imp.seqan.usability_analyzer.diff.util;

import java.io.File;

import junit.framework.Assert;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Rule;
import org.junit.Test;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFile;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFileRecord;

public class DiffUtilsTest {

	@Rule
	public JUnitRuleMockery context = new JUnitRuleMockery() {
		{
			setImposteriser(ClassImposteriser.INSTANCE);
			setThreadingPolicy(new Synchroniser());
		}
	};

	public static final File dataDirectory = new File("/log_directory");
	public static final File cachedSourcesDirectory = new File(
			"/log_directory/sources");

	@Test
	public void testGetSourceDirectory() {
		SourceCache diffUtils = new SourceCache(cachedSourcesDirectory);
		Assert.assertEquals(new File("/log_directory/sources"),
				diffUtils.getSourceCacheDirectory());
	}

	@Test
	public void testGetSourceFile() {
		final DiffFile diffFile = context.mock(DiffFile.class);
		final DiffFileRecord diffFileRecord = context
				.mock(DiffFileRecord.class);

		context.checking(new Expectations() {
			{
				oneOf(diffFile).getId();
				will(returnValue(new ID("theID")));

				oneOf(diffFile).getRevision();
				will(returnValue("0000027837"));

				oneOf(diffFileRecord).getDiffFile();
				will(returnValue(diffFile));

				oneOf(diffFileRecord).getFilename();
				will(returnValue("this/is/the/path/to/the/file.cpp"));
			}
		});

		SourceCache diffUtils = new SourceCache(cachedSourcesDirectory);
		Assert.assertEquals(
				new File(
						"/log_directory/sources/theID/27837/this/is/the/path/to/the/file.cpp"),
				diffUtils.getCachedSourceFile(diffFileRecord.getDiffFile(),
						diffFileRecord.getFilename()));
	}
}
