package de.fu_berlin.imp.seqan.usability_analyzer.diff.util;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Assert;
import org.junit.Test;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.IdentifierFactory;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.impl.FileBaseDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.FileUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFileDirectoryTest;

public class DiffCacheTest {
	private static final String root = "/"
			+ DiffFileDirectoryTest.class.getPackage().getName()
					.replace('.', '/') + "/..";

	@SuppressWarnings("serial")
	@Test
	public void testDataCache() throws URISyntaxException, IOException {
		new File(FileUtils.getFile(root), "__dataset.txt").createNewFile();
		DiffContainer diffFileDirectory = new DiffContainer(
				new FileBaseDataContainer(FileUtils.getFile(root)));
		diffFileDirectory.scan(null);

		DiffCache diffCache = new DiffCache(diffFileDirectory, 2);
		Assert.assertEquals(0, diffCache.getCachedKeys().size());

		/*
		 * Query 5lpcjqhy0b9yfech
		 */
		Assert.assertNotNull(diffCache.getPayload(
				IdentifierFactory.createFrom("5lpcjqhy0b9yfech"),
				new NullProgressMonitor()));
		Assert.assertEquals(1, diffCache.getCachedKeys().size());
		for (IIdentifier id : new HashSet<IIdentifier>() {
			{
				this.add(IdentifierFactory.createFrom("5lpcjqhy0b9yfech"));
			}
		}) {
			Assert.assertTrue(diffCache.getCachedKeys().contains(id));
		}

		/*
		 * Query amudto8y1mzxaebv
		 */
		Assert.assertNotNull(diffCache.getPayload(
				IdentifierFactory.createFrom("amudto8y1mzxaebv"),
				new NullProgressMonitor()));
		Assert.assertEquals(2, diffCache.getCachedKeys().size());
		for (IIdentifier id : new HashSet<IIdentifier>() {
			{
				this.add(IdentifierFactory.createFrom("5lpcjqhy0b9yfech"));
				this.add(IdentifierFactory.createFrom("amudto8y1mzxaebv"));
			}
		}) {
			Assert.assertTrue(diffCache.getCachedKeys().contains(id));
		}

		/*
		 * Query again 5lpcjqhy0b9yfech
		 */
		Assert.assertNotNull(diffCache.getPayload(
				IdentifierFactory.createFrom("5lpcjqhy0b9yfech"),
				new NullProgressMonitor()));

		/*
		 * Query o6lmo5tpxvn3b6fg amudto8y1mzxaebv has been removed
		 */
		Assert.assertNotNull(diffCache.getPayload(
				IdentifierFactory.createFrom("o6lmo5tpxvn3b6fg"),
				new NullProgressMonitor()));
		Assert.assertEquals(2, diffCache.getCachedKeys().size());
		for (IIdentifier id : new HashSet<IIdentifier>() {
			{
				this.add(IdentifierFactory.createFrom("5lpcjqhy0b9yfech"));
				this.add(IdentifierFactory.createFrom("o6lmo5tpxvn3b6fg"));
			}
		}) {
			Assert.assertTrue(diffCache.getCachedKeys().contains(id));
		}
	}
}
