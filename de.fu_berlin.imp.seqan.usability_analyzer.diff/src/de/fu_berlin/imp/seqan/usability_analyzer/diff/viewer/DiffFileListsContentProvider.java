package de.fu_berlin.imp.seqan.usability_analyzer.diff.viewer;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.nebula.colors.RGB;

import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiff;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecords;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffs;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.impl.DiffRecords;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeServiceListener;

public class DiffFileListsContentProvider implements
		IStructuredContentProvider, ITreeContentProvider {

	private Viewer viewer;

	private ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
			.getService(ICodeService.class);
	private ICodeServiceListener codeServiceListener = new ICodeServiceListener() {

		@Override
		public void codesAdded(List<ICode> code) {
			com.bkahlert.devel.nebula.utils.ViewerUtils.refresh(viewer);
		}

		@Override
		public void codesAssigned(List<ICode> code, List<ICodeable> codeables) {
			com.bkahlert.devel.nebula.utils.ViewerUtils.refresh(viewer);
		}

		@Override
		public void codeRenamed(ICode code, String oldCaption, String newCaption) {
			com.bkahlert.devel.nebula.utils.ViewerUtils.refresh(viewer);
		}

		@Override
		public void codeRecolored(ICode code, RGB oldColor, RGB newColor) {
			com.bkahlert.devel.nebula.utils.ViewerUtils.refresh(viewer);
		}

		@Override
		public void codesRemoved(List<ICode> codes, List<ICodeable> codeables) {
			com.bkahlert.devel.nebula.utils.ViewerUtils.refresh(viewer);
		}

		@Override
		public void codeMoved(ICode code, ICode oldParentCode,
				ICode newParentCode) {
			com.bkahlert.devel.nebula.utils.ViewerUtils.refresh(viewer);
		}

		@Override
		public void codeDeleted(ICode code) {
			com.bkahlert.devel.nebula.utils.ViewerUtils.refresh(viewer);
		}

		@Override
		public void memoAdded(ICode code) {
			com.bkahlert.devel.nebula.utils.ViewerUtils.refresh(viewer);
		}

		@Override
		public void memoAdded(ICodeable codeable) {
			com.bkahlert.devel.nebula.utils.ViewerUtils.refresh(viewer);
		}

		@Override
		public void memoModified(ICode code) {
		}

		@Override
		public void memoModified(ICodeable codeable) {
		}

		@Override
		public void memoRemoved(ICode code) {
			com.bkahlert.devel.nebula.utils.ViewerUtils.refresh(viewer);
		}

		@Override
		public void memoRemoved(ICodeable codeable) {
			com.bkahlert.devel.nebula.utils.ViewerUtils.refresh(viewer);
		}

		@Override
		public void episodeAdded(IEpisode episode) {
		}

		@Override
		public void episodeReplaced(IEpisode oldEpisode, IEpisode newEpisode) {
		}

		@Override
		public void episodesDeleted(Set<IEpisode> episodes) {
		}
	};

	public DiffFileListsContentProvider() {
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
		if (element instanceof IDiff) {
			return null;
		}
		if (element instanceof IDiffRecord) {
			return ((IDiffRecord) element).getDiffFile();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof IDiffs) {
			return ((IDiffs) element).length() > 0;
		}
		if (element instanceof IDiff) {
			DiffRecords diffFileRecords = ((IDiff) element)
					.getDiffFileRecords();
			return diffFileRecords != null && diffFileRecords.size() > 0;
		}
		return false;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IDiffs) {
			return ((IDiffs) parentElement).toArray();
		}
		if (parentElement instanceof IDiff) {
			IDiffRecords diffRecords = ((IDiff) parentElement)
					.getDiffFileRecords();
			return diffRecords != null ? diffRecords.toArray() : new Object[0];
		}
		return new Object[0];
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof Collection<?>) {
			Object[] objects = ((Collection<?>) inputElement).toArray();
			/*
			 * If the list contains only one element and this element is a list
			 * return the mentioned child list. This way we save one hierarchy
			 * level (= ID level).
			 */
			if (objects.length == 1 && objects[0] instanceof IDiffs) {
				return ((IDiffs) objects[0]).toArray();
			}
			return objects;
		}
		return new Object[0];
	}
}
