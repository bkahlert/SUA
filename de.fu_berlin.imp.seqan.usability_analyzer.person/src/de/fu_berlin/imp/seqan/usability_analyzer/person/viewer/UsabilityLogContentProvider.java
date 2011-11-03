package de.fu_berlin.imp.seqan.usability_analyzer.person.viewer;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import de.fu_berlin.imp.seqan.usability_analyzer.person.PersonManager;

public class UsabilityLogContentProvider implements IStructuredContentProvider,
		ITreeContentProvider {

	private PersonManager personManager;

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (newInput instanceof PersonManager) {
			this.personManager = (PersonManager) newInput;
		} else {
			this.personManager = null;
		}
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public Object getParent(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (this.personManager == null)
			return new Object[0];

		return personManager.getPersons().toArray();
	}

}
