package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.impl;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.impl.CodeStoreFactory;

public class ServiceFactory extends AbstractServiceFactory {

	private static final Logger LOGGER = Logger.getLogger(ServiceFactory.class);

	private static ICodeService CODE_SERVICE;

	public ServiceFactory() {
	}

	@Override
	public Object create(@SuppressWarnings("rawtypes") Class serviceInterface,
			IServiceLocator parentLocator, IServiceLocator locator) {
		if (serviceInterface == ICodeService.class) {
			if (CODE_SERVICE == null) {
				try {
					CODE_SERVICE = new CodeService(
							new CodeStoreFactory().getCodeStore());
				} catch (IOException e) {
					LOGGER.error("Could not create " + ICodeService.class, e);
				}
			}
			return CODE_SERVICE;
		}

		return null;
	}
}
