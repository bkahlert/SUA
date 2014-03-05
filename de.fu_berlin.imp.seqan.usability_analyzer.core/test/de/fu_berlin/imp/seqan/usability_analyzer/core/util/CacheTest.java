package de.fu_berlin.imp.seqan.usability_analyzer.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import junit.framework.Assert;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;

import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.IConverter;
import com.bkahlert.nebula.widgets.timeline.impl.TimePassed;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.IdentifierFactory;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.Cache.CacheFetcher;

public class CacheTest {
	@SuppressWarnings("serial")
	@Test
	public void testSmallCache() {
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

	@Test
	public void testBigCache() {
		int cacheSize = 4000;

		TimePassed passed = new TimePassed("BIG CACHE");
		Cache<Integer, Integer> cache = new Cache<Integer, Integer>(
				new CacheFetcher<Integer, Integer>() {
					@Override
					public Integer fetch(Integer key,
							IProgressMonitor progressMonitor) {
						return key + 1;
					}
				}, cacheSize);
		for (int i = 0; i < cacheSize * 10; i++) {
			assertEquals(i + 1, (int) cache.getPayload(i, null));

			int numCacheEntries = i + 1;
			while (numCacheEntries > cacheSize) {
				numCacheEntries -= cacheSize * Cache.SHRINK_BY;
			}
			assertEquals(numCacheEntries, cache.getCachedKeys().size());
		}
		passed.tell("finished");
	}

	private static class CacheRunner<KEY, PAYLOAD> {
		private final Cache<KEY, PAYLOAD> cache;
		private final IConverter<Integer, KEY> runToKeyConverter;
		private final IConverter<KEY, PAYLOAD> keyToPayloadConverter;
		int numRuns = 5;

		public CacheRunner(Cache<KEY, PAYLOAD> cache,
				IConverter<Integer, KEY> runToKeyConverter,
				IConverter<KEY, PAYLOAD> keyToPayloadConverter, int numRuns) {
			super();
			this.cache = cache;
			this.runToKeyConverter = runToKeyConverter;
			this.keyToPayloadConverter = keyToPayloadConverter;
			this.numRuns = numRuns;
		}

		public Future<Long> run() {
			return ExecUtils.nonUISyncExec(new Callable<Long>() {
				@Override
				public Long call() throws Exception {
					TimePassed passed = new TimePassed();
					for (int i = 0; i < CacheRunner.this.numRuns; i++) {
						KEY key = CacheRunner.this.runToKeyConverter.convert(i);
						assertEquals(CacheRunner.this.keyToPayloadConverter
								.convert(key), CacheRunner.this.cache
								.getPayload(key, null));
					}
					return passed.getTimePassed();
				}
			});
		}
	}

	@Test
	public void testThreadSpeedup() throws Exception {
		final double minConsecutiveSpeedUp = 0.5;
		final double maxParallelSlowDown = 1.1;

		final int cacheSize = 500;
		final int numRuns = 200;

		IConverter<Integer, String> runToKeyConverter = new IConverter<Integer, String>() {
			@Override
			public String convert(Integer run) {
				int rand = (int) (Math.random() * numRuns);
				return "key/" + rand;
			}
		};

		final IConverter<String, URI> keyToPayloadConverter = new IConverter<String, URI>() {
			@Override
			public URI convert(String key) {
				try {
					URI uri = new URI("fake://" + key);
					return uri;
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		};

		final CacheFetcher<String, URI> cacheFetcher = new CacheFetcher<String, URI>() {
			@Override
			public URI fetch(String key, IProgressMonitor progressMonitor) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				return keyToPayloadConverter.convert(key);
			}
		};

		long time1;
		long time2;
		TimePassed passed = new TimePassed("SPEED TEST");
		{
			final Cache<String, URI> cache = new Cache<String, URI>(
					cacheFetcher, cacheSize);
			passed.tell("start single runs");
			time1 = new CacheRunner<String, URI>(cache, runToKeyConverter,
					keyToPayloadConverter, numRuns).run().get();
			passed.tell("single run #1");
			time2 = new CacheRunner<String, URI>(cache, runToKeyConverter,
					keyToPayloadConverter, numRuns).run().get();
			passed.tell("single run #2");
		}
		long sum1 = time1 + time2;
		passed.tell("single run #1 + #2 is " + sum1 + "ms");

		assertTrue(
				"The consecutive run was only "
						+ Math.round(((time2 / (double) time1) - 1) * 100)
						+ "% (" + (time2 - time1)
						+ "ms) slower than the first run! Max "
						+ (time2 * minConsecutiveSpeedUp) + "ms expected.",
				time1 * minConsecutiveSpeedUp > time2);

		Future<Long> time1_;
		Future<Long> time2_;
		{
			final Cache<String, URI> cache = new Cache<String, URI>(
					cacheFetcher, cacheSize);
			passed.tell("start parallel");
			time1_ = new CacheRunner<String, URI>(cache, runToKeyConverter,
					keyToPayloadConverter, numRuns).run();
			time2_ = new CacheRunner<String, URI>(cache, runToKeyConverter,
					keyToPayloadConverter, numRuns).run();
		}
		long sum2 = time1_.get() + time2_.get();
		passed.tell("parallel run #1 + #2 is " + sum2 + "ms");

		assertTrue(
				"The parallel run was "
						+ Math.round(((sum2 / (double) sum1) - 1) * 100)
						+ "% (" + (sum2 - sum1)
						+ "ms) slower than the single runs!", sum1
						* maxParallelSlowDown > sum2);
		passed.tell("finished");
	}
}
