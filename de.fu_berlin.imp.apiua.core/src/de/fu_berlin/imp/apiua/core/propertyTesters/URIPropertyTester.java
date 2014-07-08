package de.fu_berlin.imp.apiua.core.propertyTesters;

import org.apache.log4j.Logger;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.services.location.ILocatorService;

public class URIPropertyTester extends PropertyTester {

	private static final Logger LOGGER = Logger
			.getLogger(URIPropertyTester.class);
	private static final String PROPERTY_INDIRECT_INSTANCE_OF = "indirectInstanceOf"; //$NON-NLS-1$

	private static final ILocatorService locatorService = (ILocatorService) PlatformUI
			.getWorkbench().getService(ILocatorService.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.expressions.PropertyTester#test(java.lang.Object,
	 * java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	@Override
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		if (args.length == 0 && receiver instanceof URI
				&& expectedValue != null) {
			if (PROPERTY_INDIRECT_INSTANCE_OF.equals(property)) {
				return isIndirectInstanceOf((URI) receiver,
						expectedValue.toString());
			}
		}
		return false;
	}

	private static boolean isIndirectInstanceOf(URI uri, String className) {
		try {
			Class<?> destinationClazz = Class.forName(className);
			Class<?> uriType = locatorService.getType(uri);
			if (uriType != null) {
				return destinationClazz.isAssignableFrom(uriType);
			}
		} catch (Exception e) {
			LOGGER.warn("Could not check if " + uri + " is of type "
					+ className, e);
		}
		return false;
	}

}
