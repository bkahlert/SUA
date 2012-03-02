package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.impl;

import java.io.IOException;
import java.net.URISyntaxException;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.impl.CodeStoreHelper;

public class CodeServicesHelper extends CodeStoreHelper {

	public CodeServicesHelper() throws URISyntaxException {
		super();
	}

	public ICodeService getNonExistingCodeService() throws IOException {
		return new CodeService(getNonExistingCodeStore());
	}

	public ICodeService getEmptyCodeService() throws IOException {
		return new CodeService(getEmptyCodeStore());
	}

	public ICodeService getSmallCodeService() throws IOException {
		return new CodeService(getSmallCodeStore());
	}
}
