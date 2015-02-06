package de.fu_berlin.imp.apiua.groundedtheory.viewer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.ViewerUtils;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.services.IImportanceService;
import de.fu_berlin.imp.apiua.core.services.IImportanceServiceListener;
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

public class RelationViewerContentProvider extends
		URIContentProvider<ICodeService> implements IStructuredContentProvider,
		ITreeContentProvider {

	private static final Logger LOGGER = Logger
			.getLogger(RelationViewerContentProvider.class);

	private Viewer viewer;
	private ICodeService codeService;
	private final IImportanceService importanceService = (IImportanceService) PlatformUI
			.getWorkbench().getService(IImportanceService.class);

	/**
	 * If false no {@link ICodeInstance}s are shown.
	 */
	private boolean showInstances;
	private boolean showRelationInstancesToFirst;

	private final ICodeServiceListener codeServiceListener = new ICodeServiceListener() {

		@Override
		public void codesAdded(List<ICode> codes) {
			ViewerUtils
					.refresh(RelationViewerContentProvider.this.viewer, true);
		}

		@Override
		public void codesAssigned(List<ICode> codes, List<URI> uris) {
			ViewerUtils
					.refresh(RelationViewerContentProvider.this.viewer, true);
		}

		@Override
		public void codeRenamed(ICode code, String oldCaption, String newCaption) {
			ViewerUtils
					.refresh(RelationViewerContentProvider.this.viewer, true);
		}

		@Override
		public void codesRecolored(List<ICode> codes) {
			ViewerUtils
					.refresh(RelationViewerContentProvider.this.viewer, true);
		}

		@Override
		public void codesRemoved(List<ICode> codes, List<URI> uris) {
			ViewerUtils
					.refresh(RelationViewerContentProvider.this.viewer, true);
		}

		@Override
		public void codeMoved(ICode code, ICode oldParentCode,
				ICode newParentCode) {
			ViewerUtils
					.refresh(RelationViewerContentProvider.this.viewer, true);
		}

		@Override
		public void codeDeleted(ICode code) {
			ViewerUtils
					.refresh(RelationViewerContentProvider.this.viewer, true);
		}

		@Override
		public void relationsAdded(Set<IRelation> relations) {
			ViewerUtils
					.refresh(RelationViewerContentProvider.this.viewer, true);
		}

		@Override
		public void relationsRenamed(Set<IRelation> relations) {
			ViewerUtils
					.refresh(RelationViewerContentProvider.this.viewer, true);
		}

		@Override
		public void relationsDeleted(Set<IRelation> relations) {
			ViewerUtils
					.refresh(RelationViewerContentProvider.this.viewer, true);
		}

		@Override
		public void relationInstancesAdded(Set<IRelationInstance> relations) {
			ViewerUtils
					.refresh(RelationViewerContentProvider.this.viewer, true);
		}

		@Override
		public void relationInstancesDeleted(Set<IRelationInstance> relations) {
			ViewerUtils
					.refresh(RelationViewerContentProvider.this.viewer, true);
		}

		@Override
		public void memoAdded(URI uri) {
			ViewerUtils
					.refresh(RelationViewerContentProvider.this.viewer, true);
		}

		@Override
		public void memoModified(URI uri) {
		}

		@Override
		public void memoRemoved(URI uri) {
			ViewerUtils
					.refresh(RelationViewerContentProvider.this.viewer, true);
		}

		@Override
		public void episodeAdded(IEpisode episode) {
		}

		@Override
		public void episodeReplaced(IEpisode oldEpisode, IEpisode newEpisode) {
			ViewerUtils
					.refresh(RelationViewerContentProvider.this.viewer, true);
		}

		@Override
		public void episodesDeleted(Set<IEpisode> episodes) {
		}

		@Override
		public void dimensionChanged(URI uri, IDimension oldDimension,
				IDimension newDimension) {
			ViewerUtils
					.refresh(RelationViewerContentProvider.this.viewer, true);
		}

		@Override
		public void dimensionValueChanged(URI uri, String oldValue, String value) {
			ViewerUtils
					.refresh(RelationViewerContentProvider.this.viewer, true);
		}

		@Override
		public void propertiesChanged(URI uri,
				java.util.List<URI> addedProperties,
				java.util.List<URI> removedProperties) {
			ViewerUtils
					.refresh(RelationViewerContentProvider.this.viewer, true);
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

	IImportanceServiceListener importanceServiceListener = (uris, importance) -> ViewerUtils
			.refresh(RelationViewerContentProvider.this.viewer, true);

	/**
	 * Creates a new {@link RelationViewerContentProvider} that displays all
	 * {@link ICode}s and optionally {@link ICodeInstance}s.
	 *
	 * @param initialShowInstances
	 *            false if only {@link ICode}s should be displayed
	 */
	public RelationViewerContentProvider(boolean initialShowInstances) {
		this.showInstances = initialShowInstances;
		this.importanceService
				.addImportanceServiceListener(this.importanceServiceListener);
	}

	@Override
	public void inputChanged(Viewer viewer, ICodeService oldInput,
			ICodeService newInput, Object ignore) {
		this.viewer = viewer;

		if (this.codeService != null) {
			this.codeService
					.removeCodeServiceListener(this.codeServiceListener);
		}
		this.codeService = null;

		if (newInput != null) {
			this.codeService = newInput;
			this.codeService.addCodeServiceListener(this.codeServiceListener);
		} else {
			if (this.codeService != null) {
				this.codeService
						.removeCodeServiceListener(this.codeServiceListener);
				this.codeService = null;
			}
		}
	}

	@Override
	public void dispose() {
		this.importanceService
				.removeImportanceServiceListener(this.importanceServiceListener);
		if (this.codeService != null) {
			this.codeService
					.removeCodeServiceListener(this.codeServiceListener);
		}
	}

	@Override
	public URI[] getTopLevelElements(ICodeService input) {
		if (input == null) {
			return new URI[0];
		}

		Set<IRelation> relations = input.getExplicitRelations();
		if (relations.size() > 0) {
			Set<URI> uris = new HashSet<URI>();
			for (IRelation relation : relations) {
				URI uri = this.showRelationInstancesToFirst ? relation.getTo()
						: relation.getFrom();
				if (!uris.contains(uri)) {
					uris.add(uri);
				}
			}
			return uris.toArray(new URI[0]);
		} else {
			return new URI[] { ViewerURI.NO_RELATIONS_URI };
		}
	}

	@Override
	public URI getParent(URI uri) {
		try {
			ILocatable locatable = LocatorService.INSTANCE.resolve(uri, null)
					.get();
			if (locatable instanceof IRelation) {
				IRelation relation = (IRelation) locatable;
				return this.showRelationInstancesToFirst ? relation.getTo()
						: relation.getFrom();
			} else if (locatable instanceof IRelationInstance) {
				IRelationInstance relationInstance = (IRelationInstance) locatable;
				return relationInstance.getRelation().getUri();
			}
		} catch (InterruptedException | ExecutionException e) {
			LOGGER.error("Error finding parent of " + uri, e);
		}
		return null;
	}

	@Override
	public boolean hasChildren(URI uri) throws Exception {
		if (LocatorService.INSTANCE.getType(uri) == IRelation.class
				&& !this.showInstances) {
			return false;
		}
		return this.getChildren(uri).length > 0;
	}

	@Override
	public URI[] getChildren(URI parentUri) throws Exception {
		ILocatable locatable = LocatorService.INSTANCE.resolve(parentUri, null)
				.get();
		if (!(locatable instanceof IRelation)) {
			Set<IRelation> uris = this.showRelationInstancesToFirst ? this.codeService
					.getExplicitRelationsEndingAt(parentUri) : this.codeService
					.getExplicitRelationsStartingFrom(parentUri);
			return uris.stream().map(r -> r.getUri())
					.collect(Collectors.toList()).toArray(new URI[0]);
		} else {
			IRelation relation = (IRelation) locatable;
			ArrayList<URI> childNodes = new ArrayList<URI>();
			if (this.showInstances) {
				childNodes.addAll(this.codeService
						.getExplicitRelationInstances(relation).stream()
						.map(r -> r.getUri()).collect(Collectors.toList()));
				childNodes.addAll(this.codeService
						.getImplicitRelationInstances(relation).stream()
						.map(r -> r.getUri()).collect(Collectors.toList()));
				if (childNodes.size() == 0) {
					childNodes.add(ViewerURI.NO_PHENOMENONS_URI);
				}
			}

			return childNodes.toArray(new URI[0]);
		}
	}

	public void setShowInstances(boolean showInstances) {
		this.showInstances = showInstances;
		ViewerUtils.refresh(RelationViewerContentProvider.this.viewer, true);
	}

	public void setShowRelationInstancesToFirst(
			boolean showRelationInstancesToFirst) {
		if (this.showRelationInstancesToFirst == showRelationInstancesToFirst) {
			return;
		}
		this.showRelationInstancesToFirst = showRelationInstancesToFirst;

		ExecUtils.logException(ExecUtils.asyncExec(() -> {
			TreePath[] reversedTreePaths = this.getReversedTreePaths();

			ViewerUtils
					.refresh(RelationViewerContentProvider.this.viewer, true);

			if (reversedTreePaths != null) {
				((TreeViewer) this.viewer)
						.setExpandedTreePaths(reversedTreePaths);
			}
		}));
	}

	public boolean getShowRelationInstancesToFirst() {
		return this.showRelationInstancesToFirst;
	}

	private TreePath[] getReversedTreePaths() {
		if (!(this.viewer instanceof TreeViewer)) {
			return null;
		}

		TreeViewer treeViewer = (TreeViewer) this.viewer;
		List<TreePath> reversedTreePaths = new ArrayList<>();
		TreePath[] treePaths = treeViewer.getExpandedTreePaths();
		for (TreePath treePath : treePaths) {
			URI uri = (URI) treePath.getFirstSegment();
			if (this.showRelationInstancesToFirst) {
				for (IRelation relation1 : RelationViewerContentProvider.this.codeService
						.getExplicitRelationsStartingFrom(uri)) {
					Object[] segments1 = ViewerUtils.getSegments(treePath);
					segments1[0] = relation1.getTo();
					reversedTreePaths.add(new TreePath(segments1));
				}
			} else {
				for (IRelation relation2 : RelationViewerContentProvider.this.codeService
						.getAllRelationsEndingAt(uri)) {
					Object[] segments2 = ViewerUtils.getSegments(treePath);
					segments2[0] = relation2.getFrom();
					reversedTreePaths.add(new TreePath(segments2));
				}
			}
		}
		return reversedTreePaths.toArray(new TreePath[0]);
	}
}
