package de.fu_berlin.imp.apiua.core.services.impl;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.services.ILabelProviderService;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;

public class LabelProviderService implements ILabelProviderService {

	private List<ILabelProviderFactory> labelProviderFactories = new ArrayList<ILabelProviderFactory>();

	@Override
	public void addLabelProviderFactory(
			ILabelProviderFactory labelProviderFactory) {
		Assert.isNotNull(labelProviderFactory);
		this.labelProviderFactories.add(labelProviderFactory);
	}

	@Override
	public void removeLabelProviderFactory(
			ILabelProviderFactory labelProviderFactory) {
		Assert.isNotNull(labelProviderFactory);
		this.labelProviderFactories.remove(labelProviderFactory);
	}

	@Override
	public ILabelProvider getLabelProvider(URI uri) {
		for (ILabelProviderFactory labelProviderFactory : this.labelProviderFactories) {
			ILabelProvider labelProvider = labelProviderFactory.createFor(uri);
			if (labelProvider != null) {
				return labelProvider;
			}
		}
		return null;
	}

}
