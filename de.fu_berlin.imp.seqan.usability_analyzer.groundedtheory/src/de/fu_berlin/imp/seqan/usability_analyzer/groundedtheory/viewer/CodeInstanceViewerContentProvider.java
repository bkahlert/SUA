package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeServiceListener2;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;
import de.fu_berlin.inf.nebula.utils.ViewerUtils;

public class CodeInstanceViewerContentProvider implements
		IStructuredContentProvider, ITreeContentProvider {

	private Viewer viewer;
	private ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
			.getService(ICodeService.class);
	private List<ICodeable> codeables;

	private ICodeServiceListener2 codeServiceListener = new ICodeServiceListener2() {

		@Override
		public void codesAdded(List<ICode> codes) {
			ViewerUtils.refresh(viewer, true);
		}

		@Override
		public void codesAssigned(List<ICode> codes, List<ICodeable> codeables) {
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

		@Override
		public void memoModified(ICode code) {
			ViewerUtils.update(viewer, code, null);
		}

		@Override
		public void memoModified(ICodeInstance codeInstance) {
			ViewerUtils.update(viewer, codeInstance, null);
		}

		@Override
		public void memoModified(ICodeable codeable) {
			ViewerUtils.update(viewer, codeable, null);
		}
	};

	@SuppressWarnings("unchecked")
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = viewer;

		if (List.class.isInstance(newInput)) {
			this.codeables = (List<ICodeable>) newInput;
			this.codeService.addCodeServiceListener(codeServiceListener);
		} else {
			if (this.codeables != null) {
				this.codeService.removeCodeServiceListener(codeServiceListener);
			}
			this.codeables = null;
		}
	}

	@Override
	public void dispose() {
		if (this.codeables != null) {
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
		if (ICodeable.class.isInstance(element)) {
			return true;
		}
		Object[] children = getChildren(element);
		return children != null ? getChildren(element).length > 0 : false;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (ICodeable.class.isInstance(parentElement)) {
			ICodeable codeable = (ICodeable) parentElement;
			try {
				List<ICode> codes = codeService.getCodes(codeable);
				if (codes.size() > 0)
					return codes.toArray();
				else
					return new Object[] { new NoCodesNode() };
			} catch (CodeServiceException e) {
				return null;
			}
		}
		if (ICode.class.isInstance(parentElement)) {
			ICode code = (ICode) parentElement;
			ICode parentCode = codeService.getParent(code);
			if (parentCode != null)
				return new Object[] { parentCode };
			else
				return new Object[0];
		}
		return null;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (!(inputElement instanceof List))
			return new Object[0];

		List<?> list = (List<?>) inputElement;
		if (list.size() > 0)
			return list.toArray();
		else
			return new Object[] { new NoCodesNode() };
	}

}
