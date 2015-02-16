package de.fu_berlin.imp.apiua.groundedtheory.model;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.eclipse.core.runtime.Assert;

import de.fu_berlin.imp.apiua.core.model.TimeZoneDate;
import de.fu_berlin.imp.apiua.core.model.URI;

/**
 * Is a {@link IRelationInstance} that grounds an {@link ImplicitRelation}.
 *
 * @author bkahlert
 *
 */
public class ProposedRelationInstance implements IRelationInstance {

	/**
	 *
	 */
	private static final long serialVersionUID = 4341304011219340106L;

	private URI uri;
	private IRelationInstance relationInstance;
	private ProposedRelation proposedRelation;

	@SuppressWarnings("unused")
	private ProposedRelationInstance() {
	}

	public ProposedRelationInstance(IRelationInstance explicitRelationInstance,
			ProposedRelation proposedRelation) {
		super();
		Assert.isLegal(explicitRelationInstance != null);
		Assert.isLegal(proposedRelation != null);
		Assert.isLegal(!(explicitRelationInstance.getRelation().getFrom()
				.equals(proposedRelation.getFrom()) && explicitRelationInstance
				.getRelation().getTo().equals(proposedRelation.getTo())));
		try {
			this.uri = new URI(explicitRelationInstance.getUri()
					+ "/implicit/"
					+ URLEncoder.encode(proposedRelation.getUri().toString(),
							"UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		this.relationInstance = explicitRelationInstance;
		this.proposedRelation = proposedRelation;
	}

	@Override
	public URI getUri() {
		return this.uri;
	}

	@Override
	public ProposedRelation getRelation() {
		return this.proposedRelation;
	}

	@Override
	public URI getPhenomenon() {
		return this.relationInstance.getPhenomenon();
	}

	@Override
	public TimeZoneDate getCreation() {
		return this.relationInstance.getCreation();
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
		ProposedRelationInstance other = (ProposedRelationInstance) obj;
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
		return this.getUri() + " (" + this.proposedRelation.toString()
				+ ") indirectly grounded by "
				+ this.relationInstance.getPhenomenon().toString();
	}

}
