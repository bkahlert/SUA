package de.fu_berlin.imp.seqan.usability_analyzer.core.util;

import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;

import com.bkahlert.devel.nebula.widgets.timeline.impl.TimePassed;

// TODO improve parallelization
public class Cache<KEY, PAYLOAD> {

	private static final boolean DISABLE_CACHE = false;
	static final float SHRINK_BY = 0.25f;

	public static interface CacheFetcher<KEY, PAYLOAD> {
		public PAYLOAD fetch(KEY key, IProgressMonitor progressMonitor);
	}

	private class CacheEntry {
		private int usedCount;
		private PAYLOAD payload;

		public CacheEntry(KEY key) {
			this.usedCount = 0;
		}

		public PAYLOAD getPayload(KEY key, IProgressMonitor progressMonitor) {
			if (this.usedCount == 0) {
				this.payload = Cache.this.cacheFetcher.fetch(key,
						progressMonitor);
			}
			this.usedCount++;
			return this.payload;
		}

		public int getUsedCount() {
			return this.usedCount;
		}
	}

	private final CacheFetcher<KEY, PAYLOAD> cacheFetcher;
	private final int cacheSize;
	private final HashMap<KEY, CacheEntry> cache;

	public Cache(CacheFetcher<KEY, PAYLOAD> cacheFetcher, int cacheSize) {
		Assert.isNotNull(cacheFetcher);
		this.cacheFetcher = cacheFetcher;
		this.cacheSize = cacheSize;
		this.cache = new HashMap<KEY, CacheEntry>(cacheSize);
	}

	public synchronized PAYLOAD getPayload(KEY key,
			IProgressMonitor progressMonitor) {
		// TODO insert following line if debugging diffs - makes the diffs be
		// created on every try
		// new DiffCacheEntry(id).getPayload(id, progressMonitor);

		Assert.isNotNull(key);

		if (DISABLE_CACHE || cacheSize == 0) {
			return this.cacheFetcher.fetch(key, progressMonitor);
		}

		if (!this.cache.containsKey(key)) {
			this.shrinkCache();
			CacheEntry cacheEntry = new CacheEntry(key);
			this.cache.put(key, cacheEntry);
		}

		CacheEntry cacheEntry = this.cache.get(key);
		return cacheEntry.getPayload(key, progressMonitor);
	}

	public synchronized Set<KEY> getCachedKeys() {
		return this.cache.keySet();
	}

	synchronized private void shrinkCache() {
		if (this.cache.size() >= this.cacheSize) {
			TimePassed passed = new TimePassed("cache shrink");

			int numDelete = cacheSize > 10 ? (int) (cacheSize * SHRINK_BY) : 1;
			if (numDelete == 1) {
				KEY delete = null;
				int minUsedCount = Integer.MAX_VALUE;
				for (KEY key : this.cache.keySet()) {
					int usedCount = this.cache.get(key).getUsedCount();
					if (usedCount < minUsedCount) {
						delete = key;
						minUsedCount = usedCount;
					}
				}
				this.cache.remove(delete);
			} else {
				PriorityQueue<KEY> sortedKeys = new PriorityQueue<KEY>(
						this.cache.size(), new Comparator<KEY>() {
							@Override
							public int compare(KEY arg0, KEY arg1) {
								int o1 = cache.get(arg0).usedCount;
								int o2 = cache.get(arg1).usedCount;
								if (o1 < o2) {
									return -1;
								}
								if (o1 == o2) {
									return 0;
								}
								return 1;
							}
						});
				sortedKeys.addAll(this.cache.keySet());

				passed.tell("elements sorted");

				for (int i = 0; i < numDelete; i++) {
					KEY key = sortedKeys.poll();
					if (key == null) {
						throw new RuntimeException("IMPLEMENTATION ERROR");
					}
					this.cache.remove(key);
				}
			}

			passed.tell("removed " + numDelete + " element"
					+ (numDelete == 1 ? "" : "s"));
		}
	}

	public synchronized void removeKey(KEY key) {
		if (this.cache.containsKey(key)) {
			this.cache.remove(key);
		}
	}
}
