package de.fu_berlin.imp.seqan.usability_analyzer.core.services.impl;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ILabelProvider;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.ILabelProviderService;

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
	public ILabelProvider getLabelProvider(ILocatable uri) {
		for (ILabelProviderFactory labelProviderFactory : this.labelProviderFactories) {
			ILabelProvider labelProvider = labelProviderFactory.createFor(uri);
			if (labelProvider != null) {
				return labelProvider;
			}
		}
		return null;
	}

}
