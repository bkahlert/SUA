package de.fu_berlin.imp.seqan.usability_analyzer.entity.viewer;

import java.net.URI;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.nebula.colors.RGB;
import com.bkahlert.devel.nebula.utils.ViewerUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.URIContentProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.model.Entity;
import de.fu_berlin.imp.seqan.usability_analyzer.entity.model.EntityDataContainer;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeServiceListener;

public class EntityContentProvider extends
		URIContentProvider<EntityDataContainer> {

	private Viewer viewer;

	private final ICodeService codeService = (ICodeService) PlatformUI
			.getWorkbench().getService(ICodeService.class);

	private final ICodeServiceListener codeServiceListener = new ICodeServiceListener() {

		@Override
		public void codesAdded(List<ICode> code) {
			ViewerUtils.refresh(EntityContentProvider.this.viewer);
		}

		@Override
		public void codesAssigned(List<ICode> code, List<URI> uris) {
			ViewerUtils.refresh(EntityContentProvider.this.viewer);
		}

		@Override
		public void codeRenamed(ICode code, String oldCaption, String newCaption) {
			ViewerUtils.refresh(EntityContentProvider.this.viewer);
		}

		@Override
		public void codeRecolored(ICode code, RGB oldColor, RGB newColor) {
			ViewerUtils.refresh(EntityContentProvider.this.viewer);
		}

		@Override
		public void codesRemoved(List<ICode> codes, List<URI> uris) {
			ViewerUtils.refresh(EntityContentProvider.this.viewer);
		}

		@Override
		public void codeMoved(ICode code, ICode oldParentCode,
				ICode newParentCode) {
			ViewerUtils.refresh(EntityContentProvider.this.viewer);
		}

		@Override
		public void codeDeleted(ICode code) {
			ViewerUtils.refresh(EntityContentProvider.this.viewer);
		}

		@Override
		public void memoAdded(URI uri) {
			ViewerUtils.refresh(EntityContentProvider.this.viewer);
		}

		@Override
		public void memoModified(URI uri) {
		}

		@Override
		public void memoRemoved(URI uri) {
			ViewerUtils.refresh(EntityContentProvider.this.viewer);
		}

		@Override
		public void episodeAdded(IEpisode episode) {
			ViewerUtils.refresh(EntityContentProvider.this.viewer);
		}

		@Override
		public void episodeReplaced(IEpisode oldEpisode, IEpisode newEpisode) {
			ViewerUtils.refresh(EntityContentProvider.this.viewer);
		}

		@Override
		public void episodesDeleted(Set<IEpisode> episodes) {
			ViewerUtils.refresh(EntityContentProvider.this.viewer);
		}
	};

	public EntityContentProvider() {
		this.codeService.addCodeServiceListener(this.codeServiceListener);
	}

	@Override
	public void inputChanged(Viewer viewer, EntityDataContainer oldInput,
			EntityDataContainer newInput, Object ignore) {
		this.viewer = viewer;
	}

	@Override
	public void dispose() {
		this.codeService.removeCodeServiceListener(this.codeServiceListener);
	}

	@Override
	public URI[] getTopLevelElements(EntityDataContainer input) {
		List<Entity> entities = ((EntityDataContainer) input)
				.getEntityManager().getPersons();
		URI[] uris = new URI[entities.size()];
		for (int i = 0; i < uris.length; i++) {
			uris[i] = entities.get(i).getUri();
		}
		return uris;
	}

	@Override
	public URI getParent(URI uri) {
		return null;
	}

	@Override
	public boolean hasChildren(URI uri) {
		return false;
	}

	@Override
	public URI[] getChildren(URI parentUri) {
		return new URI[0];
	}
}
