package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.impl;

import java.util.HashMap;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;

class CodeMapper {
	private HashMap<Long, ICode> codes;

	public CodeMapper(ICode[] codes) {
		this.codes = new HashMap<Long, ICode>(codes.length);
		for (ICode code : codes) {
			this.codes.put(code.getId(), code);
		}
	}

	public ICode getCode(long codeId) {
		return this.codes.get(codeId);
	}
}
