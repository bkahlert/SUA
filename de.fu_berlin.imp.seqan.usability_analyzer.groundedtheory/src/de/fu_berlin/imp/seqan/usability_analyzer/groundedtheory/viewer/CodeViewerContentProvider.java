package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeServiceListener;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;
import de.fu_berlin.inf.nebula.utils.ViewerUtils;

public class CodeViewerContentProvider implements IStructuredContentProvider,
		ITreeContentProvider {

	private Viewer viewer;
	private ICodeService codeService;

	private ICodeServiceListener codeServiceListener = new ICodeServiceListener() {

		@Override
		public void codeAdded(ICode code) {
			ViewerUtils.refresh(viewer, true);
		}

		@Override
		public void codeAssigned(ICode code, List<ICodeable> codeables) {
			ViewerUtils.refresh(viewer, true);
		}

		@Override
		public void codeRenamed(ICode code, String oldCaption, String newCaption) {
			ViewerUtils.refresh(viewer, true);
		}

		@Override
		public void codeRemoved(ICode code, List<ICodeable> codeables) {
			ViewerUtils.refresh(viewer, true);
		}

		@Override
		public void codeMoved(ICode code, ICode oldParentCode,
				ICode newParentCode) {
			ViewerUtils.refresh(viewer, true);
		}

		@Override
		public void codeDeleted(ICode code) {
			ViewerUtils.refresh(viewer, true);
		}
	};

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = viewer;

		if (ICodeService.class.isInstance(newInput)) {
			this.codeService = (ICodeService) newInput;
			this.codeService.addCodeServiceListener(codeServiceListener);
		} else {
			if (this.codeService != null) {
				this.codeService.removeCodeServiceListener(codeServiceListener);
			}
			this.codeService = null;
		}
	}

	@Override
	public void dispose() {
		if (this.codeService != null) {
			this.codeService.removeCodeServiceListener(codeServiceListener);
		}
	}

	@Override
	public Object getParent(Object element) {
		if (ICodeInstance.class.isInstance(element)) {
			return ((ICodeInstance) element).getCode();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (ICode.class.isInstance(element)) {
			ICode code = (ICode) element;
			for (ICodeInstance codeInstance : codeService.getCodeStore()
					.loadInstances()) {
				if (codeInstance.getCode().equals(code))
					return true;
			}
		}
		return false;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (ICode.class.isInstance(parentElement)) {
			ICode code = (ICode) parentElement;

			// Child Codes
			ArrayList<Object> childNodes = new ArrayList<Object>();
			childNodes.addAll(code.getChildCodes());

			// Instances
			for (ICodeInstance codeInstance : codeService.getCodeStore()
					.loadInstances()) {
				if (codeInstance.getCode().equals(code))
					childNodes.add(codeInstance);
			}

			return childNodes.toArray();
		}
		return null;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (!(inputElement instanceof ICodeService))
			return new Object[0];

		Set<ICode> codes = ((ICodeService) inputElement).getCodeStore()
				.getTopLevelCodes();
		if (codes.size() > 0)
			return codes.toArray();
		else
			return new Object[] { new NoCodesNode() };
	}

}
