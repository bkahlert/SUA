package de.fu_berlin.imp.apiua.groundedtheory.views;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.CalendarUtils;
import com.bkahlert.nebula.utils.StringUtils;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.TimeZoneDate;
import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.services.ILabelProviderService;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelation;
import de.fu_berlin.imp.apiua.groundedtheory.model.ProposedRelation;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;

public class MergedProposedRelation implements ILocatable, IRelation {
	private static final long serialVersionUID = 1L;

	private URI uri;
	private List<ProposedRelation> proposedRelations;
	private URI from;
	private URI to;
	private String name;
	private TimeZoneDate creation;

	/**
	 * Creates a new {@link MergedProposedRelation} that actually points from
	 * <code>from</code> to <code>to</code> and therefore is a proposition for a
	 * not yet existing {@link IRelation}.
	 *
	 * @param proposedRelations
	 * @param from
	 * @param to
	 */
	public MergedProposedRelation(List<ProposedRelation> proposedRelations) {
		super();
		Assert.isLegal(proposedRelations != null);
		Assert.isLegal(proposedRelations.size() > 0);
		this.uri = new URI("apiua://relation/merged/"
				+ proposedRelations.stream().map(r -> r.getUri() + r.getName())
						.collect(Collectors.toList()).hashCode());
		this.proposedRelations = proposedRelations;

		ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
				.getService(ICodeService.class);

		List<URI> froms = proposedRelations.stream().map(r -> r.getFrom())
				.collect(Collectors.toList());
		List<URI> tos = proposedRelations.stream().map(r -> r.getTo())
				.collect(Collectors.toList());

		this.from = codeService.getCommonAncestor(froms);
		this.to = codeService.getCommonAncestor(tos);

		List<String> names = new ArrayList<>();
		for (ProposedRelation r : proposedRelations) {
			if (!names.contains(r.getName())) {
				names.add(r.getName());
			}
		}
		this.name = StringUtils.join(names, " | ");

		this.creation = new TimeZoneDate(
				CalendarUtils.getLatestCalendar(proposedRelations.stream()
						.map(r -> r.getCreation().getCalendar())
						.collect(Collectors.toList())));
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

	public List<ProposedRelation> getProposedRelations() {
		return this.proposedRelations;
	}

	public List<IRelation> getExplicitRelations() {
		return this.proposedRelations.stream()
				.map(r -> r.getExplicitRelation()).collect(Collectors.toList());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (this.uri == null ? 0 : this.uri.hashCode());
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
		MergedProposedRelation other = (MergedProposedRelation) obj;
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
		return "Merged Proposed Relation \"" + this.getName() + "\": "
				+ labelProviderService.getText(this.getFrom()) + " -> "
				+ labelProviderService.getText(this.getTo()) + " derived from "
				+ this.proposedRelations.toString();
	}

	@Override
	public TimeZoneDate getCreation() {
		return this.creation;
	}

}