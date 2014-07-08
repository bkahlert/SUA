package de.fu_berlin.imp.apiua.groundedtheory.propertyTesters;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;

public class ICodePropertyTester extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		if (receiver instanceof ICode) {
			ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getService(ICodeService.class);
			ICode code = (ICode) receiver;
			if ("hasInstances".equals(property)) {
				return codeService.getInstances(code).size() > 0;
			}
		}
		return false;
	}

}
