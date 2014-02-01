package de.fu_berlin.imp.seqan.usability_analyzer.diff.model.source;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.impl.FileBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.FileUtils;

public class RevisionTest {

	private static final String root = "/"
			+ RevisionTest.class.getPackage().getName().replace('.', '/')
			+ "/../..";

	@Test
	public void testCreateManually() {
		FileBaseDataContainer baseDir = new FileBaseDataContainer(
				FileUtils.getFile(root));
		IDataContainer container = baseDir.getSubContainer("source_static")
				.getSubContainer("1").getSubContainer("sandbox")
				.getSubContainer("my_sandbox").getSubContainer("apps");

		SourceData[] sourceDatas = new SourceData[] {
				new SourceData(container.getSubContainer("first_app")
						.getResource("first_app.cpp")),
				new SourceData(container.getSubContainer("seq_iter")
						.getResource("seq_iter.cpp")) };

		Revision revision = new Revision(sourceDatas);

		int i = 0;
		for (ISourceData sourceData : revision) {
			assertEquals(sourceDatas[i], sourceData);
			i++;
		}
		assertNotNull(revision);
	}

	@Test
	public void testCreateAutomatically() {
		FileBaseDataContainer baseDir = new FileBaseDataContainer(
				FileUtils.getFile(root));
		IDataContainer container = baseDir.getSubContainer("source_static");

		Revision revision = new Revision(container.getSubContainer("1"));

		assertNotNull(revision);
		assertEquals(4, revision.size());
	}
}
