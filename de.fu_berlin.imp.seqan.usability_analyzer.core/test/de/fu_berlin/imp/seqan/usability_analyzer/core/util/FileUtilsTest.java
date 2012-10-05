package de.fu_berlin.imp.seqan.usability_analyzer.core.util;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import junit.framework.Assert;

import org.junit.Test;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.FileBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.FileData;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.dataresource.IBaseDataContainer;

public class FileUtilsTest {
	@Test
	public void testReadFirstLine() throws URISyntaxException {
		File file = FileUtils.getFile("data/0meio6dzt3eo1wj7_doclog.txt");
		Assert.assertEquals(
				"2011-09-13T12-05-22	unload	http://www.seqan.de/dddoc/html_devel/SHORTCUT_Peptide_Iterator.html	141.14.249.178	-	0	132	1137	520",
				FileUtils.readFirstLine(file));
	}

	@Test
	public void testReadLastLines() throws URISyntaxException {
		File file = FileUtils.getFile("data/0meio6dzt3eo1wj7_doclog.txt");
		Assert.assertEquals(
				"2011-09-14T09-17-49	unload	http://bkahlert.com/promotion/survey/index.php	141.14.249.178	-	0	270	1031	150",
				FileUtils.readLastLines(file, 1));
	}

	@Test
	public void testReadFromTo() throws URISyntaxException, IOException {
		File file = FileUtils.getFile("data/0meio6dzt3eo1wj7_doclog.txt");
		Assert.assertEquals("2011",
				new String(FileUtils.readBytesFromTo(file, 0, 4)));

		int firstLineLength = "2011-09-13T12-05-22	unload	http://www.seqan.de/dddoc/html_devel/SHORTCUT_Peptide_Iterator.html	141.14.249.178	-	0	132	1137	520"
				.length();
		Assert.assertEquals(
				"\n",
				new String(FileUtils.readBytesFromTo(file, firstLineLength,
						firstLineLength + 1)));

		Assert.assertEquals("\n",
				new String(FileUtils.readBytesFromTo(file, 126, 127)));
		Assert.assertEquals("#PeptideIterator",
				new String(FileUtils.readBytesFromTo(file, 220, 236)));
	}

	@Test
	public void testGetNewlineLengthAt() throws URISyntaxException {
		IBaseDataContainer baseDataContainer = new FileBaseDataContainer(
				FileUtils.getFile("data"));
		FileData file = new FileData(
				baseDataContainer,
				baseDataContainer,
				FileUtils
						.getFile("data/blcdihoxu16s53yo_r00000003_2011-09-13T10-28-07.diff"));
		Assert.assertEquals(0, FileUtils.getNewlineLengthAt(file, 0x3AB4)); // nothing
		Assert.assertEquals(1, FileUtils.getNewlineLengthAt(file, 0x3AB5)); // CR
		Assert.assertEquals(2, FileUtils.getNewlineLengthAt(file, 0x3AB6)); // CR+LF
		Assert.assertEquals(1, FileUtils.getNewlineLengthAt(file, 0x3AB7)); // LF
		Assert.assertEquals(0, FileUtils.getNewlineLengthAt(file, 0x3AB8)); // nothing
	}
}
