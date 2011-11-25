package de.fu_berlin.imp.seqan.usability_analyzer.core.util;

import java.io.File;
import java.net.URISyntaxException;

import junit.framework.Assert;

import org.junit.Test;

public class FileUtilsTest {
	@Test
	public void testGetFirstLine() throws URISyntaxException {
		File file = FileUtils.getFile("data/0meio6dzt3eo1wj7_doclog.txt");
		Assert.assertEquals(
				"2011-09-13T12-05-22	unload	http://www.seqan.de/dddoc/html_devel/SHORTCUT_Peptide_Iterator.html	141.14.249.178	-	0	132	1137	520",
				FileUtils.readFirstLine(file));
	}

	@Test
	public void testGetLastLines() throws URISyntaxException {
		File file = FileUtils.getFile("data/0meio6dzt3eo1wj7_doclog.txt");
		Assert.assertEquals(
				"2011-09-14T09-17-49	unload	http://bkahlert.com/promotion/survey/index.php	141.14.249.178	-	0	270	1031	150",
				FileUtils.readLastLines(file, 1));
	}
}
