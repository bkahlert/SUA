package de.fu_berlin.imp.apiua.uri.viewers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.ui.viewer.URIContentProvider;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelation;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelationInstance;
import de.fu_berlin.imp.apiua.groundedtheory.model.dimension.IDimension;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeServiceListener;
import de.fu_berlin.imp.apiua.uri.model.IUri;
import de.fu_berlin.imp.apiua.uri.services.IUriService;
import de.fu_berlin.imp.apiua.uri.services.IUriServiceListener;

public class UriContentProvider extends URIContentProvider<IUriService> {

	private Viewer viewer;

	private final ICodeService codeService = (ICodeService) PlatformUI
			.getWorkbench().getService(ICodeService.class);
	private final ICodeServiceListener codeServiceListener = new ICodeServiceListener() {

		@Override
		public void codesAdded(List<ICode> code) {
		}

		@Override
		public void codesAssigned(List<ICode> code, List<URI> uris) {
			com.bkahlert.nebula.utils.ViewerUtils
			.refresh(UriContentProvider.this.viewer);
		}

		@Override
		public void codeRenamed(ICode code, String oldCaption, String newCaption) {
		}

		@Override
		public void codeRecolored(ICode code,
				com.bkahlert.nebula.utils.colors.RGB oldColor,
				com.bkahlert.nebula.utils.colors.RGB newColor) {
		}

		@Override
		public void codesRemoved(List<ICode> codes, List<URI> uris) {
			com.bkahlert.nebula.utils.ViewerUtils
			.refresh(UriContentProvider.this.viewer);
		}

		@Override
		public void codeMoved(ICode code, ICode oldParentCode,
				ICode newParentCode) {
		}

		@Override
		public void codeDeleted(ICode code) {
			com.bkahlert.nebula.utils.ViewerUtils
			.refresh(UriContentProvider.this.viewer);
		}

		@Override
		public void relationsAdded(Set<IRelation> relations) {
			com.bkahlert.nebula.utils.ViewerUtils
					.refresh(UriContentProvider.this.viewer);
		}

		@Override
		public void relationsRenamed(Set<IRelation> relations) {
			com.bkahlert.nebula.utils.ViewerUtils
					.refresh(UriContentProvider.this.viewer);
		}

		@Override
		public void relationsDeleted(Set<IRelation> relations) {
			com.bkahlert.nebula.utils.ViewerUtils
					.refresh(UriContentProvider.this.viewer);
		}

		@Override
		public void relationInstancesAdded(Set<IRelationInstance> relations) {
			com.bkahlert.nebula.utils.ViewerUtils
					.refresh(UriContentProvider.this.viewer);
		}

		@Override
		public void relationInstancesDeleted(Set<IRelationInstance> relations) {
			com.bkahlert.nebula.utils.ViewerUtils
					.refresh(UriContentProvider.this.viewer);
		}

		@Override
		public void memoAdded(URI uri) {
			com.bkahlert.nebula.utils.ViewerUtils
			.refresh(UriContentProvider.this.viewer);
		}

		@Override
		public void memoModified(URI uri) {
		}

		@Override
		public void memoRemoved(URI uri) {
			com.bkahlert.nebula.utils.ViewerUtils
			.refresh(UriContentProvider.this.viewer);
		}

		@Override
		public void episodeAdded(IEpisode episode) {
			com.bkahlert.nebula.utils.ViewerUtils
			.refresh(UriContentProvider.this.viewer);
		}

		@Override
		public void episodeReplaced(IEpisode oldEpisode, IEpisode newEpisode) {
			com.bkahlert.nebula.utils.ViewerUtils
			.refresh(UriContentProvider.this.viewer);
		}

		@Override
		public void episodesDeleted(Set<IEpisode> episodes) {
			com.bkahlert.nebula.utils.ViewerUtils
			.refresh(UriContentProvider.this.viewer);
		}

		@Override
		public void dimensionChanged(URI uri, IDimension oldDimension,
				IDimension newDimension) {
			com.bkahlert.nebula.utils.ViewerUtils
			.refresh(UriContentProvider.this.viewer);
		}

		@Override
		public void dimensionValueChanged(URI uri, String oldValue, String value) {
			com.bkahlert.nebula.utils.ViewerUtils
			.refresh(UriContentProvider.this.viewer);
		}

		@Override
		public void propertiesChanged(URI uri, List<URI> addedProperties,
				List<URI> removedProperties) {
			com.bkahlert.nebula.utils.ViewerUtils
			.refresh(UriContentProvider.this.viewer);
		}

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

	private final IUriService uriService = (IUriService) PlatformUI
			.getWorkbench().getService(IUriService.class);
	private final IUriServiceListener uriServiceListener = new IUriServiceListener() {

		@Override
		public void urisAdded(Set<IUri> uris) {
			com.bkahlert.nebula.utils.ViewerUtils
			.refresh(UriContentProvider.this.viewer);
		}

		@Override
		public void uriReplaced(IUri oldUri, IUri newUri) {
			com.bkahlert.nebula.utils.ViewerUtils
			.refresh(UriContentProvider.this.viewer);
		}

		@Override
		public void urisRemoved(Set<IUri> uris) {
			com.bkahlert.nebula.utils.ViewerUtils
			.refresh(UriContentProvider.this.viewer);
		}

	};

	public UriContentProvider() {
		this.codeService.addCodeServiceListener(this.codeServiceListener);
		this.uriService.addUriServiceListener(this.uriServiceListener);
	}

	@Override
	public void inputChanged(Viewer viewer, IUriService oldInput,
			IUriService newInput, Object ignore) {
		this.viewer = viewer;
	}

	@Override
	public void dispose() {
		this.uriService.removeUriServiceListener(this.uriServiceListener);
		this.codeService.removeCodeServiceListener(this.codeServiceListener);
	}

	@Override
	public URI[] getTopLevelElements(IUriService input) {
		if (input != null) {
			IUriService uriService = input;
			List<IUri> uris = new ArrayList<IUri>(uriService.getUris());
			URI[] uris_ = new URI[uris.size()];
			for (int i = 0; i < uris_.length; i++) {
				uris_[i] = uris.get(i).getUri();
			}
			return uris_;
		} else {
			return new URI[0];
		}
	}

	@Override
	public URI getParent(URI uri) {
		return null;
	}

	@Override
	public boolean hasChildren(URI element) {
		return false;
	}

	@Override
	public URI[] getChildren(URI parentElement) {
		return new URI[0];
	}

}
