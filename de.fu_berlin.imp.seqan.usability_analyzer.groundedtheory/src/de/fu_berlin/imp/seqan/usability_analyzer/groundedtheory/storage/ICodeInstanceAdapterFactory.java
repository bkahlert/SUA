package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage;

import java.net.URI;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.ILocatorService;

public class ICodeInstanceAdapterFactory implements IAdapterFactory {

	private static final Logger LOGGER = Logger
			.getLogger(ICodeInstanceAdapterFactory.class);

	@Override
	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] { ILocatable.class };
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof ICodeInstance) {
			if (adapterType == ILocatable.class) {
				URI id = ((ICodeInstance) adaptableObject).getId();
				ILocatorService locatorService = (ILocatorService) PlatformUI
						.getWorkbench().getService(ILocatorService.class);
				try {
					return locatorService.resolve(id, null).get();
				} catch (InterruptedException e) {
					LOGGER.error(e);
				} catch (ExecutionException e) {
					LOGGER.error(e);
				}
			}
		}

		return null;
	}

}
