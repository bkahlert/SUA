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
public class ImplicitRelationInstance implements IRelationInstance {

	/**
	 *
	 */
	private static final long serialVersionUID = 4341304011219340106L;

	private URI uri;
	private IRelationInstance relationInstance;
	private ImplicitRelation implicitRelation;

	@SuppressWarnings("unused")
	private ImplicitRelationInstance() {
	}

	public ImplicitRelationInstance(IRelationInstance explicitRelationInstance,
			ImplicitRelation implicitRelation) {
		super();
		Assert.isLegal(explicitRelationInstance != null);
		Assert.isLegal(implicitRelation != null);
		Assert.isLegal(!(explicitRelationInstance.getRelation().getFrom()
				.equals(implicitRelation.getFrom()) && explicitRelationInstance
				.getRelation().getTo().equals(implicitRelation.getTo())));
		try {
			this.uri = new URI(explicitRelationInstance.getUri()
					+ "/implicit/"
					+ URLEncoder.encode(implicitRelation.getUri().toString(),
							"UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		this.relationInstance = explicitRelationInstance;
		this.implicitRelation = implicitRelation;
	}

	@Override
	public URI getUri() {
		return this.uri;
	}

	@Override
	public ImplicitRelation getRelation() {
		return this.implicitRelation;
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
		ImplicitRelationInstance other = (ImplicitRelationInstance) obj;
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
		return this.getUri() + " (" + this.implicitRelation.toString()
				+ ") indirectly grounded by "
				+ this.relationInstance.getPhenomenon().toString();
	}

}
