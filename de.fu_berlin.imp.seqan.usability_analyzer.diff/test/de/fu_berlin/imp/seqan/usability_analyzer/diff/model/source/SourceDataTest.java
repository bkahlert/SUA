package de.fu_berlin.imp.seqan.usability_analyzer.diff.model.source;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.impl.FileBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.FileUtils;

public class SourceDataTest {

	private static final String root = "/"
			+ SourceDataTest.class.getPackage().getName().replace('.', '/')
			+ "/../..";

	@Test
	public void test() {
		FileBaseDataContainer baseDir = new FileBaseDataContainer(
				FileUtils.getFile(root));
		IDataContainer container = baseDir.getSubContainer("source_static")
				.getSubContainer("1").getSubContainer("sandbox")
				.getSubContainer("my_sandbox").getSubContainer("apps")
				.getSubContainer("first_app");
		SourceData sourceData = new SourceData(
				container.getResource("first_app.cpp"));

		assertNotNull(sourceData);
	}

}
