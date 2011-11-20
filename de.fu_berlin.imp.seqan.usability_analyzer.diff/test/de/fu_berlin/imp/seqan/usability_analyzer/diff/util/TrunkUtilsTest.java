package de.fu_berlin.imp.seqan.usability_analyzer.diff.util;

import java.io.File;
import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Test;

import de.fu_berlin.imp.seqan.usability_analyzer.core.util.FileUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFileManagerTest;

public class TrunkUtilsTest {

	private static final String root = "/"
			+ DiffFileManagerTest.class.getPackage().getName()
					.replace('.', '/') + "/..";

	@Test
	public void testGetTrunkFile() throws URISyntaxException {
		String filename = "sandbox/mordor/apps/exastellar/exastellar.cpp";

		File file = FileUtils.getFile(root + "/trunk/" + filename);
		Assert.assertTrue(file.exists());

		File trunkDir = FileUtils.getFile(root + "/trunk");
		Assert.assertTrue(trunkDir.exists());

		File trunkFile = TrunkUtils.getTrunkFile(trunkDir,
				"sandbox/mordor/apps/exastellar/exastellar.cpp");
		Assert.assertTrue(trunkFile.exists());
	}
}
