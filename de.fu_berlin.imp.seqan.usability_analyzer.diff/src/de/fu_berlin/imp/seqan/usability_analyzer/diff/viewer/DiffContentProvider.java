package de.fu_berlin.imp.seqan.usability_analyzer.diff.viewer;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.nebula.colors.RGB;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.ILocatorService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.URIUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.URIContentProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.gt.DiffLocatorProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.ICompilable;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiff;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecord;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffRecords;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.IDiffs;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.services.CompilationServiceAdapter;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.services.ICompilationService;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.services.ICompilationServiceListener;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeServiceListener;

public class DiffContentProvider extends URIContentProvider<Set<IDiffs>> {

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
		public void codesRemoved(List<ICode> codes, List<URI> uris) {
			if (this.isResponsible(uris)) {
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
		public void memoAdded(URI uri) {
			if (this.isResponsible(new ArrayList<URI>(Arrays.asList(uri)))) {
				com.bkahlert.devel.nebula.utils.ViewerUtils
						.refresh(DiffContentProvider.this.viewer);
			}
		}

		@Override
		public void memoModified(URI uri) {
		}

		@Override
		public void memoRemoved(URI uri) {
			if (this.isResponsible(new ArrayList<URI>(Arrays.asList(uri)))) {
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

	private final ICompilationService compilationService = (ICompilationService) PlatformUI
			.getWorkbench().getService(ICompilationService.class);
	private final ICompilationServiceListener compilationServiceListener = new CompilationServiceAdapter() {
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
	public void inputChanged(Viewer viewer, Set<IDiffs> oldInput,
			Set<IDiffs> newInput, Object ignore) {
		this.viewer = viewer;
	}

	@Override
	public void dispose() {
		this.compilationService
				.removeCompilationServiceListener(this.compilationServiceListener);
		this.codeService.removeCodeServiceListener(this.codeServiceListener);
	}

	@Override
	public URI[] getTopLevelElements(Set<IDiffs> input) {
		List<URI> uris = new ArrayList<URI>();

		/*
		 * If the list contains only one element and this element is a list
		 * return the mentioned child list. This way we save one hierarchy level
		 * (= ID level).
		 */
		if (input.size() == 1) {
			IDiff[] diffs = input.toArray(new IDiffs[1])[0].toArray();
			for (int i = 0; i < diffs.length; i++) {
				uris.add(diffs[i].getUri());
			}
		} else {
			for (Iterator<IDiffs> it = input.iterator(); it.hasNext();) {
				IDiffs diffs = it.next();
				uris.add(diffs.getUri());
			}
		}

		return uris.toArray(new URI[uris.size()]);
	}

	@Override
	public URI getParent(URI uri) {
		ILocatable locatable = null;
		try {
			locatable = this.locatorService.resolve(uri, null).get();
		} catch (Exception e) {
			LOGGER.error("Error getting parent of " + uri);
		}

		if (locatable instanceof IDiffs) {
			return null;
		}
		if (locatable instanceof IDiff) {
			return null;
		}
		if (locatable instanceof IDiffRecord) {
			return ((IDiffRecord) locatable).getDiffFile().getUri();
		}
		// TODO return DiffRecordSegments
		return null;
	}

	@Override
	public boolean hasChildren(URI uri) {
		return this.getChildren(uri).length > 0;
	}

	@Override
	public URI[] getChildren(URI parentUri) {
		ILocatable locatable = null;
		try {
			locatable = this.locatorService.resolve(parentUri, null).get();
		} catch (Exception e) {
			LOGGER.error("Error getting children of " + parentUri);
		}

		if (locatable instanceof IDiffs) {
			IDiff[] diffs = ((IDiffs) locatable).toArray();
			URI[] uris = new URI[diffs.length];
			for (int i = 0; i < diffs.length; i++) {
				uris[i] = diffs[i].getUri();
			}
			return uris;
		}

		if (locatable instanceof IDiff) {
			IDiffRecords diffRecords = ((IDiff) locatable).getDiffFileRecords();
			if (diffRecords != null) {
				URI[] uris = new URI[diffRecords.size()];
				for (int i = 0; i < diffRecords.size(); i++) {
					uris[i] = diffRecords.get(i).getUri();
				}
				return uris;
			} else {
				return new URI[0];
			}
		}

		return new URI[0];
	}
}
