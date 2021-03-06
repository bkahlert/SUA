package de.fu_berlin.imp.apiua.entity.viewer;

import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.ViewerUtils;
import com.bkahlert.nebula.utils.colors.RGB;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.ui.viewer.URIContentProvider;
import de.fu_berlin.imp.apiua.entity.model.Entity;
import de.fu_berlin.imp.apiua.entity.model.EntityDataContainer;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelation;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelationInstance;
import de.fu_berlin.imp.apiua.groundedtheory.model.dimension.IDimension;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeServiceListener;

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
		public void relationsAdded(Set<IRelation> relations) {
			ViewerUtils.refresh(EntityContentProvider.this.viewer);
		}

		@Override
		public void relationsRenamed(Set<IRelation> relations) {
			ViewerUtils.refresh(EntityContentProvider.this.viewer);
		}

		@Override
		public void relationsDeleted(Set<IRelation> relations) {
			ViewerUtils.refresh(EntityContentProvider.this.viewer);
		}

		@Override
		public void relationInstancesAdded(Set<IRelationInstance> relations) {
			ViewerUtils.refresh(EntityContentProvider.this.viewer);
		}

		@Override
		public void relationInstancesDeleted(Set<IRelationInstance> relations) {
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

		@Override
		public void dimensionChanged(URI uri, IDimension oldDimension,
				IDimension newDimension) {
			ViewerUtils.refresh(EntityContentProvider.this.viewer);
		}

		@Override
		public void dimensionValueChanged(URI uri, String oldValue, String value) {
			ViewerUtils.refresh(EntityContentProvider.this.viewer);
		}

		@Override
		public void propertiesChanged(URI uri,
				java.util.List<URI> addedProperties,
				java.util.List<URI> removedProperties) {
			ViewerUtils.refresh(EntityContentProvider.this.viewer);
		};

		@Override
		public void axialCodingModelAdded(URI uri) {
		}

		@Override
		public void axialCodingModelUpdated(URI uri) {
		}

		@Override
		public void axialCodingModelRemoved(URI uri) {
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
		List<Entity> entities = input.getEntityManager().getPersons();
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
