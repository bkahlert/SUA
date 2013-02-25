package de.fu_berlin.imp.seqan.usability_analyzer.entity.viewer;

import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.nebula.colors.RGB;
import com.bkahlert.devel.nebula.utils.ViewerUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.entity.EntityManager;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.model.EntityDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeServiceListener;

public class EntityContentProvider implements IStructuredContentProvider,
		ITreeContentProvider {

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
		public void codeRecolored(ICode code, RGB oldColor, RGB newColor) {
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
		public void memoAdded(ICode code) {
			ViewerUtils.refresh(viewer);
		}

		@Override
		public void memoAdded(ICodeable codeable) {
			ViewerUtils.refresh(viewer);
		}

		@Override
		public void memoModified(ICode code) {
		}

		@Override
		public void memoModified(ICodeable codeable) {
		}

		@Override
		public void memoRemoved(ICode code) {
			ViewerUtils.refresh(viewer);
		}

		@Override
		public void memoRemoved(ICodeable codeable) {
			ViewerUtils.refresh(viewer);
		}

		@Override
		public void episodeAdded(IEpisode episode) {
			ViewerUtils.refresh(viewer);
		}

		@Override
		public void episodeReplaced(IEpisode oldEpisode, IEpisode newEpisode) {
			ViewerUtils.refresh(viewer);
		}

		@Override
		public void episodesDeleted(Set<IEpisode> episodes) {
			ViewerUtils.refresh(viewer);
		}
	};

	public EntityContentProvider() {
		codeService.addCodeServiceListener(codeServiceListener);
	}

	@SuppressWarnings("unused")
	@PostConstruct
	private void init() {

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
		if (inputElement instanceof EntityDataContainer) {
			return ((EntityDataContainer) inputElement).getEntityManager()
					.getPersons().toArray();
		}

		throw new RuntimeException("Invalid Input. Expected "
				+ EntityManager.class);
	}

}
