package de.fu_berlin.imp.seqan.usability_analyzer.entity.viewer;

import org.eclipse.jface.viewers.LabelProvider;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.model.Entity;

public class UsabilityLogLabelProvider extends LabelProvider {
	@Override
	public String getText(Object element) {
		Entity person = (Entity) element;
		ID id = person.getId();
		return (id != null) ? id.toString() : "";
	}
}
