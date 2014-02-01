package de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.impl;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IAdapterFactory;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.IRevealableInOS;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.data.IData;

public class DataAdapterFactory implements IAdapterFactory {

	@Override
	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] { IRevealableInOS.class };
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof IData) {
			final IData data = (IData) adaptableObject;
			if (adapterType == IRevealableInOS.class) {
				return new IRevealableInOS() {
					@Override
					public File getFile() throws IOException {
						return data.getStaticFile();
					}
				};
			}
			return null;
		}
		return null;
	}

}
