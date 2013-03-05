package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.impl;

import java.net.URI;
import java.security.InvalidParameterException;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;

class CodeInstance implements ICodeInstance {

	private final ICode code;
	private final URI id;
	private final TimeZoneDate creation;

	public CodeInstance(ICode code, URI id, TimeZoneDate creation) {
		super();
		if (code == null)
			throw new InvalidParameterException("code must not be null");
		if (id == null)
			throw new InvalidParameterException("id must not be null");
		if (creation == null)
			throw new InvalidParameterException("creation must not be null");
		this.code = code;
		this.id = id;
		this.creation = creation;
	}

	public CodeInstance(ICode code, ICodeable codeable, TimeZoneDate creation) {
		this(code, codeable.getUri(), creation);
	}

	@Override
	public ICode getCode() {
		return code;
	}

	@Override
	public URI getId() {
		return id;
	}

	@Override
	public TimeZoneDate getCreation() {
		return creation;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof CodeInstance))
			return false;
		CodeInstance other = (CodeInstance) obj;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getId().toString();
	}

}
