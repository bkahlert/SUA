package de.fu_berlin.imp.seqan.usability_analyzer.entity.viewer;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.seqan.usability_analyzer.entity.EntityManager;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceListener;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;

public class UsabilityLogContentProvider implements IStructuredContentProvider,
		ITreeContentProvider {

	private Viewer viewer;
	private ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
			.getService(ICodeService.class);

	private CodeServiceListener codeServiceListener = new CodeServiceListener() {

		@Override
		public void codeAdded(ICode code) {
			viewer.refresh();
		}

		@Override
		public void codeAssigned(ICode code, List<ICodeable> codeables) {
			viewer.refresh();
		}

		@Override
		public void codeRemoved(ICode code, List<ICodeable> codeables) {
			viewer.refresh();
		}

		@Override
		public void codeDeleted(ICode code) {
			viewer.refresh();
		}
	};

	private EntityManager personManager;

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = viewer;

		if (newInput instanceof EntityManager) {
			this.personManager = (EntityManager) newInput;
			codeService.addCodeServiceListener(codeServiceListener);
		} else {
			codeService.removeCodeServiceListener(codeServiceListener);
			this.personManager = null;
		}
	}

	@Override
	public void dispose() {
		codeService.removeCodeServiceListener(codeServiceListener);
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		return false;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		return null;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (this.personManager == null)
			return new Object[0];

		return personManager.getPersons().toArray();
	}

}
