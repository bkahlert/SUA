package de.fu_berlin.imp.apiua.diff.viewer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.ViewerUtils;
import com.bkahlert.nebula.utils.colors.RGB;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.services.location.ILocatorService;
import de.fu_berlin.imp.apiua.core.services.location.URIUtils;
import de.fu_berlin.imp.apiua.core.ui.viewer.URIContentProvider;
import de.fu_berlin.imp.apiua.diff.gt.DiffLocatorProvider;
import de.fu_berlin.imp.apiua.diff.model.ICompilable;
import de.fu_berlin.imp.apiua.diff.model.IDiff;
import de.fu_berlin.imp.apiua.diff.model.IDiffRecord;
import de.fu_berlin.imp.apiua.diff.model.IDiffRecordSegment;
import de.fu_berlin.imp.apiua.diff.model.IDiffRecords;
import de.fu_berlin.imp.apiua.diff.model.IDiffs;
import de.fu_berlin.imp.apiua.diff.services.CompilationServiceAdapter;
import de.fu_berlin.imp.apiua.diff.services.ICompilationService;
import de.fu_berlin.imp.apiua.diff.services.ICompilationServiceListener;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelation;
import de.fu_berlin.imp.apiua.groundedtheory.model.IRelationInstance;
import de.fu_berlin.imp.apiua.groundedtheory.model.dimension.IDimension;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeServiceListener;

public class DiffContentProvider extends URIContentProvider<URI[]> {

	private static final Logger LOGGER = Logger
			.getLogger(DiffContentProvider.class);

	private Viewer viewer;

