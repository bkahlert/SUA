package de.fu_berlin.imp.apiua.groundedtheory.model;

import org.eclipse.core.runtime.Assert;

import de.fu_berlin.imp.apiua.core.model.TimeZoneDate;
import de.fu_berlin.imp.apiua.core.model.URI;

public class RelationInstance implements IRelationInstance {

	/**
	 *
	 */
	private static final long serialVersionUID = 4341304011219340106L;

	private URI uri;
	private IRelation relation;
	private URI phenomenon;
	private TimeZoneDate creation;

	@SuppressWarnings("unused")
	private RelationInstance() {
	}

	public RelationInstance(URI uri, IRelation relation, URI phenomenon) {
		super();
		Assert.isLegal(uri != null);
		Assert.isLegal(relation != null);
		Assert.isLegal(phenomenon != null);
		this.uri = uri;
		this.relation = relation;
		this.phenomenon = phenomenon;
		this.creation = new TimeZoneDate();
	}

	@Override
	public URI getUri() {
		return this.uri;
	}

	@Override
	public IRelation getRelation() {
		return this.relation;
	}

	@Override
	public URI getPhenomenon() {
		return this.phenomenon;
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
		RelationInstance other = (RelationInstance) obj;
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
		return this.getUri() + " (" + this.relation.toString()
				+ ") grounded by " + this.phenomenon.toString();
	}

}
