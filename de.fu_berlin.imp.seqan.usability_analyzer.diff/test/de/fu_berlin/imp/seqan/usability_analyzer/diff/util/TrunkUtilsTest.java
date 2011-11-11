package de.fu_berlin.imp.seqan.usability_analyzer.diff.util;

import java.io.File;
import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Test;

import de.fu_berlin.imp.seqan.usability_analyzer.diff.FileUtils;

public class TrunkUtilsTest {
	@Test
	public void testGetTrunkFile() throws URISyntaxException {
		File file = FileUtils
				.getFile("sandbox/mordor/apps/exastellar/exastellar.cpp");
		Assert.assertTrue(file.exists());

		File trunkDir = FileUtils.getFile(".");
		Assert.assertTrue(trunkDir.exists());

		File trunkFile = TrunkUtils.getTrunkFile(trunkDir,
				"sandbox/mordor/apps/exastellar/exastellar.cpp");
		Assert.assertTrue(trunkFile.exists());
	}
}
