package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.impl;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeStore;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.impl.CodeStoreFactory;

public class CodeServiceFactory extends AbstractServiceFactory implements
		ServiceFactory<ICodeService> {

	private static final Logger LOGGER = Logger
			.getLogger(CodeServiceFactory.class);

	private static ICodeService CODE_SERVICE;

	public CodeServiceFactory() {
	}

	@Override
	public Object create(@SuppressWarnings("rawtypes") Class serviceInterface,
			IServiceLocator parentLocator, IServiceLocator locator) {
		if (serviceInterface == ICodeService.class) {
			if (CODE_SERVICE == null) {
				try {
					ICodeStore codeStore = new CodeStoreFactory()
							.getCodeStore();
					CODE_SERVICE = new CodeService(codeStore);
				} catch (IOException e) {
					LOGGER.error("Could not create " + ICodeService.class, e);
				}
			}
			return CODE_SERVICE;
		}

		return null;
	}

	@Override
	public ICodeService getService(Bundle bundle,
			ServiceRegistration<ICodeService> registration) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void ungetService(Bundle bundle,
			ServiceRegistration<ICodeService> registration, ICodeService service) {
		// TODO Auto-generated method stub

	}
}
