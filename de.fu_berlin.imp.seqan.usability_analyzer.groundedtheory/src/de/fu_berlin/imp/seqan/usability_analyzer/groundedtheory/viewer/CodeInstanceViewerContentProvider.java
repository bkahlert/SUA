package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer;

import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.nebula.colors.RGB;
import com.bkahlert.devel.nebula.utils.ViewerUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeServiceListener2;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;

public class CodeInstanceViewerContentProvider implements
		IStructuredContentProvider, ITreeContentProvider {

	private Viewer viewer;
	private ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
			.getService(ICodeService.class);
	private List<ILocatable> codeables;

	private ICodeServiceListener2 codeServiceListener = new ICodeServiceListener2() {

		@Override
		public void codesAdded(List<ICode> codes) {
			ViewerUtils.refresh(CodeInstanceViewerContentProvider.this.viewer,
					true);
		}

		@Override
		public void codesAssigned(List<ICode> codes, List<ILocatable> codeables) {
			ViewerUtils.refresh(CodeInstanceViewerContentProvider.this.viewer,
					true);
		}

		@Override
		public void codeRenamed(ICode code, String oldCaption, String newCaption) {
			ViewerUtils.refresh(CodeInstanceViewerContentProvider.this.viewer,
					true);
		}

		@Override
		public void codeRecolored(ICode code, RGB oldColor, RGB newColor) {
			ViewerUtils.refresh(CodeInstanceViewerContentProvider.this.viewer,
					true);
		}

		@Override
		public void codesRemoved(List<ICode> codes, List<ILocatable> codeables) {
			ViewerUtils.refresh(CodeInstanceViewerContentProvider.this.viewer,
					true);
		}

		@Override
		public void codeMoved(ICode code, ICode oldParentCode,
				ICode newParentCode) {
			ViewerUtils.refresh(CodeInstanceViewerContentProvider.this.viewer,
					true);
		}

		@Override
		public void codeDeleted(ICode code) {
			ViewerUtils.refresh(CodeInstanceViewerContentProvider.this.viewer,
					true);
		}

		@Override
		public void memoAdded(ICode code) {
			ViewerUtils.update(CodeInstanceViewerContentProvider.this.viewer,
					code, null);
		}

		@Override
		public void memoAdded(ILocatable codeable) {
			ViewerUtils.update(CodeInstanceViewerContentProvider.this.viewer,
					codeable, null);
		}

		@Override
		public void memoAdded(ICodeInstance codeInstance) {
			ViewerUtils.update(CodeInstanceViewerContentProvider.this.viewer,
					codeInstance, null);
		}

		@Override
		public void memoModified(ICode code) {
		}

		@Override
		public void memoModified(ICodeInstance codeInstance) {
		}

		@Override
		public void memoModified(ILocatable codeable) {
		}

		@Override
		public void memoRemoved(ICode code) {
			ViewerUtils.update(CodeInstanceViewerContentProvider.this.viewer,
					code, null);
		}

		@Override
		public void memoRemoved(ILocatable codeable) {
			ViewerUtils.update(CodeInstanceViewerContentProvider.this.viewer,
					codeable, null);
		}

		@Override
		public void memoRemoved(ICodeInstance codeInstance) {
			ViewerUtils.update(CodeInstanceViewerContentProvider.this.viewer,
					codeInstance, null);
		}

		@Override
		public void episodeAdded(IEpisode episode) {
			ViewerUtils.refresh(CodeInstanceViewerContentProvider.this.viewer,
					false);
		}

		@Override
		public void episodeReplaced(IEpisode oldEpisode, IEpisode newEpisode) {
			ViewerUtils.refresh(CodeInstanceViewerContentProvider.this.viewer,
					false);
		}

		@Override
		public void episodesDeleted(Set<IEpisode> episodes) {
			ViewerUtils.refresh(CodeInstanceViewerContentProvider.this.viewer,
					false);
		}
	};

	@SuppressWarnings("unchecked")
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = viewer;

		if (List.class.isInstance(oldInput)) {
			this.codeService
					.removeCodeServiceListener(this.codeServiceListener);
		}

		if (List.class.isInstance(newInput)) {
			this.codeables = (List<ILocatable>) newInput;
			this.codeService.addCodeServiceListener(this.codeServiceListener);
		} else {
			if (this.codeables != null) {
				this.codeService
						.removeCodeServiceListener(this.codeServiceListener);
				this.codeables = null;
			}
		}
	}

	@Override
	public void dispose() {
		if (this.codeables != null) {
			this.codeService
					.removeCodeServiceListener(this.codeServiceListener);
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
		if (ILocatable.class.isInstance(element)) {
			return true;
		}
		Object[] children = this.getChildren(element);
		return children != null ? this.getChildren(element).length > 0 : false;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (ILocatable.class.isInstance(parentElement)) {
			ILocatable codeable = (ILocatable) parentElement;
			try {
				List<ICode> codes = this.codeService.getCodes(codeable);
				if (codes.size() > 0) {
					return codes.toArray();
				} else {
					return new Object[] { new NoCodesNode() };
				}
			} catch (CodeServiceException e) {
				return null;
			}
		}
		if (ICode.class.isInstance(parentElement)) {
			ICode code = (ICode) parentElement;
			ICode parentCode = this.codeService.getParent(code);
			if (parentCode != null) {
				return new Object[] { parentCode };
			} else {
				return new Object[0];
			}
		}
		return null;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (!(inputElement instanceof List)) {
			return new Object[0];
		}

		List<?> list = (List<?>) inputElement;
		if (list.size() > 0) {
			return list.toArray();
		} else {
			return new Object[] { new NoCodesNode() };
		}
	}

}
