package de.fu_berlin.imp.seqan.usability_analyzer.core.services;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;

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
	 * for the given {@link ILocatable}s.
	 * 
	 * @author bkahlert
	 */
	public static interface ILabelProviderFactory {
		/**
		 * Creates an {@link ILabelProvider} for the given {@link ILocatable}.
		 * 
		 * @param locatable
		 * @return null if this {@link ILabelProvider} can't format the
		 *         requested object.
		 */
		public ILabelProvider createFor(ILocatable locatable);
	}

	/**
	 * Instances of this class decide on the provided {@link ILocatable}'s
	 * specific features whether to return an {@link ILabelProvider} or not.
	 * 
	 * @author bkahlert
	 */
	public static abstract class LocatablePathLabelProviderFactory implements
			ILabelProviderFactory {
		private int pathSegmentIndex;
		private String pathSegmentValue;

		/**
		 * Constructs an instance that checks the i-th segment of the
		 * {@link URI}s path (including the host) for a specific value.
		 * <p>
		 * e.g. a {@link LocatablePathLabelProviderFactory} will return a
		 * {@link ILabelProvider} for sua://foo/bar if it check
		 * <code>pathSegmentIndex
		 * = 1</code> and <code>pathSegmentValue = bar</code>.
		 * 
		 * @param pathSegmentIndex
		 * @param pathSegmentValue
		 */
		public LocatablePathLabelProviderFactory(int pathSegmentIndex,
				String pathSegmentValue) {
			Assert.isTrue(pathSegmentIndex >= 0);
			Assert.isNotNull(pathSegmentValue);
			this.pathSegmentIndex = pathSegmentIndex;
			this.pathSegmentValue = pathSegmentValue;
		}

		@Override
		public ILabelProvider createFor(ILocatable locatable) {
			List<String> segments = getSegments(locatable);

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

		private List<String> getSegments(ILocatable locatable) {
			URI uri = locatable.getUri();
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
	 * Instances of this class can provide callers of
	 * {@link ICodeableProvider#getLabelProvider(ILocatable) with further information.
	 * @author bkahlert
	 *
	 */
	public static interface IDetailedLabelProvider extends ILabelProvider {
		/**
		 * Returns true if this {@link IDetailedLabelProvider} can fill a popup.
		 * 
		 * @param element
		 * @return
		 */
		public boolean canFillPopup(Object element);

		/**
		 * Fills the given {@link Composite} with detailed information.
		 * 
		 * @param element
		 * @param composite
		 * @return the main control; null if no popup should be displayed.
		 */
		public Control fillPopup(Object element, Composite composite);
	}

	/**
	 * Default implemention of {@link IDetailedLabelProvider}.
	 * 
	 * @author bkahlert
	 * 
	 */
	public static class DetailedLabelProvider extends LabelProvider implements
			IDetailedLabelProvider {

		@Override
		public boolean canFillPopup(Object element) {
			return false;
		}

		@Override
		public Control fillPopup(Object element, Composite composite) {
			return null;
		}

	}

	/**
	 * Adds a {@link ILabelProviderFactory} that is consulted when
	 * {@link #getLabelProvider(ILocatable)} is called.
	 * 
	 * @param labelProviderFactory
	 */
	public void addLabelProviderFactory(
			ILabelProviderFactory labelProviderFactory);

	/**
	 * Removed a {@link ILabelProviderFactory} from the pool of
	 * {@link ILabelProviderFactory}s that is consulted when
	 * {@link #getLabelProvider(ILocatable)} is called.
	 * 
	 * @param labelProviderFactory
	 */
	public void removeLabelProviderFactory(
			ILabelProviderFactory labelProviderFactory);

	/**
	 * Returns the {@link ILabelProvider} than can format the object referenced
	 * by the given {@link URI}.
	 * 
	 * @param locatable
	 * @return null if no {@link ILabelProvider} can be provided.
	 */
	public ILabelProvider getLabelProvider(ILocatable locatable);

}
