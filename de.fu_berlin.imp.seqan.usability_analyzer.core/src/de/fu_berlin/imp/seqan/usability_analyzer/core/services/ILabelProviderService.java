package de.fu_berlin.imp.seqan.usability_analyzer.core.services;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.URIUtils;

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
		 * Creates an {@link ILabelProvider} for the given {@link URI}.
		 * 
		 * @param uri
		 * @return null if this {@link ILabelProvider} can't format the
		 *         requested object.
		 */
		public ILabelProvider createFor(URI uri);
	}

	/**
	 * {@link ILabelProvider} that can not only retrieve text and {@link Image
	 * image} for a given value object but also for an {@link URI} pointing to
	 * it.
	 * 
	 * @author bjornson
	 * 
	 */
	public static interface ILabelProvider extends
			org.eclipse.jface.viewers.ILabelProvider {

		/**
		 * Returns the text of the value object the {@link URI} is pointing to.
		 * If the link can't be resolved <code>null</code> is returned.
		 * 
		 * @param element
		 * @return
		 */
		public String getText(URI element) throws Exception;

		/**
		 * Returns the {@link Image image} of the value object the {@link URI}
		 * is pointing to. If the link can't be resolved <code>null</code> is
		 * returned.
		 * 
		 * @param element
		 * @return
		 */
		public Image getImage(URI element) throws Exception;

		/**
		 * Returns the text of the given value object. If there is no text
		 * <code>null</code> is returned.
		 * 
		 * @param element
		 * @return
		 */
		@Override
		public String getText(Object element);

		/**
		 * Returns the {@link Image image} of the given value object. If there
		 * is no {@link Image image} <code>null</code> is returned.
		 * 
		 * @param element
		 * @return
		 */
		@Override
		public Image getImage(Object element);

	}

	/**
	 * {@link ILabelProvider} that can not only retrieve text and {@link Image
	 * image} for a given value object but also for an {@link URI} pointing to
	 * it.
	 * 
	 * @author bkahlert
	 * 
	 */
	public static interface IStyledLabelProvider
			extends
			ILabelProvider,
			org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider {
		public StyledString getStyledText(URI element) throws Exception;
	}

	/**
	 * {@link ILabelProvider} that tries to adapt the given elements to
	 * {@link URI}s.
	 * 
	 * @author bkahlert
	 */
	public static class LabelProvider extends
			org.eclipse.jface.viewers.LabelProvider implements ILabelProvider {

		private static final Logger LOGGER = Logger
				.getLogger(LabelProvider.class);

		@Override
		public String getText(URI element) throws Exception {
			return super.getText(element);
		}

		@Override
		public Image getImage(URI element) throws Exception {
			return super.getImage(element);
		}

		@Override
		public final String getText(Object element) {
			URI uri = URIUtils.adapt(element);
			if (uri != null) {
				try {
					return getText(uri);
				} catch (Exception e) {
					LOGGER.error("Error retrieving text for " + element, e);
					return "ERROR";
				}
			} else {
				return super.getText(element);
			}
		}

		@Override
		public final Image getImage(Object element) {
			URI uri = URIUtils.adapt(element);
			if (uri != null) {
				try {
					return getImage(uri);
				} catch (Exception e) {
					LOGGER.error("Error retrieving image for " + element, e);
					return null;
				}
			} else {
				return super.getImage(element);
			}
		}
	}

	public static class StyledColumnLabelProvider extends ColumnLabelProvider
			implements IStyledLabelProvider {

		private static final Logger LOGGER = Logger
				.getLogger(StyledColumnLabelProvider.class);

		@Override
		public StyledString getStyledText(URI element) throws Exception {
			return new StyledString("");
		}

		@Override
		public String getText(URI element) throws Exception {
			return getStyledText(element).getString();
		}

		@Override
		public Image getImage(URI element) throws Exception {
			return super.getImage(element);
		}

		public Color getBackground(URI element) throws Exception {
			return super.getBackground(element);
		}

		@Override
		public final String getText(Object element) {
			URI uri = URIUtils.adapt(element);
			if (uri != null) {
				try {
					return getText(uri);
				} catch (Exception e) {
					LOGGER.error("Error retrieving text for " + element, e);
					return "ERROR";
				}
			} else {
				return super.getText(element);
			}
		}

		@Override
		public final Image getImage(Object element) {
			URI uri = URIUtils.adapt(element);
			if (uri != null) {
				try {
					return getImage(uri);
				} catch (Exception e) {
					LOGGER.error("Error retrieving image for " + element, e);
					return null;
				}
			} else {
				return super.getImage(element);
			}
		}

		@Override
		public StyledString getStyledText(Object element) {
			URI uri = URIUtils.adapt(element);
			if (uri != null) {
				try {
					return getStyledText(uri);
				} catch (Exception e) {
					LOGGER.error("Error retrieving styled text for " + element,
							e);
					return new StyledString("ERROR");
				}
			} else {
				return new StyledString(super.getText(element));
			}
		}

		@Override
		public Color getBackground(Object element) {
			URI uri = URIUtils.adapt(element);
			if (uri != null) {
				try {
					return getBackground(uri);
				} catch (Exception e) {
					LOGGER.error("Error retrieving background for " + element,
							e);
					return Display.getDefault().getSystemColor(SWT.COLOR_RED);
				}
			}
			return super.getBackground(element);
		}

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
		private String[] pathSegmentValues;

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
			this(pathSegmentIndex, new String[] { pathSegmentValue });
		}

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
				String[] pathSegmentValues) {
			Assert.isTrue(pathSegmentIndex >= 0);
			Assert.isNotNull(pathSegmentValues);
			Assert.isTrue(pathSegmentValues.length > 0);
			this.pathSegmentIndex = pathSegmentIndex;
			this.pathSegmentValues = pathSegmentValues;
		}

		@Override
		public ILabelProvider createFor(URI uri) {
			List<String> segments = getSegments(uri);

			if (segments.size() <= this.pathSegmentIndex) {
				return null;
			}
			if (ArrayUtils.contains(this.pathSegmentValues,
					segments.get(this.pathSegmentIndex))) {
				return create();
			} else {
				return null;
			}
		}

		// TODO move to URIUtils
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
