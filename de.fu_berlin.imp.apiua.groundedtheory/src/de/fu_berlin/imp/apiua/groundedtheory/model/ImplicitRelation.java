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

public class ImplicitRelation implements ILocatable, IRelation {
	private static final long serialVersionUID = 1L;

	private URI uri;
	private IRelation explicitRelation;
	private IRelation implicitFor;

	/**
	 * Creates a new {@link ImplicitRelation} that actually points from
	 * <code>from</code> to <code>to</code> and therefore implicitly models the
	 * given {@link IRelation}.
	 *
	 * @param explicitRelation
	 * @param from
	 * @param to
	 */
	public ImplicitRelation(IRelation explicitRelation, IRelation implicitFor) {
		super();
		Assert.isLegal(explicitRelation.getName().equals(implicitFor.getName()));
		Assert.isLegal(explicitRelation != null);
		Assert.isLegal(implicitFor != null);
		try {
			this.uri = new URI(explicitRelation.getUri().toString()
					+ "/implicit/"
					+ URLEncoder.encode(implicitFor.getUri().toString(),
							"UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		this.explicitRelation = explicitRelation;
		this.implicitFor = implicitFor;
	}

	@Override
	public URI getUri() {
		return this.uri;
	}

	@Override
	public URI getFrom() {
		return this.implicitFor.getFrom();
	}

	@Override
	public URI getTo() {
		return this.implicitFor.getTo();
	}

	@Override
	public String getName() {
		return this.explicitRelation.getName();
	}

	public IRelation getImplicitFor() {
		return this.implicitFor;
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
		ImplicitRelation other = (ImplicitRelation) obj;
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
		return "Implicit Relation \"" + this.getName() + "\": "
				+ labelProviderService.getText(this.getFrom()) + " -> "
				+ labelProviderService.getText(this.getTo()) + " derived from "
				+ this.explicitRelation.toString();
	}

	@Override
	public TimeZoneDate getCreation() {
		return this.explicitRelation.getCreation();
	}

}
