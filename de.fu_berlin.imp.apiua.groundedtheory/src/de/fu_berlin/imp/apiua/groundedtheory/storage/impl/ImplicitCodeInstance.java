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
	private ICodeInstance explicitCodeInstance;
	private ICode newCode;
	private URI uri;

	public ImplicitCodeInstance(ICodeInstance explicitCodeInstance,
			ICode newCode) {
		Assert.isLegal(explicitCodeInstance != null);
		Assert.isLegal(newCode != null);
		this.explicitCodeInstance = explicitCodeInstance;
		this.newCode = newCode;
		try {
			this.uri = new URI(explicitCodeInstance.getUri() + "/implicit/"
					+ URLEncoder.encode(newCode.getUri().toString(), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public TimeZoneDateRange getDateRange() {
		return this.explicitCodeInstance.getDateRange();
	}

	@Override
	public IIdentifier getIdentifier() {
		return this.explicitCodeInstance.getIdentifier();
	}

	@Override
	public long getCodeInstanceID() {
		return this.explicitCodeInstance.getCodeInstanceID();
	}

	public ICodeInstance getExplicitCodeInstance() {
		return this.explicitCodeInstance;
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
		return this.explicitCodeInstance.getId();
	}

	@Override
	public TimeZoneDate getCreation() {
		return this.explicitCodeInstance.getCreation();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((this.explicitCodeInstance == null) ? 0
						: this.explicitCodeInstance.hashCode());
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
		if (this.explicitCodeInstance == null) {
			if (other.explicitCodeInstance != null) {
				return false;
			}
		} else if (!this.explicitCodeInstance
				.equals(other.explicitCodeInstance)) {
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
