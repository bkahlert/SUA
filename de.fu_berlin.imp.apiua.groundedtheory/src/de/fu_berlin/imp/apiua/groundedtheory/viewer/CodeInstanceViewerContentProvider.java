package de.fu_berlin.imp.apiua.groundedtheory.viewer;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.ViewerUtils;

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
		public void codesRecolored(List<ICode> codes) {
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
		public void relationsUpdated(Set<IRelation> relations) {
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
			if (LocatorService.INSTANCE.getType(uri) == ICodeInstance.class) {
				ICodeInstance codeInstance = LocatorService.INSTANCE.resolve(
						uri, ICodeInstance.class, null).get();
				if (codeInstance == null) {
					return null;
				}
				return codeInstance.getCode().getUri();
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
		boolean isCode = false;
		boolean isCodeInstance = false;

		if (LocatorService.INSTANCE.getType(parent) == ICodeInstance.class) {
			isCodeInstance = true;
			ICodeInstance codeInstance = LocatorService.INSTANCE.resolve(
					parent, ICodeInstance.class, null).get();
			if (codeInstance == null) {
				return new URI[0];
			}
			return this.getChildren(codeInstance.getId());
		}

		List<URI> children = new LinkedList<URI>();
		if (LocatorService.INSTANCE.getType(parent) == ICode.class) {
			isCode = true;
			ICode code = LocatorService.INSTANCE.resolve(parent, ICode.class,
					null).get();
			if (code == null) {
				return new URI[0];
			}

			if (this.codeService.getParent(code) != null) {
				URI parentUri = this.codeService.getParent(code).getUri();
				children.add(new ViewerURI(parentUri).setFlag("parent", true));
			}
		}

		if (parent != null && !(parent instanceof ViewerURI)) {
			children.addAll(this.codeService.getExplicitCodes(parent).stream()
					.map(c -> c.getUri()).collect(Collectors.toList()));
		}

		if (parent != ViewerURI.NO_CODES_URI && !isCode && !isCodeInstance
				&& children.size() == 0) {
			children.add(ViewerURI.NO_CODES_URI);
		}

		return children.toArray(new URI[0]);
	}
}
