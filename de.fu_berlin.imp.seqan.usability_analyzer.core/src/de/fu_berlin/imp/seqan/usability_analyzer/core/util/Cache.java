package de.fu_berlin.imp.seqan.usability_analyzer.core.util;

import java.util.HashMap;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;

// TODO improve parallelization
public class Cache<KEY, PAYLOAD> {

	public static interface CacheFetcher<KEY, PAYLOAD> {
		public PAYLOAD fetch(KEY key, IProgressMonitor progressMonitor);
	}

	private class DiffCacheEntry {
		private int usedCount;
		private PAYLOAD payload;

		public DiffCacheEntry(KEY key) {
			this.usedCount = 0;
		}

		public PAYLOAD getPayload(KEY key, IProgressMonitor progressMonitor) {
			if (this.payload == null) {
				if (cacheFetcher != null && key != null)
					this.payload = cacheFetcher.fetch(key, progressMonitor);
			}
			this.usedCount++;
			return payload;
		}

		public int getUsedCount() {
			return this.usedCount;
		}
	}

	private CacheFetcher<KEY, PAYLOAD> cacheFetcher;
	private int cacheSize;
	private HashMap<KEY, DiffCacheEntry> cache;

	public Cache(CacheFetcher<KEY, PAYLOAD> cacheFetcher, int cacheSize) {
		this.cacheFetcher = cacheFetcher;
		this.cacheSize = cacheSize;
		this.cache = new HashMap<KEY, DiffCacheEntry>();
	}

	public synchronized PAYLOAD getPayload(KEY id,
			IProgressMonitor progressMonitor) {
		if (!this.cache.containsKey(id)) {
			shrinkCache();
			DiffCacheEntry cacheEntry = new DiffCacheEntry(id);
			this.cache.put(id, cacheEntry);
		}

		DiffCacheEntry cacheEntry = this.cache.get(id);
		return cacheEntry.getPayload(id, progressMonitor);
	}

	public synchronized Set<KEY> getCachedKeys() {
		return cache.keySet();
	}

	synchronized private void shrinkCache() {
		if (this.cache.size() >= cacheSize) {
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
		}
	}

	public synchronized void removeKey(KEY key) {
		if (this.cache.containsKey(key))
			this.cache.remove(key);
	}
}
