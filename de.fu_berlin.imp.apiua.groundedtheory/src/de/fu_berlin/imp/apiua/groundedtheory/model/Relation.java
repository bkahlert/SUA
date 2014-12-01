package de.fu_berlin.imp.apiua.groundedtheory.model;

import org.eclipse.core.runtime.Assert;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.TimeZoneDate;
import de.fu_berlin.imp.apiua.core.model.URI;

public class Relation implements ILocatable, IRelation {
	private static final long serialVersionUID = 1L;

	private URI uri;
	private URI from;
	private URI to;
	private String name;

	private TimeZoneDate timeZoneDate;

	@SuppressWarnings("unused")
	private Relation() {
		// needed by persistence
	}

	public Relation(URI uri, URI from, URI to, String name) {
		super();
		Assert.isLegal(uri != null);
		Assert.isLegal(to != null);
		Assert.isLegal(from != null);
		Assert.isLegal(name != null);
		Assert.isLegal(from.getHost().equals(to.getHost()));
		this.uri = uri;
		this.from = from;
		this.to = to;
		this.name = name;
		this.timeZoneDate = new TimeZoneDate();
	}

	@Override
	public URI getUri() {
		return this.uri;
	}

	@Override
	public URI getFrom() {
		return this.from;
	}

	@Override
	public URI getTo() {
		return this.to;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.uri == null) ? 0 : this.uri.hashCode());
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
		Relation other = (Relation) obj;
		if (this.uri == null) {
			if (other.uri != null) {
				return false;
			}
		} else if (!this.uri.equals(other.uri)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		ILabelProviderService labelProviderService = (ILabelProviderService) PlatformUI
				.getWorkbench().getService(ILabelProviderService.class);
		return "Relation \"" + this.getName() + "\": "
				+ labelProviderService.getText(this.getFrom()) + " -> "
				+ labelProviderService.getText(this.getTo());
	}

	@Override
	public TimeZoneDate getCreation() {
		return this.timeZoneDate;
	}

}
