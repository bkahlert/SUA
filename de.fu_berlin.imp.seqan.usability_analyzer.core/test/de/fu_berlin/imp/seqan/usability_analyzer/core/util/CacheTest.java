package de.fu_berlin.imp.seqan.usability_analyzer.core.util;

import java.util.HashMap;
import java.util.HashSet;

import junit.framework.Assert;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.IdentifierFactory;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.Cache.CacheFetcher;

public class CacheTest {
	@SuppressWarnings("serial")
	@Test
	public void testCache() {
		final HashMap<IIdentifier, String> db = new HashMap<IIdentifier, String>() {
			{
				this.put(IdentifierFactory.createFrom("a"), "aa");
				this.put(IdentifierFactory.createFrom("b"), "bb");
				this.put(IdentifierFactory.createFrom("c"), "cc");
				this.put(IdentifierFactory.createFrom("d"), "dd");
			}
		};

		Cache<IIdentifier, String> cache = new Cache<IIdentifier, String>(
				new CacheFetcher<IIdentifier, String>() {
					@Override
					public String fetch(IIdentifier key,
							IProgressMonitor progressMonitor) {
						return db.get(key);
					}
				}, 3);

		Assert.assertEquals("aa", cache.getPayload(
				IdentifierFactory.createFrom("a"), new NullProgressMonitor()));
		Assert.assertEquals(1, cache.getCachedKeys().size());
		for (IIdentifier id : new HashSet<IIdentifier>() {
			{
				this.add(IdentifierFactory.createFrom("a"));
			}
		}) {
			Assert.assertTrue(id + " not found", cache.getCachedKeys()
					.contains(id));
		}

		Assert.assertEquals("aa", cache.getPayload(
				IdentifierFactory.createFrom("a"), new NullProgressMonitor()));
		Assert.assertEquals(1, cache.getCachedKeys().size());
		for (IIdentifier id : new HashSet<IIdentifier>() {
			{
				this.add(IdentifierFactory.createFrom("a"));
			}
		}) {
			Assert.assertTrue(id + " not found", cache.getCachedKeys()
					.contains(id));
		}

		Assert.assertEquals("bb", cache.getPayload(
				IdentifierFactory.createFrom("b"), new NullProgressMonitor()));
		Assert.assertEquals(2, cache.getCachedKeys().size());
		for (IIdentifier id : new HashSet<IIdentifier>() {
			{
				this.add(IdentifierFactory.createFrom("a"));
				this.add(IdentifierFactory.createFrom("b"));
			}
		}) {
			Assert.assertTrue(id + " not found", cache.getCachedKeys()
					.contains(id));
		}

		Assert.assertEquals("bb", cache.getPayload(
				IdentifierFactory.createFrom("b"), new NullProgressMonitor()));
		Assert.assertEquals(2, cache.getCachedKeys().size());
		for (IIdentifier id : new HashSet<IIdentifier>() {
			{
				this.add(IdentifierFactory.createFrom("a"));
				this.add(IdentifierFactory.createFrom("b"));
			}
		}) {
			Assert.assertTrue(id + " not found", cache.getCachedKeys()
					.contains(id));
		}

		Assert.assertEquals("cc", cache.getPayload(
				IdentifierFactory.createFrom("c"), new NullProgressMonitor()));
		Assert.assertEquals(3, cache.getCachedKeys().size());
		for (IIdentifier id : new HashSet<IIdentifier>() {
			{
				this.add(IdentifierFactory.createFrom("a"));
				this.add(IdentifierFactory.createFrom("b"));
				this.add(IdentifierFactory.createFrom("c"));
			}
		}) {
			Assert.assertTrue(id + " not found", cache.getCachedKeys()
					.contains(id));
		}

		Assert.assertEquals("dd", cache.getPayload(
				IdentifierFactory.createFrom("d"), new NullProgressMonitor()));
		Assert.assertEquals(3, cache.getCachedKeys().size());
		for (IIdentifier id : new HashSet<IIdentifier>() {
			{
				this.add(IdentifierFactory.createFrom("a"));
				this.add(IdentifierFactory.createFrom("b"));
				this.add(IdentifierFactory.createFrom("d"));
			}
		}) {
			Assert.assertTrue(id + " not found", cache.getCachedKeys()
					.contains(id));
		}

		Assert.assertEquals("cc", cache.getPayload(
				IdentifierFactory.createFrom("c"), new NullProgressMonitor()));
		Assert.assertEquals(3, cache.getCachedKeys().size());
		for (IIdentifier id : new HashSet<IIdentifier>() {
			{
				this.add(IdentifierFactory.createFrom("a"));
				this.add(IdentifierFactory.createFrom("b"));
				this.add(IdentifierFactory.createFrom("c"));
			}
		}) {
			Assert.assertTrue(id + " not found", cache.getCachedKeys()
					.contains(id));
		}

		Assert.assertNull(cache.getPayload(IdentifierFactory.createFrom("x"),
				new NullProgressMonitor()));
		Assert.assertEquals(3, cache.getCachedKeys().size());
		for (IIdentifier id : new HashSet<IIdentifier>() {
			{
				this.add(IdentifierFactory.createFrom("a"));
				this.add(IdentifierFactory.createFrom("b"));
				this.add(IdentifierFactory.createFrom("x"));
			}
		}) {
			Assert.assertTrue(id + " not found", cache.getCachedKeys()
					.contains(id));
		}
	}
}
