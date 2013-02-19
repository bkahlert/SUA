package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.bkahlert.devel.nebula.colors.RGB;
import com.bkahlert.devel.nebula.utils.ViewerUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeServiceListener2;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;

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
		public void codesAdded(List<ICode> codes) {
			ViewerUtils.refresh(viewer, false);
			for (ICode code : codes)
				ViewerUtils.expandAll(viewer, code);
		}

		@Override
		public void codesAssigned(List<ICode> codes, List<ICodeable> codeables) {
			for (ICode code : codes)
				ViewerUtils.refresh(viewer, code, true);
		}

		@Override
		public void codeRenamed(ICode code, String oldCaption, String newCaption) {
			ViewerUtils.update(viewer, code, null);
		}

		@Override
		public void codeRecolored(ICode code, RGB oldColor, RGB newColor) {
			ViewerUtils.refresh(viewer, true); // TODO check if update is enough
		}

		@Override
		public void codesRemoved(List<ICode> codes, List<ICodeable> codeables) {
			ViewerUtils.refresh(viewer, true);
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

		@Override
		public void episodeAdded(IEpisode episode) {
		}

		@Override
		public void episodeReplaced(IEpisode oldEpisode, IEpisode newEpisode) {
			ViewerUtils.refresh(viewer, true);
		}

		@Override
		public void episodesDeleted(Set<IEpisode> episodes) {
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
			if (code.getCaption().equals("Hey2")) {
				System.err.println("hey2");
			}

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
