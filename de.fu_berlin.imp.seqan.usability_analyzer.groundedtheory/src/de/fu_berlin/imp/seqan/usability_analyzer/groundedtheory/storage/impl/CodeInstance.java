package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.impl;

import java.net.URI;
import java.security.InvalidParameterException;

import org.apache.log4j.Logger;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.CodeInstanceLocatorProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;

class CodeInstance implements ICodeInstance {

	private static final long serialVersionUID = 7683462895079284073L;
	private static final Logger LOGGER = Logger.getLogger(CodeInstance.class);

	private final long codeInstanceId;
	private final ICode code;
	private final URI id;
	private final TimeZoneDate creation;

	public CodeInstance(long codeInstanceId, ICode code, URI id,
			TimeZoneDate creation) {
		super();
		if (code == null) {
			throw new InvalidParameterException("code must not be null");
		}
		if (id == null) {
			throw new InvalidParameterException("id must not be null");
		}
		if (creation == null) {
			throw new InvalidParameterException("creation must not be null");
		}
		this.codeInstanceId = codeInstanceId;
		this.code = code;
		this.id = id;
		this.creation = creation;
	}

	@Override
	public URI getUri() {
		try {
			return new URI("sua://"
					+ CodeInstanceLocatorProvider.CODE_INSTANCE_NAMESPACE + "/"
					+ this.codeInstanceId);
		} catch (Exception e) {
			LOGGER.error(
					"Could not create URI for a " + ICode.class.getSimpleName(),
					e);
		}
		return null;
	}

	@Override
	public long getCodeInstanceID() {
		return this.codeInstanceId;
	}

	@Override
	public ICode getCode() {
		return this.code;
	}

	@Override
	public URI getId() {
		return this.id;
	}

	@Override
	public TimeZoneDate getCreation() {
		return this.creation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ (int) (this.codeInstanceId ^ (this.codeInstanceId >>> 32));
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ICodeInstance)) {
			return false;
		}
		CodeInstance other = (CodeInstance) obj;
		if (this.codeInstanceId != other.codeInstanceId) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return this.getUri() + " (" + this.getId().toString() + " -> "
				+ this.getCode() + ")";
	}

}
