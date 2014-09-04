package de.fu_berlin.imp.apiua.groundedtheory.storage;

import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IAdapterFactory;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.groundedtheory.LocatorService;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICodeInstance;

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
				try {
					return LocatorService.INSTANCE.resolve(id, null).get();
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
