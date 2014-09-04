package de.fu_berlin.imp.apiua.groundedtheory.storage.impl;

import java.security.InvalidParameterException;

import org.apache.log4j.Logger;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

import de.fu_berlin.imp.apiua.core.model.HasIdentifier;
import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.TimeZoneDate;
import de.fu_berlin.imp.apiua.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.apiua.core.ui.viewer.filters.HasDateRange;
import de.fu_berlin.imp.apiua.groundedtheory.CodeInstanceLocatorProvider;
import de.fu_berlin.imp.apiua.groundedtheory.LocatorService;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICodeInstance;

class CodeInstance implements ICodeInstance {

	private static final long serialVersionUID = 7683462895079284073L;
	private static final Logger LOGGER = Logger.getLogger(CodeInstance.class);

	private URI uri;
	private final long codeInstanceId;
	private final ICode code;
	private final URI id;
	private final TimeZoneDate creation;

	@XStreamOmitField
	private IIdentifier identifier;

	@XStreamOmitField
	private TimeZoneDateRange range;

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
		if (this.uri == null) {
			try {
				this.uri = new URI("apiua://"
						+ CodeInstanceLocatorProvider.CODE_INSTANCE_NAMESPACE
						+ "/" + this.codeInstanceId);
			} catch (Exception e) {
				throw new RuntimeException("Error calculating " + URI.class
						+ " for " + CodeInstance.class, e);
			}
		}
		return this.uri;
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

	@Override
	public IIdentifier getIdentifier() {
		if (this.identifier == null) {
			ILocatable locatable;
			try {
				locatable = LocatorService.INSTANCE.resolve(this.getId(), null)
						.get();
				if (locatable instanceof HasIdentifier) {
					this.identifier = ((HasIdentifier) locatable)
							.getIdentifier();
				}
			} catch (Exception e) {
				LOGGER.error("Error resolving " + this.getId(), e);
			}
		}
		return this.identifier;
	}

	@Override
	public TimeZoneDateRange getDateRange() {
		if (this.range == null) {
			ILocatable locatable;
			try {
				locatable = LocatorService.INSTANCE.resolve(this.getId(), null)
						.get();
				if (locatable instanceof HasDateRange) {
					this.range = ((HasDateRange) locatable).getDateRange();
				}
			} catch (Exception e) {
				LOGGER.error("Error resolving " + this.getId(), e);
			}
		}
		return this.range;
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
