package de.fu_berlin.imp.seqan.usability_analyzer.doclog.viewer;

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
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.Doclog;
import de.fu_berlin.imp.seqan.usability_analyzer.doclog.model.DoclogRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeServiceListener;

public class DoclogContentProvider implements IStructuredContentProvider,
		ITreeContentProvider {

	private Viewer viewer;
	private ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
			.getService(ICodeService.class);
	private ICodeServiceListener codeServiceListener = new ICodeServiceListener() {

		private boolean isResponsible(List<ILocatable> codeables) {
			for (ILocatable codeable : codeables) {
				if (codeable.getUri().getHost().equals("doclog")) {
					return true;
				}
			}
			return false;
		}

		@Override
		public void codesAdded(List<ICode> code) {
		}

		@Override
		public void codesAssigned(List<ICode> codes, List<ILocatable> codeables) {
			if (this.isResponsible(codeables)) {
				com.bkahlert.devel.nebula.utils.ViewerUtils
						.refresh(DoclogContentProvider.this.viewer);
			}
		}

		@Override
		public void codeRenamed(ICode code, String oldCaption, String newCaption) {
		}

		@Override
		public void codeRecolored(ICode code, RGB oldColor, RGB newColor) {
			com.bkahlert.devel.nebula.utils.ViewerUtils
					.refresh(DoclogContentProvider.this.viewer);
		}

		@Override
		public void codesRemoved(List<ICode> codes, List<ILocatable> codeables) {
			if (this.isResponsible(codeables)) {
				com.bkahlert.devel.nebula.utils.ViewerUtils
						.refresh(DoclogContentProvider.this.viewer);
			}
		}

		@Override
		public void codeMoved(ICode code, ICode oldParentCode,
				ICode newParentCode) {
		}

		@Override
		public void codeDeleted(ICode code) {
			com.bkahlert.devel.nebula.utils.ViewerUtils
					.refresh(DoclogContentProvider.this.viewer);
		}

		@Override
		public void memoAdded(ICode code) {
		}

		@Override
		public void memoAdded(ILocatable codeable) {
			if (this.isResponsible(new ArrayList<ILocatable>(Arrays
					.asList(codeable)))) {
				com.bkahlert.devel.nebula.utils.ViewerUtils
						.refresh(DoclogContentProvider.this.viewer);
			}
		}

		@Override
		public void memoModified(ICode code) {
		}

		@Override
		public void memoModified(ILocatable codeable) {
		}

		@Override
		public void memoRemoved(ICode code) {
		}

		@Override
		public void memoRemoved(ILocatable codeable) {
			if (this.isResponsible(new ArrayList<ILocatable>(Arrays
					.asList(codeable)))) {
				com.bkahlert.devel.nebula.utils.ViewerUtils
						.refresh(DoclogContentProvider.this.viewer);
			}
		}

		@Override
		public void episodeAdded(IEpisode episode) {
			com.bkahlert.devel.nebula.utils.ViewerUtils
					.refresh(DoclogContentProvider.this.viewer);
		}

		@Override
		public void episodeReplaced(IEpisode oldEpisode, IEpisode newEpisode) {
			com.bkahlert.devel.nebula.utils.ViewerUtils
					.refresh(DoclogContentProvider.this.viewer);
		}

		@Override
		public void episodesDeleted(Set<IEpisode> episodes) {
			com.bkahlert.devel.nebula.utils.ViewerUtils
					.refresh(DoclogContentProvider.this.viewer);
		}
	};

	public DoclogContentProvider() {
		this.codeService.addCodeServiceListener(this.codeServiceListener);
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = viewer;
	}

	@Override
	public void dispose() {
		this.codeService.removeCodeServiceListener(this.codeServiceListener);
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
