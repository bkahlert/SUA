package de.fu_berlin.imp.seqan.usability_analyzer.core.services;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ILabelProvider;

/**
 * This service is responsible to make {@link ILabelProvider}s accessible across
 * plugin borders.
 * 
 * @author bkahlert
 * 
 */
public interface ILabelProviderService {

	/**
	 * Instances of this class may be able to construct a {@link ILabelProvider}
	 * for the given objects.
	 * 
	 * @author bkahlert
	 */
	public static interface ILabelProviderFactory {
		/**
		 * Creates an {@link ILabelProvider} for the object references by the
		 * given {@link URI}.
		 * 
		 * @param uri
		 * @return null if this {@link ILabelProvider} can't format the
		 *         requested object.
		 */
		public ILabelProvider createFor(URI uri);
	}

	/**
	 * Instances of this class decide on the provided {@link URI}' specific
	 * features whether to return an {@link ILabelProvider} or not.
	 * 
	 * @author bkahlert
	 */
	public static abstract class URIPathLabelProviderFactory implements
			ILabelProviderFactory {
		private int pathSegmentIndex;
		private String pathSegmentValue;

		/**
		 * Constructs an instance that checks the i-th segment of the
		 * {@link URI}s path (including the host) for a specific value.
		 * <p>
		 * e.g. a {@link URIPathLabelProviderFactory} will return a
		 * {@link ILabelProvider} for sua://foo/bar if it check
		 * <code>pathSegmentIndex
		 * = 1</code> and <code>pathSegmentValue = bar</code>.
		 * 
		 * @param pathSegmentIndex
		 * @param pathSegmentValue
		 */
		public URIPathLabelProviderFactory(int pathSegmentIndex,
				String pathSegmentValue) {
			Assert.isTrue(pathSegmentIndex >= 0);
			Assert.isNotNull(pathSegmentValue);
			this.pathSegmentIndex = pathSegmentIndex;
			this.pathSegmentValue = pathSegmentValue;
		}

		@Override
		public ILabelProvider createFor(URI uri) {
			List<String> segments = getSegments(uri);

			if (segments.size() <= this.pathSegmentIndex) {
				return null;
			}
			if (segments.get(this.pathSegmentIndex).equals(
					this.pathSegmentValue)) {
				return create();
			} else {
				return null;
			}
		}

		private List<String> getSegments(URI uri) {
			String host = uri.getHost();
			String path = uri.getRawPath();

			List<String> segments = new ArrayList<String>();
			segments.add(host != null ? host : "");
			List<String> pathSegments = path != null ? Arrays.asList(path
					.split("/")) : new ArrayList<String>(null);
			for (int i = 1, m = pathSegments.size(); i < m; i++) {
				segments.add(pathSegments.get(i));
			}
			return segments;
		}

		protected abstract ILabelProvider create();
	}

	/**
	 * Adds a {@link ILabelProviderFactory} that is consulted when
	 * {@link #getLabelProvider(URI)} is called.
	 * 
	 * @param labelProviderFactory
	 */
	public void addLabelProviderFactory(
			ILabelProviderFactory labelProviderFactory);

	/**
	 * Removed a {@link ILabelProviderFactory} from the pool of
	 * {@link ILabelProviderFactory}s that is consulted when
	 * {@link #getLabelProvider(URI)} is called.
	 * 
	 * @param labelProviderFactory
	 */
	public void removeLabelProviderFactory(
			ILabelProviderFactory labelProviderFactory);

	/**
	 * Returns the {@link ILabelProvider} than can format the object referenced
	 * by the given {@link URI}.
	 * 
	 * @param uri
	 * @return null if no {@link ILabelProvider} can be provided.
	 */
	public ILabelProvider getLabelProvider(URI uri);

}