	private final ILocatorService locatorService = (ILocatorService) PlatformUI
			.getWorkbench().getService(ILocatorService.class);
	private final ICodeService codeService = (ICodeService) PlatformUI
			.getWorkbench().getService(ICodeService.class);
	private final ICodeServiceListener codeServiceListener = new ICodeServiceListener() {

		private boolean isResponsible(List<URI> uris) {
			return URIUtils.filterByResource(uris,
					DiffLocatorProvider.DIFF_NAMESPACE).size() > 0;
		}

		@Override
		public void codesAdded(List<ICode> code) {
		}

		@Override
		public void codesAssigned(List<ICode> code, List<URI> uris) {
			if (this.isResponsible(uris)) {
				com.bkahlert.nebula.utils.ViewerUtils
						.refresh(DiffContentProvider.this.viewer);
			}
		}

		@Override
		public void codeRenamed(ICode code, String oldCaption, String newCaption) {
		}

		@Override
		public void codeRecolored(ICode code, RGB oldColor, RGB newColor) {
			com.bkahlert.nebula.utils.ViewerUtils
					.refresh(DiffContentProvider.this.viewer);
		}

		@Override
		public void codesRemoved(List<ICode> codes, List<URI> uris) {
			if (this.isResponsible(uris)) {
				com.bkahlert.nebula.utils.ViewerUtils
						.refresh(DiffContentProvider.this.viewer);
			}
		}

		@Override
		public void codeMoved(ICode code, ICode oldParentCode,
				ICode newParentCode) {
		}

		@Override
		public void codeDeleted(ICode code) {
			com.bkahlert.nebula.utils.ViewerUtils
					.refresh(DiffContentProvider.this.viewer);
		}

		@Override
		public void relationsAdded(Set<IRelation> relations) {
			ViewerUtils.refresh(DiffContentProvider.this.viewer);
		}

		@Override
		public void relationsRenamed(Set<IRelation> relations) {
			ViewerUtils.refresh(DiffContentProvider.this.viewer);
		}

		@Override
		public void relationsDeleted(Set<IRelation> relations) {
			ViewerUtils.refresh(DiffContentProvider.this.viewer);
		}

		@Override
		public void relationInstancesAdded(Set<IRelationInstance> relations) {
			ViewerUtils.refresh(DiffContentProvider.this.viewer);
		}

		@Override
		public void relationInstancesDeleted(Set<IRelationInstance> relations) {
			ViewerUtils.refresh(DiffContentProvider.this.viewer);
		}

		@Override
		public void memoAdded(URI uri) {
			if (this.isResponsible(new ArrayList<URI>(Arrays.asList(uri)))) {
				com.bkahlert.nebula.utils.ViewerUtils
						.refresh(DiffContentProvider.this.viewer);
			}
		}

		@Override
		public void memoModified(URI uri) {
		}

		@Override
		public void memoRemoved(URI uri) {
			if (this.isResponsible(new ArrayList<URI>(Arrays.asList(uri)))) {
				com.bkahlert.nebula.utils.ViewerUtils
						.refresh(DiffContentProvider.this.viewer);
			}
		}

		@Override
		public void episodeAdded(IEpisode episode) {
			com.bkahlert.nebula.utils.ViewerUtils
					.refresh(DiffContentProvider.this.viewer);
		}

		@Override
		public void episodeReplaced(IEpisode oldEpisode, IEpisode newEpisode) {
			com.bkahlert.nebula.utils.ViewerUtils
					.refresh(DiffContentProvider.this.viewer);
		}

		@Override
		public void episodesDeleted(Set<IEpisode> episodes) {
			com.bkahlert.nebula.utils.ViewerUtils
					.refresh(DiffContentProvider.this.viewer);
		}

		@Override
		public void dimensionChanged(URI uri, IDimension oldDimension,
				IDimension newDimension) {
			com.bkahlert.nebula.utils.ViewerUtils
					.refresh(DiffContentProvider.this.viewer);
		}

		@Override
		public void dimensionValueChanged(URI uri, String oldValue, String value) {
			com.bkahlert.nebula.utils.ViewerUtils
					.refresh(DiffContentProvider.this.viewer);
		}

		@Override
		public void propertiesChanged(URI uri,
				java.util.List<URI> addedProperties,
				java.util.List<URI> removedProperties) {
			com.bkahlert.nebula.utils.ViewerUtils
					.refresh(DiffContentProvider.this.viewer);
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

	private final ICompilationService compilationService = (ICompilationService) PlatformUI
			.getWorkbench().getService(ICompilationService.class);
	private final ICompilationServiceListener compilationServiceListener = new CompilationServiceAdapter() {
		@Override
		public void compilationStateChanged(ICompilable[] compilables,
				Boolean state) {
			com.bkahlert.nebula.utils.ViewerUtils.update(
					DiffContentProvider.this.viewer, compilables, null);
		}
	};

	public DiffContentProvider() {
		this.codeService.addCodeServiceListener(this.codeServiceListener);
		this.compilationService
				.addCompilationServiceListener(this.compilationServiceListener);
	}

	@Override
	public void inputChanged(Viewer viewer, URI[] oldInput, URI[] newInput,
			Object ignore) {
		this.viewer = viewer;
	}

	@Override
	public void dispose() {
		this.compilationService
				.removeCompilationServiceListener(this.compilationServiceListener);
		this.codeService.removeCodeServiceListener(this.codeServiceListener);
	}

	@Override
	public URI[] getTopLevelElements(URI[] uris) {
		if (uris.length == 1
				&& (this.locatorService.getType(uris[0]) == IDiffs.class || this.locatorService
						.getType(uris[0]) == IDiff.class)) {
			return this.getChildren(uris[0]);
		} else {
			return uris;
		}
	}

	@Override
	public URI getParent(URI uri) {
		Class<? extends ILocatable> type = this.locatorService.getType(uri);
		if (type == IDiffs.class) {
			return null;
		} else if (type == IDiff.class) {
			return null;
		} else if (type == IDiffRecord.class) {
			try {
				IDiffRecord diffRecord = this.locatorService.resolve(uri,
						IDiffRecord.class, null).get();
				return diffRecord.getDiffFile().getUri();
			} catch (Exception e) {
				LOGGER.error("Error getting parent of " + uri, e);
			}
		} else if (type == IDiffRecordSegment.class) {
			try {
				IDiffRecordSegment diffRecordSegment = this.locatorService
						.resolve(uri, IDiffRecordSegment.class, null).get();
				return diffRecordSegment.getDiffFileRecord().getUri();
			} catch (Exception e) {
				LOGGER.error("Error getting parent of " + uri, e);
			}
		}
		return null;
	}

	@Override
	public boolean hasChildren(URI uri) {
		Class<? extends ILocatable> type = this.locatorService.getType(uri);
		if (type == IDiffRecord.class || type == IDiffRecordSegment.class) {
			return false;
		}
		return true;
	}

	@Override
	public URI[] getChildren(URI uri) {
		Class<? extends ILocatable> type = this.locatorService.getType(uri);
		if (type == IDiffs.class) {
			try {
				IDiff[] diffs = this.locatorService
						.resolve(uri, IDiffs.class, null).get().toArray();
				URI[] uris = new URI[diffs.length];
				for (int i = 0; i < diffs.length; i++) {
					uris[i] = diffs[i].getUri();
				}
				return uris;
			} catch (Exception e) {
				LOGGER.error("Could not get children for " + uri, e);
			}
		} else if (type == IDiff.class) {
			try {
				IDiffRecords diffRecords = this.locatorService
						.resolve(uri, IDiff.class, null).get()
						.getDiffFileRecords();
				if (diffRecords != null) {
					URI[] uris = new URI[diffRecords.size()];
					for (int i = 0; i < diffRecords.size(); i++) {
						uris[i] = diffRecords.get(i).getUri();
					}
					return uris;
				} else {
					return new URI[0];
				}
			} catch (Exception e) {
				LOGGER.error("Could not get children for " + uri, e);
			}
		} else if (type == IDiffRecord.class) {
		} else if (type == IDiffRecordSegment.class) {
		}
		return new URI[0];
	}

}
