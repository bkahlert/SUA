package de.fu_berlin.imp.apiua.uri.services;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import de.fu_berlin.imp.apiua.uri.model.IUri;
import de.fu_berlin.imp.apiua.uri.model.Uri;
import de.fu_berlin.imp.apiua.uri.services.IUriService;
import de.fu_berlin.imp.apiua.uri.services.IUriServiceListener;
import de.fu_berlin.imp.apiua.uri.services.impl.UriService;

public class UriServiceTest {

	public static class SpyingUriServiceListener implements IUriServiceListener {
		public Set<IUri> lastAdded = new HashSet<IUri>();
		public IUri lastReplaced = null;
		public IUri lastReplacedNew = null;
		public Set<IUri> lastRemoved = new HashSet<IUri>();

		@Override
		public void urisAdded(Set<IUri> uris) {
			this.lastAdded = uris;
		}

		@Override
		public void uriReplaced(IUri oldUri, IUri newUri) {
			this.lastReplaced = oldUri;
			this.lastReplacedNew = newUri;
		}

		@Override
		public void urisRemoved(Set<IUri> uris) {
			this.lastRemoved = uris;
		}
	}

	public static <T> Set<T> set(T... objects) {
		Set<T> set = new HashSet<T>();
		for (T object : objects) {
			set.add(object);
		}
		return set;
	}

	private static IUri uri1;
	private static IUri uri2;
	private static IUri uri3;

	static {
		try {
			uri1 = new Uri("http://sua.bkahlert.com");
			uri2 = new Uri("http://seqan.de");
			uri3 = new Uri("http://seqan-biostore.com");
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testAddDeleteEpisode() throws IOException {
		File file = File.createTempFile("uri", ".txt");
		file.deleteOnExit();

		IUriService uriService = new UriService(file);
		assertEquals(0, uriService.getUris().size());

		SpyingUriServiceListener listener = new SpyingUriServiceListener();
		uriService.addUriServiceListener(listener);

		uriService.addUri(uri1);
		assertEquals(1, uriService.getUris().size());
		assertEquals(set(uri1), uriService.getUris());
		assertEquals(set(uri1), listener.lastAdded);
		assertEquals(null, listener.lastReplaced);
		assertEquals(null, listener.lastReplacedNew);
		assertEquals(set(), listener.lastRemoved);

		uriService.addUri(uri2);
		assertEquals(2, uriService.getUris().size());
		assertEquals(set(uri1, uri2), uriService.getUris());
		assertEquals(set(uri2), listener.lastAdded);
		assertEquals(null, listener.lastReplaced);
		assertEquals(null, listener.lastReplacedNew);
		assertEquals(set(), listener.lastRemoved);

		uriService.replaceUri(uri2, uri3);
		assertEquals(2, uriService.getUris().size());
		assertEquals(set(uri1, uri3), uriService.getUris());
		assertEquals(set(uri2), listener.lastAdded);
		assertEquals(uri2, listener.lastReplaced);
		assertEquals(uri3, listener.lastReplacedNew);
		assertEquals(set(), listener.lastRemoved);

		// open file a second time
		IUriService uriService2 = new UriService(file);
		assertEquals(2, uriService2.getUris().size());
		assertEquals(set(uri1, uri3), uriService2.getUris());

		uriService.removeUri(uri1);
		assertEquals(1, uriService.getUris().size());
		assertEquals(set(uri3), uriService.getUris());
		assertEquals(set(uri2), listener.lastAdded);
		assertEquals(uri2, listener.lastReplaced);
		assertEquals(uri3, listener.lastReplacedNew);
		assertEquals(set(uri1), listener.lastRemoved);

		uriService.removeUri(uri3);
		assertEquals(0, uriService.getUris().size());
		assertEquals(set(), uriService.getUris());
		assertEquals(set(uri2), listener.lastAdded);
		assertEquals(uri2, listener.lastReplaced);
		assertEquals(uri3, listener.lastReplacedNew);
		assertEquals(set(uri3), listener.lastRemoved);
	}

}
