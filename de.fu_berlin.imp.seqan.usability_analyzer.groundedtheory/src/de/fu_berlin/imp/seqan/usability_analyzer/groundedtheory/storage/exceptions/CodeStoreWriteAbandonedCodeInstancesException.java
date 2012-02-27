package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions;

import org.apache.commons.lang.StringUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;

public class CodeStoreWriteAbandonedCodeInstancesException extends
		CodeStoreWriteException {

	private static final long serialVersionUID = 4695434392874929969L;

	private ICodeInstance[] codeInstances;

	public CodeStoreWriteAbandonedCodeInstancesException(
			ICodeInstance[] codeInstances) {
		super("Saving the given " + ICode.class.getSimpleName()
				+ " would lead to " + ICodeInstance.class.getSimpleName()
				+ " with an invalid " + ICode.class + " reference.\nAffected "
				+ ICodeInstance.class.getSimpleName() + "s:\n- "
				+ StringUtils.join(codeInstances, "\n- "));
		this.codeInstances = codeInstances;
	}

	public ICodeInstance[] getAffectedCodeInstances() {
		return codeInstances;
	}
}
