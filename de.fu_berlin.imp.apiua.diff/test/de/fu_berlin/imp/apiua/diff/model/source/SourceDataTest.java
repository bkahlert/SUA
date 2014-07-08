package de.fu_berlin.imp.apiua.diff.model.source;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import de.fu_berlin.imp.apiua.core.model.data.IDataContainer;
import de.fu_berlin.imp.apiua.core.model.data.impl.FileBaseDataContainer;
import de.fu_berlin.imp.apiua.core.util.FileUtils;
import de.fu_berlin.imp.apiua.diff.model.source.SourceData;

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
