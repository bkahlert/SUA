package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.dnd;

import java.io.Serializable;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;

public class CodeIdentifier implements Serializable {
	private static final long serialVersionUID = 1L;
	private final long id;

	public CodeIdentifier(ICode code) {
		this.id = code.getId();
	}

	public long getId() {
		return id;
	}
}
