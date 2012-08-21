package de.fu_berlin.imp.seqan.usability_analyzer.diff.viewer;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.seqan.usability_analyzer.core.util.ViewerUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFile;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFileList;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFileRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.DiffFileRecordList;
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
			ViewerUtils.refresh(viewer);
		}

		@Override
		public void codesAssigned(List<ICode> code, List<ICodeable> codeables) {
			ViewerUtils.refresh(viewer);
		}

		@Override
		public void codeRenamed(ICode code, String oldCaption, String newCaption) {
			ViewerUtils.refresh(viewer);
		}

		@Override
		public void codesRemoved(List<ICode> codes, List<ICodeable> codeables) {
			ViewerUtils.refresh(viewer);
		}

		@Override
		public void codeMoved(ICode code, ICode oldParentCode,
				ICode newParentCode) {
			ViewerUtils.refresh(viewer);
		}

		@Override
		public void codeDeleted(ICode code) {
			ViewerUtils.refresh(viewer);
		}

		@Override
		public void memoModified(ICode code) {
			ViewerUtils.refresh(viewer);
		}

		@Override
		public void memoModified(ICodeable codeable) {
			ViewerUtils.refresh(viewer);
		}

		@Override
		public void episodeAdded(IEpisode episode) {
			// TODO Auto-generated method stub

		}

		@Override
		public void episodeReplaced(IEpisode oldEpisode, IEpisode newEpisode) {
			// TODO Auto-generated method stub

		}

		@Override
		public void episodesDeleted(Set<IEpisode> episodes) {
			// TODO Auto-generated method stub

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
		if (element instanceof DiffFile) {
			return null;
		}
		if (element instanceof DiffFileRecord) {
			return ((DiffFileRecord) element).getDiffFile();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof DiffFileList) {
			return ((DiffFileList) element).size() > 0;
		}
		if (element instanceof DiffFile) {
			DiffFileRecordList diffFileRecords = ((DiffFile) element)
					.getDiffFileRecords();
			return diffFileRecords != null && diffFileRecords.size() > 0;
		}
		return false;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof DiffFileList) {
			return ((DiffFileList) parentElement).toArray();
		}
		if (parentElement instanceof DiffFile) {
			DiffFileRecordList diffFileRecords = ((DiffFile) parentElement)
					.getDiffFileRecords();
			return diffFileRecords != null ? diffFileRecords.toArray()
					: new Object[0];
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
			if (objects.length == 1 && objects[0] instanceof DiffFileList) {
				return ((DiffFileList) objects[0]).toArray();
			}
			return objects;
		}
		return new Object[0];
	}
}
