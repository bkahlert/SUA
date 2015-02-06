package de.fu_berlin.imp.apiua.groundedtheory.storage.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.eclipse.core.runtime.Assert;

import de.fu_berlin.imp.apiua.core.model.TimeZoneDate;
import de.fu_berlin.imp.apiua.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.apiua.groundedtheory.CodeInstanceLocatorProvider;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICodeInstance;

public class IsACodeInstance implements ICodeInstance {

	private static final long serialVersionUID = 1L;
	private ICode parentCode;
	private ICode childCode;
	private URI uri;
	private TimeZoneDate creation;
	private TimeZoneDateRange range;

	public IsACodeInstance(ICode parentCode, ICode childCode) {
		Assert.isLegal(parentCode != null);
		Assert.isLegal(childCode != null);
		this.parentCode = parentCode;
		this.childCode = childCode;
		this.creation = parentCode.getCreation().compareTo(
				childCode.getCreation()) <= 0 ? parentCode.getCreation()
				: childCode.getCreation();
		this.range = new TimeZoneDateRange(this.creation, this.creation);
		try {
			this.uri = new URI(
					"apiua://"
							+ CodeInstanceLocatorProvider.CODE_INSTANCE_NAMESPACE
							+ "/"
							+ URLEncoder.encode(parentCode.getUri().toString(),
									"UTF-8")
							+ "/parentOf/"
							+ URLEncoder.encode(childCode.getUri().toString(),
									"UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public TimeZoneDateRange getDateRange() {
		return this.range;
	}

	@Override
	public IIdentifier getIdentifier() {
		throw new UnsupportedOperationException();
	}

	@Override
	public long getCodeInstanceID() {
		throw new UnsupportedOperationException();
	}

	@Override
	public URI getUri() {
		return this.uri;
	}

	@Override
	public ICode getCode() {
		return this.parentCode;
	}

	@Override
	public URI getId() {
		return this.childCode.getUri();
	}

	@Override
	public TimeZoneDate getCreation() {
		return this.creation;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.childCode == null) ? 0 : this.childCode.hashCode());
		result = prime * result
				+ ((this.parentCode == null) ? 0 : this.parentCode.hashCode());
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
		IsACodeInstance other = (IsACodeInstance) obj;
		if (this.childCode == null) {
			if (other.childCode != null) {
				return false;
			}
		} else if (!this.childCode.equals(other.childCode)) {
			return false;
		}
		if (this.parentCode == null) {
			if (other.parentCode != null) {
				return false;
			}
		} else if (!this.parentCode.equals(other.parentCode)) {
			return false;
		}
		return true;
	}

}
