package de.fu_berlin.imp.apiua.groundedtheory.storage.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.eclipse.core.runtime.Assert;

import de.fu_berlin.imp.apiua.core.model.TimeZoneDate;
import de.fu_berlin.imp.apiua.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICodeInstance;

public class ImplicitCodeInstance implements ICodeInstance {

	private static final long serialVersionUID = 1L;
	private ICodeInstance codeInstance;
	private ICode newCode;
	private URI uri;

	public ImplicitCodeInstance(ICodeInstance codeInstance, ICode newCode) {
		Assert.isLegal(codeInstance != null);
		Assert.isLegal(newCode != null);
		this.codeInstance = codeInstance;
		this.newCode = newCode;
		try {
			this.uri = new URI(codeInstance.getUri() + "/implicit/"
					+ URLEncoder.encode(newCode.getUri().toString(), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public TimeZoneDateRange getDateRange() {
		return this.codeInstance.getDateRange();
	}

	@Override
	public IIdentifier getIdentifier() {
		return this.codeInstance.getIdentifier();
	}

	@Override
	public long getCodeInstanceID() {
		return this.codeInstance.getCodeInstanceID();
	}

	@Override
	public URI getUri() {
		return this.uri;
	}

	@Override
	public ICode getCode() {
		return this.newCode;
	}

	@Override
	public URI getId() {
		return this.codeInstance.getId();
	}

	@Override
	public TimeZoneDate getCreation() {
		return this.codeInstance.getCreation();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((this.codeInstance == null) ? 0 : this.codeInstance
						.hashCode());
		result = prime * result
				+ ((this.newCode == null) ? 0 : this.newCode.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		ImplicitCodeInstance other = (ImplicitCodeInstance) obj;
		if (this.codeInstance == null) {
			if (other.codeInstance != null) {
				return false;
			}
		} else if (!this.codeInstance.equals(other.codeInstance)) {
			return false;
		}
		if (this.newCode == null) {
			if (other.newCode != null) {
				return false;
			}
		} else if (!this.newCode.equals(other.newCode)) {
			return false;
		}
		return true;
	}

}
