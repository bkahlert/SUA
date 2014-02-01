package de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer;

import java.net.URI;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.URIUtils;

public abstract class URIContentProvider<INPUT> implements
		IStructuredContentProvider, ITreeContentProvider {

	private static final Logger LOGGER = Logger
			.getLogger(URIContentProvider.class);

	public abstract void inputChanged(Viewer viewer, INPUT oldInput,
			INPUT newInput, Object ignore);

	public abstract URI[] getTopLevelElements(INPUT input);

	public abstract URI getParent(URI uri);

	public abstract boolean hasChildren(URI uri);

	public abstract URI[] getChildren(URI parentUri);

	@Override
	public abstract void dispose();

	@SuppressWarnings("unchecked")
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		try {
			this.inputChanged(viewer, (INPUT) oldInput, (INPUT) newInput, null);
		} catch (ClassCastException e) {
			LOGGER.error("Unsupported input type passed as input for "
					+ this.getClass().getSimpleName());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public final Object[] getElements(Object inputElement) {
		try {
			return this.getTopLevelElements((INPUT) inputElement);
		} catch (ClassCastException e) {
			LOGGER.error("Unsupported input type passed to getElements of "
					+ this.getClass().getSimpleName());
			return new Object[0];
		}
	}

	@Override
	public final Object getParent(Object element) {
		URI uri = URIUtils.adapt(element);
		return uri != null ? this.getParent(uri) : null;
	}

	@Override
	public final boolean hasChildren(Object element) {
		URI uri = URIUtils.adapt(element);
		return uri != null ? this.hasChildren(uri) : false;
	}

	@Override
	public final Object[] getChildren(Object parentElement) {
		URI uri = URIUtils.adapt(parentElement);
		return uri != null ? this.getChildren(uri) : null;
	}

}
