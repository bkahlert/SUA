package de.fu_berlin.imp.apiua.groundedtheory.model;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.TimeZoneDate;
import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.services.ILabelProviderService;

public class ProposedRelation implements ILocatable, IRelation {
	private static final long serialVersionUID = 1L;

	private URI uri;
	private IRelation explicitRelation;
	private URI from;
	private URI to;

	/**
	 * Creates a new {@link ProposedRelation} that actually points from
	 * <code>from</code> to <code>to</code> and therefore is a proposition for a
	 * not yet existing {@link IRelation}.
	 *
	 * @param explicitRelation
	 * @param from
	 * @param to
	 */
	public ProposedRelation(IRelation explicitRelation, URI from, URI to) {
		super();
		Assert.isLegal(explicitRelation != null);
		Assert.isLegal(from != null);
		Assert.isLegal(to != null);
		try {
			this.uri = new URI(explicitRelation.getUri().toString()
					+ "/proposed/from/"
					+ URLEncoder.encode(from.toString(), "UTF-8") + "/to/"
					+ URLEncoder.encode(to.toString(), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		this.explicitRelation = explicitRelation;
		this.from = from;
		this.to = to;
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
		return this.explicitRelation.getName();
	}

	public IRelation getExplicitRelation() {
		return this.explicitRelation;
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
		ProposedRelation other = (ProposedRelation) obj;
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
		ILabelProviderService labelProviderService = null;
		try {
			labelProviderService = (ILabelProviderService) PlatformUI
					.getWorkbench().getService(ILabelProviderService.class);
		} catch (NoClassDefFoundError e) {
			labelProviderService = new ILabelProviderService() {
				@Override
				public void addLabelProviderFactory(
						ILabelProviderFactory labelProviderFactory) {
				}

				@Override
				public void removeLabelProviderFactory(
						ILabelProviderFactory labelProviderFactory) {
				}

				@Override
				public ILabelProvider getLabelProvider(URI uri) {
					return null;
				}

				@Override
				public StyledString getStyledText(URI uri) {
					return new StyledString(uri.toString(), null);
				}

				@Override
				public String getText(URI uri) {
					return uri.toString();
				}

				@Override
				public Image getImage(URI uri) {
					return null;
				}
			};
		}
		return "Proposed Relation \"" + this.getName() + "\": "
				+ labelProviderService.getText(this.getFrom()) + " -> "
				+ labelProviderService.getText(this.getTo()) + " derived from "
				+ this.explicitRelation.toString();
	}

	@Override
	public TimeZoneDate getCreation() {
		return this.explicitRelation.getCreation();
	}

}
