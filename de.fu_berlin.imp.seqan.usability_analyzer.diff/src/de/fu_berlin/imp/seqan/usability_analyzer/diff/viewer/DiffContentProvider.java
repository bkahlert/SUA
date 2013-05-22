package de.fu_berlin.imp.seqan.usability_analyzer.diff.viewer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.nebula.colors.RGB;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.ICompilable;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiff;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecords;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffs;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.impl.DiffRecords;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.services.CompilationServiceAdapter;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.services.ICompilationService;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.services.ICompilationServiceListener;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeServiceListener;

public class DiffContentProvider implements IStructuredContentProvider,
		ITreeContentProvider {

	private Viewer viewer;

	private ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
			.getService(ICodeService.class);
	private ICodeServiceListener codeServiceListener = new ICodeServiceListener() {

		private boolean isResponsible(List<ILocatable> locatables) {
			for (ILocatable locatable : locatables) {
				if (locatable.getUri().getHost().equals("diff")) {
					return true;
				}
			}
			return false;
		}

		@Override
		public void codesAdded(List<ICode> code) {
		}

		@Override
		public void codesAssigned(List<ICode> code, List<ILocatable> locatables) {
			if (this.isResponsible(locatables)) {
				com.bkahlert.devel.nebula.utils.ViewerUtils
						.refresh(DiffContentProvider.this.viewer);
			}
		}

		@Override
		public void codeRenamed(ICode code, String oldCaption, String newCaption) {
		}

		@Override
		public void codeRecolored(ICode code, RGB oldColor, RGB newColor) {
			com.bkahlert.devel.nebula.utils.ViewerUtils
					.refresh(DiffContentProvider.this.viewer);
		}

		@Override
		public void codesRemoved(List<ICode> codes, List<ILocatable> locatables) {
			if (this.isResponsible(locatables)) {
				com.bkahlert.devel.nebula.utils.ViewerUtils
						.refresh(DiffContentProvider.this.viewer);
			}
		}

		@Override
		public void codeMoved(ICode code, ICode oldParentCode,
				ICode newParentCode) {
		}

		@Override
		public void codeDeleted(ICode code) {
			com.bkahlert.devel.nebula.utils.ViewerUtils
					.refresh(DiffContentProvider.this.viewer);
		}

		@Override
		public void memoAdded(ICode code) {
		}

		@Override
		public void memoAdded(ILocatable locatable) {
			if (this.isResponsible(new ArrayList<ILocatable>(Arrays
					.asList(locatable)))) {
				com.bkahlert.devel.nebula.utils.ViewerUtils
						.refresh(DiffContentProvider.this.viewer);
			}
		}

		@Override
		public void memoModified(ICode code) {
		}

		@Override
		public void memoModified(ILocatable locatable) {
		}

		@Override
		public void memoRemoved(ICode code) {
		}

		@Override
		public void memoRemoved(ILocatable locatable) {
			if (this.isResponsible(new ArrayList<ILocatable>(Arrays
					.asList(locatable)))) {
				com.bkahlert.devel.nebula.utils.ViewerUtils
						.refresh(DiffContentProvider.this.viewer);
			}
		}

		@Override
		public void episodeAdded(IEpisode episode) {
			com.bkahlert.devel.nebula.utils.ViewerUtils
					.refresh(DiffContentProvider.this.viewer);
		}

		@Override
		public void episodeReplaced(IEpisode oldEpisode, IEpisode newEpisode) {
			com.bkahlert.devel.nebula.utils.ViewerUtils
					.refresh(DiffContentProvider.this.viewer);
		}

		@Override
		public void episodesDeleted(Set<IEpisode> episodes) {
			com.bkahlert.devel.nebula.utils.ViewerUtils
					.refresh(DiffContentProvider.this.viewer);
		}
	};

	private ICompilationService compilationService = (ICompilationService) PlatformUI
			.getWorkbench().getService(ICompilationService.class);
	private ICompilationServiceListener compilationServiceListener = new CompilationServiceAdapter() {
		@Override
		public void compilationStateChanged(ICompilable[] compilables,
				Boolean state) {
			com.bkahlert.devel.nebula.utils.ViewerUtils.update(
					DiffContentProvider.this.viewer, compilables, null);
		}
	};

	public DiffContentProvider() {
		this.codeService.addCodeServiceListener(this.codeServiceListener);
		this.compilationService
				.addCompilationServiceListener(this.compilationServiceListener);
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = viewer;
	}

	@Override
	public void dispose() {
		this.compilationService
				.removeCompilationServiceListener(this.compilationServiceListener);
		this.codeService.removeCodeServiceListener(this.codeServiceListener);
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof IDiff) {
			return null;
		}
		if (element instanceof IDiffRecord) {
			return ((IDiffRecord) element).getDiffFile();
		}
		// TODO return DiffRecordSegments
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
