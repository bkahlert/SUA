package de.fu_berlin.imp.seqan.usability_analyzer.person.viewer;

import org.eclipse.jface.viewers.LabelProvider;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.person.model.Person;

public class UsabilityLogLabelProvider extends LabelProvider {
	@Override
	public String getText(Object element) {
		Person person = (Person) element;
		ID id = person.getId();
		return (id != null) ? id.toString() : "";
	}
}
