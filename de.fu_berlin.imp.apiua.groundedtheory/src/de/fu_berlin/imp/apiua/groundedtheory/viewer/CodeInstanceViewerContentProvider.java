package de.fu_berlin.imp.apiua.groundedtheory.viewer;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.AdapterUtils;
import com.bkahlert.nebula.utils.ViewerUtils;
import com.bkahlert.nebula.utils.colors.RGB;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.ui.viewer.URIContentProvider;
import de.fu_berlin.imp.apiua.groundedtheory.LocatorService;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICodeInstance;
import de.fu_berlin.imp.apiua.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelation;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelationInstance;
import de.fu_berlin.imp.apiua.groundedtheory.model.dimension.IDimension;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeServiceListener;

public class CodeInstanceViewerContentProvider extends
		URIContentProvider<URI[]> {

	public static enum Annotation {
		PARENT_CODE;
	}

	private static final Logger LOGGER = Logger
			.getLogger(CodeInstanceViewerContentProvider.class);

	private Viewer viewer;

	private final ICodeService codeService = (ICodeService) PlatformUI
			.getWorkbench().getService(ICodeService.class);
	private URI[] uris;

	private final ICodeServiceListener codeServiceListener = new ICodeServiceListener() {

		@Override
		public void codesAdded(List<ICode> codes) {
			ViewerUtils.refresh(CodeInstanceViewerContentProvider.this.viewer,
					true);
		}

		@Override
		public void codesAssigned(List<ICode> codes, List<URI> uris) {
			ViewerUtils.refresh(CodeInstanceViewerContentProvider.this.viewer,
					true);
		}

		@Override
		public void codeRenamed(ICode code, String oldCaption, String newCaption) {
			ViewerUtils.refresh(CodeInstanceViewerContentProvider.this.viewer,
					true);
		}

		@Override
		public void codeRecolored(ICode code, RGB oldColor, RGB newColor) {
			ViewerUtils.refresh(CodeInstanceViewerContentProvider.this.viewer,
					true);
		}

		@Override
		public void codesRemoved(List<ICode> codes, List<URI> uris) {
			ViewerUtils.refresh(CodeInstanceViewerContentProvider.this.viewer,
					true);
		}

		@Override
		public void codeMoved(ICode code, ICode oldParentCode,
				ICode newParentCode) {
			ViewerUtils.refresh(CodeInstanceViewerContentProvider.this.viewer,
					true);
		}

		@Override
		public void codeDeleted(ICode code) {
			ViewerUtils.refresh(CodeInstanceViewerContentProvider.this.viewer,
					true);
		}

		@Override
		public void relationsAdded(Set<IRelation> relations) {
			com.bkahlert.nebula.utils.ViewerUtils
					.refresh(CodeInstanceViewerContentProvider.this.viewer);
		}

		@Override
		public void relationsRenamed(Set<IRelation> relations) {
			com.bkahlert.nebula.utils.ViewerUtils
					.refresh(CodeInstanceViewerContentProvider.this.viewer);
		}

		@Override
		public void relationsDeleted(Set<IRelation> relations) {
			com.bkahlert.nebula.utils.ViewerUtils
					.refresh(CodeInstanceViewerContentProvider.this.viewer);
		}

		@Override
		public void relationInstancesAdded(Set<IRelationInstance> relations) {
			com.bkahlert.nebula.utils.ViewerUtils
					.refresh(CodeInstanceViewerContentProvider.this.viewer);
		}

		@Override
		public void relationInstancesDeleted(Set<IRelationInstance> relations) {
			com.bkahlert.nebula.utils.ViewerUtils
					.refresh(CodeInstanceViewerContentProvider.this.viewer);
		}

		@Override
		public void memoAdded(URI uri) {
			ViewerUtils.update(CodeInstanceViewerContentProvider.this.viewer,
					uri, null);
		}

		@Override
		public void memoModified(URI uri) {
		}

		@Override
		public void memoRemoved(URI uri) {
			ViewerUtils.update(CodeInstanceViewerContentProvider.this.viewer,
					uri, null);
		}

		@Override
		public void episodeAdded(IEpisode episode) {
			ViewerUtils.refresh(CodeInstanceViewerContentProvider.this.viewer,
					false);
		}

		@Override
		public void episodeReplaced(IEpisode oldEpisode, IEpisode newEpisode) {
			ViewerUtils.refresh(CodeInstanceViewerContentProvider.this.viewer,
					false);
		}

		@Override
		public void episodesDeleted(Set<IEpisode> episodes) {
			ViewerUtils.refresh(CodeInstanceViewerContentProvider.this.viewer,
					false);
		}

		@Override
		public void dimensionChanged(URI uri, IDimension oldDimension,
				IDimension newDimension) {
			ViewerUtils.refresh(CodeInstanceViewerContentProvider.this.viewer,
					true);
		}

		@Override
		public void dimensionValueChanged(URI uri, String oldValue, String value) {
			ViewerUtils.refresh(CodeInstanceViewerContentProvider.this.viewer,
					true);
		}

		@Override
		public void propertiesChanged(URI uri,
				java.util.List<URI> addedProperties,
				java.util.List<URI> removedProperties) {
			ViewerUtils.refresh(CodeInstanceViewerContentProvider.this.viewer,
					true);
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

	@Override
	public void inputChanged(Viewer viewer, URI[] oldInput, URI[] newInput,
			Object ignore) {
		this.viewer = viewer;

		if (oldInput != null) {
			this.codeService
					.removeCodeServiceListener(this.codeServiceListener);
		}

		if (newInput != null) {
			this.uris = newInput;
			this.codeService.addCodeServiceListener(this.codeServiceListener);
		} else {
			this.uris = null;
		}
	}

	@Override
	public void dispose() {
		if (this.uris != null) {
			this.codeService
					.removeCodeServiceListener(this.codeServiceListener);
		}
	}

	@Override
	public URI[] getTopLevelElements(URI[] uris) throws Exception {
		if (uris.length == 1) {
			return this.getChildren(uris[0]);
		} else {
			return uris;
		}
	}

	@Override
	public URI getParent(URI uri) {
		try {
			ILocatable locatable = LocatorService.INSTANCE.resolve(uri, null)
					.get();
			if (ICodeInstance.class.isInstance(locatable)) {
				return ((ICodeInstance) locatable).getCode().getUri();
			}
		} catch (Exception e) {
			LOGGER.error("Error resolving " + uri, e);
		}
		return null;
	}

	@Override
	public boolean hasChildren(URI uri) throws Exception {
		return this.getChildren(uri).length > 0;
	}

	@Override
	public URI[] getChildren(final URI parent) throws Exception {

		ILocatable locatable = LocatorService.INSTANCE.resolve(parent, null)
				.get();
		if (locatable instanceof ICodeInstance) {
			return this.getChildren(((ICodeInstance) locatable).getId());
		}

		List<URI> children = new LinkedList<URI>();
		if (locatable instanceof ICode) {
			ICode code = (ICode) locatable;

			if (this.codeService.getParent(code) != null) {
				URI parentUri = this.codeService.getParent(code).getUri();
				children.add(new ViewerURI(parentUri).setFlag("parent", true));
			}
		}

		children.addAll(AdapterUtils.adaptAll(
				this.codeService.getCodes(parent), URI.class));

		if (parent != ViewerURI.NO_CODES_URI && !(locatable instanceof ICode)
				&& !(locatable instanceof ICodeInstance)
				&& children.size() == 0) {
			children.add(ViewerURI.NO_CODES_URI);
		}

		return children.toArray(new URI[0]);
	}
}
