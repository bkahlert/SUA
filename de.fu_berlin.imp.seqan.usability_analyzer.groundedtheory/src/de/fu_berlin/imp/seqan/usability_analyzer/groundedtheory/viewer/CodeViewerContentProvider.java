package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeServiceListener2;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;
import de.fu_berlin.inf.nebula.utils.ViewerUtils;

public class CodeViewerContentProvider implements IStructuredContentProvider,
		ITreeContentProvider {

	private Viewer viewer;
	private ICodeService codeService;

	/**
	 * If false no {@link ICodeInstance}s are shown.
	 */
	private boolean showInstances;

	private ICodeServiceListener2 codeServiceListener = new ICodeServiceListener2() {

		@Override
		public void codeAdded(ICode code) {
			ViewerUtils.refresh(viewer, false);
			ViewerUtils.expandAll(viewer, code);
		}

		@Override
		public void codeAssigned(ICode code, List<ICodeable> codeables) {
			ViewerUtils.refresh(viewer, code, true);
		}

		@Override
		public void codeRenamed(ICode code, String oldCaption, String newCaption) {
			ViewerUtils.update(viewer, code, null);
		}

		@Override
		public void codeRemoved(ICode code, List<ICodeable> codeables) {
			ViewerUtils.refresh(viewer, false);
		}

		@Override
		public void codeMoved(ICode code, ICode oldParentCode,
				ICode newParentCode) {
			if (oldParentCode == null || newParentCode == null) {
				ViewerUtils.refresh(viewer, false);
			} else {
				if (oldParentCode != null)
					ViewerUtils.refresh(viewer, oldParentCode, true);
				if (newParentCode != null)
					ViewerUtils.refresh(viewer, newParentCode, true);
			}
		}

		@Override
		public void codeDeleted(ICode code) {
			ViewerUtils.remove(viewer, code);
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

	/**
	 * Creates a new {@link CodeViewerContentProvider} that displays all
	 * {@link ICode}s and optionally {@link ICodeInstance}s.
	 * 
	 * @param showInstances
	 *            false if only {@link ICode}s should be displayed
	 */
	public CodeViewerContentProvider(boolean showInstances) {
		this.showInstances = showInstances;
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = viewer;

		if (this.codeService != null) {
			this.codeService.removeCodeServiceListener(codeServiceListener);
		}
		this.codeService = null;

		if (ICodeService.class.isInstance(newInput)) {
			this.codeService = (ICodeService) newInput;
			this.codeService.addCodeServiceListener(codeServiceListener);
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
		if (ICode.class.isInstance(element)) {
			ICode code = (ICode) element;
			if (this.codeService != null) {
				ICode parent = this.codeService.getParent(code);
				return parent;
			}
			return null;
		}
		if (ICodeInstance.class.isInstance(element)) {
			return ((ICodeInstance) element).getCode();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (ICode.class.isInstance(element)) {
			ICode code = (ICode) element;
			if (this.codeService.getChildren(code).size() > 0)
				return true;
			if (this.showInstances
					&& this.codeService.getInstances(code).size() > 0)
				return true;
		}
		return false;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (ICode.class.isInstance(parentElement)) {
			ICode code = (ICode) parentElement;

			ArrayList<Object> childNodes = new ArrayList<Object>();
			childNodes.addAll(this.codeService.getChildren(code));
			if (this.showInstances)
				childNodes.addAll(this.codeService.getInstances(code));

			return childNodes.toArray();
		}
		return null;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (!(inputElement instanceof ICodeService))
			return new Object[0];

		List<ICode> codes = ((ICodeService) inputElement).getTopLevelCodes();
		if (codes.size() > 0)
			return codes.toArray();
		else
			return new Object[] { new NoCodesNode() };
	}

}
