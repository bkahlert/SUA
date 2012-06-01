package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage;

import java.net.URI;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;

public class ICodeInstanceAdapterFactory implements IAdapterFactory {

	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] { ICodeable.class };
	}

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof ICodeInstance) {
			if (adapterType == ICodeable.class) {
				URI id = ((ICodeInstance) adaptableObject).getId();
				ICodeService codeService = (ICodeService) PlatformUI
						.getWorkbench().getService(ICodeService.class);
				return codeService.getCodedObject(id);
			}
		}

		return null;
	}

}
