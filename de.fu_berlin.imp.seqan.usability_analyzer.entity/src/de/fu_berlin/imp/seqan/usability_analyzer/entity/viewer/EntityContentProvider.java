package de.fu_berlin.imp.seqan.usability_analyzer.entity.viewer;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.seqan.usability_analyzer.core.util.ViewerUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.EntityManager;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeServiceListener;

public class EntityContentProvider implements IStructuredContentProvider,
		ITreeContentProvider {

	private Viewer viewer;
	private ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
			.getService(ICodeService.class);
	private ICodeServiceListener codeServiceListener = new ICodeServiceListener() {

		@Override
		public void codeAdded(ICode code) {
			ViewerUtils.refresh(viewer);
		}

		@Override
		public void codeAssigned(ICode code, List<ICodeable> codeables) {
			ViewerUtils.refresh(viewer);
		}

		@Override
		public void codeRemoved(ICode code, List<ICodeable> codeables) {
			ViewerUtils.refresh(viewer);
		}

		@Override
		public void codeDeleted(ICode code) {
			ViewerUtils.refresh(viewer);
		}
	};

	public EntityContentProvider() {
		codeService.addCodeServiceListener(codeServiceListener);
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = viewer;
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
		if (inputElement instanceof EntityManager) {
			return ((EntityManager) inputElement).getPersons().toArray();
		}

		return new Object[0];
	}

}
