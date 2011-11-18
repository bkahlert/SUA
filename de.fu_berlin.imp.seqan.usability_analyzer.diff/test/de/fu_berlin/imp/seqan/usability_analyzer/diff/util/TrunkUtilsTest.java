package de.fu_berlin.imp.seqan.usability_analyzer.diff.util;

import java.io.File;
import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Test;

import de.fu_berlin.imp.seqan.usability_analyzer.core.util.FileUtils;



public class TrunkUtilsTest {
	@Test
	public void testGetTrunkFile() throws URISyntaxException {
		String filename = "sandbox/mordor/apps/exastellar/exastellar.cpp";

		File file = FileUtils.getFile("trunk/" + filename);
		Assert.assertTrue(file.exists());

		File trunkDir = FileUtils.getFile("trunk");
		Assert.assertTrue(trunkDir.exists());

		File trunkFile = TrunkUtils.getTrunkFile(trunkDir,
				"sandbox/mordor/apps/exastellar/exastellar.cpp");
		Assert.assertTrue(trunkFile.exists());
	}
}
