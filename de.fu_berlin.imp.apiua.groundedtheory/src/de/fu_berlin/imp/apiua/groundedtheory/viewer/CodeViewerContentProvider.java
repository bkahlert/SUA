package de.fu_berlin.imp.apiua.groundedtheory.viewer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.AdapterUtils;
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

public class CodeViewerContentProvider extends URIContentProvider<ICodeService>
		implements IStructuredContentProvider, ITreeContentProvider {

	private static final Logger LOGGER = Logger
			.getLogger(CodeViewerContentProvider.class);

	private Viewer viewer;
	private ICodeService codeService;
	private final IImportanceService importanceService = (IImportanceService) PlatformUI
			.getWorkbench().getService(IImportanceService.class);

	/**
	 * If false no {@link ICodeInstance}s are shown.
	 */
	private boolean showInstances;
	
	/**
	 * If false only a few {@link ICodeInstance}s are shown.
	 */
	private boolean showAllInstances;

	private final ICodeServiceListener codeServiceListener = new ICodeServiceListener() {

		@Override
		public void codesAdded(List<ICode> codes) {
			ViewerUtils.refresh(CodeViewerContentProvider.this.viewer, false);
			for (ICode code : codes) {
				ViewerUtils.expandAll(CodeViewerContentProvider.this.viewer,
						code.getUri());
			}
		}

		@Override
		public void codesAssigned(List<ICode> codes, List<URI> uris) {
			for (ICode code : codes) {
				ViewerUtils.refresh(CodeViewerContentProvider.this.viewer,
						code.getUri(), true);
			}
		}

		@Override
		public void codeRenamed(ICode code, String oldCaption, String newCaption) {
			ViewerUtils.update(CodeViewerContentProvider.this.viewer,
					code.getUri(), null);
		}

		@Override
		public void codesRecolored(List<ICode> codes) {
			ViewerUtils.refresh(CodeViewerContentProvider.this.viewer, true); // TODO
			// check
			// if
			// update
			// is
			// enough
		}

		@Override
		public void codesRemoved(List<ICode> codes, List<URI> uris) {
			ViewerUtils.refresh(CodeViewerContentProvider.this.viewer, true);
		}

		@Override
		public void codeMoved(ICode code, ICode oldParentCode,
				ICode newParentCode) {
			if (oldParentCode == null || newParentCode == null) {
				ViewerUtils.refresh(CodeViewerContentProvider.this.viewer,
						false);
			} else {
				if (oldParentCode != null) {
					ViewerUtils.refresh(CodeViewerContentProvider.this.viewer,
							oldParentCode.getUri(), true);
				}
				if (newParentCode != null) {
					ViewerUtils.refresh(CodeViewerContentProvider.this.viewer,
							newParentCode.getUri(), true);
				}
			}
		}

		@Override
		public void codeDeleted(ICode code) {
			ViewerUtils.remove(CodeViewerContentProvider.this.viewer,
					code.getUri());
		}

		@Override
		public void relationsAdded(Set<IRelation> relations) {
			ViewerUtils.refresh(CodeViewerContentProvider.this.viewer, true);
		}

		@Override
		public void relationsRenamed(Set<IRelation> relations) {
			ViewerUtils.refresh(CodeViewerContentProvider.this.viewer, true);
		}

		@Override
		public void relationsDeleted(Set<IRelation> relations) {
			ViewerUtils.refresh(CodeViewerContentProvider.this.viewer, true);
		}

		@Override
		public void relationInstancesAdded(Set<IRelationInstance> relations) {
			ViewerUtils.refresh(CodeViewerContentProvider.this.viewer, true);
		}

		@Override
		public void relationInstancesDeleted(Set<IRelationInstance> relations) {
			ViewerUtils.refresh(CodeViewerContentProvider.this.viewer, true);
		}

		@Override
		public void memoAdded(URI uri) {
			ViewerUtils.refresh(CodeViewerContentProvider.this.viewer, true);
		}

		@Override
		public void memoModified(URI uri) {
		}

		@Override
		public void memoRemoved(URI uri) {
			ViewerUtils.refresh(CodeViewerContentProvider.this.viewer, true);
		}

		@Override
		public void episodeAdded(IEpisode episode) {
		}

		@Override
		public void episodeReplaced(IEpisode oldEpisode, IEpisode newEpisode) {
			ViewerUtils.refresh(CodeViewerContentProvider.this.viewer, true);
		}

		@Override
		public void episodesDeleted(Set<IEpisode> episodes) {
		}

		@Override
		public void dimensionChanged(URI uri, IDimension oldDimension,
				IDimension newDimension) {
			ViewerUtils.refresh(CodeViewerContentProvider.this.viewer, true);
		}

		@Override
		public void dimensionValueChanged(URI uri, String oldValue, String value) {
			ViewerUtils.refresh(CodeViewerContentProvider.this.viewer, true);
		}

		@Override
		public void propertiesChanged(URI uri,
				java.util.List<URI> addedProperties,
				java.util.List<URI> removedProperties) {
			ViewerUtils.refresh(CodeViewerContentProvider.this.viewer, true);
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
			.update(CodeViewerContentProvider.this.viewer,
					uris.toArray(new URI[0]), null);

	/**
	 * Creates a new {@link CodeViewerContentProvider} that displays all
	 * {@link ICode}s and optionally {@link ICodeInstance}s.
	 *
	 * @param initialShowInstances
	 *            false if only {@link ICode}s should be displayed
	 */
	public CodeViewerContentProvider(boolean initialShowInstances, boolean initialShowAllInstances) {
		this.showInstances = initialShowInstances;
		this.showAllInstances = initialShowAllInstances;
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

		List<ICode> codes = input.getTopLevelCodes();
		if (codes.size() > 0) {
			URI[] uris = new URI[codes.size()];
			for (int i = 0; i < uris.length; i++) {
				uris[i] = codes.get(i).getUri();
			}
			return uris;
		} else {
			return new URI[] { ViewerURI.NO_CODES_URI };
		}
	}

	@Override
	public URI getParent(URI uri) {
		try {
			ILocatable locatable = LocatorService.INSTANCE.resolve(uri, null)
					.get();
			if (ICode.class.isInstance(locatable)) {
				ICode code = (ICode) locatable;
				if (this.codeService != null) {
					ICode parent = this.codeService.getParent(code);
					if (parent != null) {
						return parent.getUri();
					}
				}
				return null;
			}
			if (ICodeInstance.class.isInstance(locatable)) {
				ICodeInstance codeInstance = (ICodeInstance) locatable;
				return codeInstance.getCode().getUri();
			}
		} catch (Exception e) {
			LOGGER.error("Error resolving " + uri);
		}
		return null;
	}

	@Override
	public boolean hasChildren(URI uri) throws Exception {
		return this.getChildren(uri).length > 0;
	}

	@Override
	public URI[] getChildren(URI parentUri) throws Exception {
		ILocatable locatable = LocatorService.INSTANCE.resolve(parentUri, null)
				.get();
		if (ICode.class.isInstance(locatable)) {
			ICode code = (ICode) locatable;

			ArrayList<URI> childNodes = new ArrayList<URI>();
			childNodes.addAll(AdapterUtils.adaptAll(
					this.codeService.getChildren(code), URI.class));
			if (this.showInstances) {
				Set<ICodeInstance> instances = this.codeService
						.getExplicitInstances(code);
				if (instances.size() > 0) {				
					if (!this.showAllInstances && instances.size() > 3) {
						ArrayList<ICodeInstance> instances_ = new ArrayList<>(instances);
						childNodes.add(instances_.get(0).getUri());
						childNodes.add(ViewerURI.NOT_ALL_INSTANCES_URI);
						childNodes.add(instances_.get(instances_.size()-1).getUri());
					} else {
						childNodes.addAll(AdapterUtils.adaptAll(instances,
								URI.class));
					}
				} else {
					childNodes.add(ViewerURI.NO_PHENOMENONS_URI);
				}
			}

			return childNodes.toArray(new URI[0]);
		}
		return new URI[0];
	}

	public void setShowInstances(boolean showInstances) {
		this.showInstances = showInstances;
		ViewerUtils.refresh(CodeViewerContentProvider.this.viewer, true);
	}
	
	public void setShowAllInstances(boolean showAllInstances) {
		this.showAllInstances = showAllInstances;
		ViewerUtils.refresh(CodeViewerContentProvider.this.viewer, true);
	}
}
