package de.fu_berlin.imp.seqan.usability_analyzer.core.util;

import java.util.HashMap;
import java.util.HashSet;

import junit.framework.Assert;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.Cache.CacheFetcher;

public class CacheTest {
	@SuppressWarnings("serial")
	@Test
	public void testCache() {
		final HashMap<ID, String> db = new HashMap<ID, String>() {
			{
				put(new ID("a"), "aa");
				put(new ID("b"), "bb");
				put(new ID("c"), "cc");
				put(new ID("d"), "dd");
			}
		};

		Cache<ID, String> cache = new Cache<ID, String>(
				new CacheFetcher<ID, String>() {
					@Override
					public String fetch(ID key, IProgressMonitor progressMonitor) {
						return db.get(key);
					}
				}, 3);

		Assert.assertEquals("aa",
				cache.getPayload(new ID("a"), new NullProgressMonitor()));
		Assert.assertEquals(1, cache.getCachedKeys().size());
		for (ID id : new HashSet<ID>() {
			{
				add(new ID("a"));
			}
		}) {
			Assert.assertTrue(id + " not found", cache.getCachedKeys()
					.contains(id));
		}

		Assert.assertEquals("aa",
				cache.getPayload(new ID("a"), new NullProgressMonitor()));
		Assert.assertEquals(1, cache.getCachedKeys().size());
		for (ID id : new HashSet<ID>() {
			{
				add(new ID("a"));
			}
		}) {
			Assert.assertTrue(id + " not found", cache.getCachedKeys()
					.contains(id));
		}

		Assert.assertEquals("bb",
				cache.getPayload(new ID("b"), new NullProgressMonitor()));
		Assert.assertEquals(2, cache.getCachedKeys().size());
		for (ID id : new HashSet<ID>() {
			{
				add(new ID("a"));
				add(new ID("b"));
			}
		}) {
			Assert.assertTrue(id + " not found", cache.getCachedKeys()
					.contains(id));
		}

		Assert.assertEquals("bb",
				cache.getPayload(new ID("b"), new NullProgressMonitor()));
		Assert.assertEquals(2, cache.getCachedKeys().size());
		for (ID id : new HashSet<ID>() {
			{
				add(new ID("a"));
				add(new ID("b"));
			}
		}) {
			Assert.assertTrue(id + " not found", cache.getCachedKeys()
					.contains(id));
		}

		Assert.assertEquals("cc",
				cache.getPayload(new ID("c"), new NullProgressMonitor()));
		Assert.assertEquals(3, cache.getCachedKeys().size());
		for (ID id : new HashSet<ID>() {
			{
				add(new ID("a"));
				add(new ID("b"));
				add(new ID("c"));
			}
		}) {
			Assert.assertTrue(id + " not found", cache.getCachedKeys()
					.contains(id));
		}

		Assert.assertEquals("dd",
				cache.getPayload(new ID("d"), new NullProgressMonitor()));
		Assert.assertEquals(3, cache.getCachedKeys().size());
		for (ID id : new HashSet<ID>() {
			{
				add(new ID("a"));
				add(new ID("b"));
				add(new ID("d"));
			}
		}) {
			Assert.assertTrue(id + " not found", cache.getCachedKeys()
					.contains(id));
		}

		Assert.assertEquals("cc",
				cache.getPayload(new ID("c"), new NullProgressMonitor()));
		Assert.assertEquals(3, cache.getCachedKeys().size());
		for (ID id : new HashSet<ID>() {
			{
				add(new ID("a"));
				add(new ID("b"));
				add(new ID("c"));
			}
		}) {
			Assert.assertTrue(id + " not found", cache.getCachedKeys()
					.contains(id));
		}

		Assert.assertNull(cache.getPayload(new ID("x"),
				new NullProgressMonitor()));
		Assert.assertEquals(3, cache.getCachedKeys().size());
		for (ID id : new HashSet<ID>() {
			{
				add(new ID("a"));
				add(new ID("b"));
				add(new ID("x"));
			}
		}) {
			Assert.assertTrue(id + " not found", cache.getCachedKeys()
					.contains(id));
		}
	}
}
