package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceListener;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;
import de.fu_berlin.inf.nebula.utils.ViewerUtils;

public class CodeInstanceViewerContentProvider implements
		IStructuredContentProvider, ITreeContentProvider {

	private Viewer viewer;
	private ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
			.getService(ICodeService.class);
	private List<ICodeable> codeables;

	private CodeServiceListener codeServiceListener = new CodeServiceListener() {

		@Override
		public void codeAdded(ICode code) {
			ViewerUtils.refresh(viewer, true);
		}

		@Override
		public void codeAssigned(ICode code, List<ICodeable> codeables) {
			ViewerUtils.refresh(viewer, true);
		}

		@Override
		public void codeRemoved(ICode code, List<ICodeable> codeables) {
			ViewerUtils.refresh(viewer, true);
		}

		@Override
		public void codeDeleted(ICode code) {
			ViewerUtils.refresh(viewer, true);
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
			ICodeable codeable = (ICodeable) element;
			try {
				return codeService.getCodes(codeable).size() > 0;
			} catch (CodeServiceException e) {

			}
		}
		return false;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (ICodeable.class.isInstance(parentElement)) {
			ICodeable codeable = (ICodeable) parentElement;
			try {
				ArrayList<ICode> codes = new ArrayList<ICode>();
				for (ICode code : codeService.getCodes(codeable)) {
					codes.add(code);
				}
				return codes.toArray();
			} catch (CodeServiceException e) {
				return null;
			}
		}
		return null;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (!(inputElement instanceof List))
			return new Object[0];

		return ((List<?>) inputElement).toArray();
	}

}
