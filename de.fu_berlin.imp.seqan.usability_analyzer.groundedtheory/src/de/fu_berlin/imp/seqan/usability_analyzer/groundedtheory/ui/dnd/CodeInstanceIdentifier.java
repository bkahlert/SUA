package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.dnd;

import java.io.Serializable;
import java.net.URI;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;

public class CodeInstanceIdentifier implements Serializable {
	private static final long serialVersionUID = 1L;
	private final long codeId;
	private final URI codeableId;

	public CodeInstanceIdentifier(ICodeInstance codeInstance) {
		this.codeId = codeInstance.getCode().getId();
		this.codeableId = codeInstance.getId();
	}

	public long getCodeId() {
		return codeId;
	}

	public URI getCodeableId() {
		return codeableId;
	}
}
