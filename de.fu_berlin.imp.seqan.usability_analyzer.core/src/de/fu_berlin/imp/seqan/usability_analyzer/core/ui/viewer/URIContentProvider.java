package de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;

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
			INPUT newInput, Object ignore) throws Exception;

	public abstract URI[] getTopLevelElements(INPUT input) throws Exception;

	public abstract URI getParent(URI uri) throws Exception;

	public abstract boolean hasChildren(URI uri) throws Exception;

	public abstract URI[] getChildren(URI parentUri) throws Exception;

	@Override
	public abstract void dispose();

	@SuppressWarnings("unchecked")
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		try {
			this.inputChanged(viewer, (INPUT) oldInput, (INPUT) newInput, null);
		} catch (ClassCastException e) {
			LOGGER.error("Unsupported input type passed as input for "
					+ this.getClass().getSimpleName(), e);
		} catch (Exception e) {
			LOGGER.error("Error while changing input for "
					+ this.getClass().getSimpleName(), e);
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
		} catch (Exception e) {
			LOGGER.error("Error while getting element of " + inputElement, e);
			return new Object[0];
		}
	}

	@Override
	public final Object getParent(Object element) {
		URI uri = URIUtils.adapt(element);
		try {
			return uri != null ? this.getParent(uri) : null;
		} catch (Exception e) {
			LOGGER.error("Error while getting parent of " + element, e);
			return null;
		}
	}

	@Override
	public final boolean hasChildren(Object element) {
		URI uri = URIUtils.adapt(element);
		try {
			return uri != null ? this.hasChildren(uri) : false;
		} catch (Exception e) {
			LOGGER.error("Error while checking children of " + element, e);
			return false;
		}
	}

	@Override
	public final Object[] getChildren(Object parentElement) {
		URI uri = URIUtils.adapt(parentElement);
		try {
			return uri != null ? this.getChildren(uri) : null;
		} catch (Exception e) {
			LOGGER.error("Error while getting children of " + parentElement, e);
			return null;
		}
	}

}
