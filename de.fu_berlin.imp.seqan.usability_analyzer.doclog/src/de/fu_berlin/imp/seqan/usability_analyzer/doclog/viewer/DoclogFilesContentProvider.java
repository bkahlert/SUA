package de.fu_berlin.imp.seqan.usability_analyzer.doclog.viewer;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.Doclog;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeServiceListener;

public class DoclogFilesContentProvider implements IStructuredContentProvider,
		ITreeContentProvider {

	private Viewer viewer;
	private ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
			.getService(ICodeService.class);
	private ICodeServiceListener codeServiceListener = new ICodeServiceListener() {

		@Override
		public void codesAdded(List<ICode> code) {
			com.bkahlert.devel.nebula.utils.ViewerUtils.refresh(viewer);
		}

		@Override
		public void codesAssigned(List<ICode> codes, List<ICodeable> codeables) {
			com.bkahlert.devel.nebula.utils.ViewerUtils.refresh(viewer);
		}

		@Override
		public void codeRenamed(ICode code, String oldCaption, String newCaption) {
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
		public void memoModified(ICode code) {
			com.bkahlert.devel.nebula.utils.ViewerUtils.refresh(viewer);
		}

		@Override
		public void memoModified(ICodeable codeable) {
			com.bkahlert.devel.nebula.utils.ViewerUtils.refresh(viewer);
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

	public DoclogFilesContentProvider() {
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
		if (element instanceof DoclogRecord) {
			return ((DoclogRecord) element).getDoclog();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof Doclog) {
			return ((Doclog) element).getDoclogRecords().size() > 0;
		}
		return false;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof Doclog) {
			return ((Doclog) parentElement).getDoclogRecords().toArray();
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
			if (objects.length == 1 && objects[0] instanceof Doclog) {
				return ((Doclog) objects[0]).getDoclogRecords().toArray();
			}
			return objects;
		}
		return new Object[0];
	}
}
