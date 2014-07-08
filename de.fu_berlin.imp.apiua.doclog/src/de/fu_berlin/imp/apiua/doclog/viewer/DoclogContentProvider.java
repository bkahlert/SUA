package de.fu_berlin.imp.apiua.doclog.viewer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.colors.RGB;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.services.location.ILocatorService;
import de.fu_berlin.imp.apiua.core.services.location.URIUtils;
import de.fu_berlin.imp.apiua.core.ui.viewer.URIContentProvider;
import de.fu_berlin.imp.apiua.doclog.gt.DoclogLocatorProvider;
import de.fu_berlin.imp.apiua.doclog.model.Doclog;
import de.fu_berlin.imp.apiua.doclog.model.DoclogRecord;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeServiceListener;

public class DoclogContentProvider extends URIContentProvider<URI[]> {

	private static final Logger LOGGER = Logger
			.getLogger(DoclogContentProvider.class);

	private Viewer viewer;

	private final ILocatorService locatorService = (ILocatorService) PlatformUI
			.getWorkbench().getService(ILocatorService.class);
	private final ICodeService codeService = (ICodeService) PlatformUI
			.getWorkbench().getService(ICodeService.class);
	private final ICodeServiceListener codeServiceListener = new ICodeServiceListener() {

		private boolean isResponsible(List<URI> uris) {
			return URIUtils.filterByResource(uris,
					DoclogLocatorProvider.DOCLOG_NAMESPACE).size() > 0;
		}

		@Override
		public void codesAdded(List<ICode> code) {
		}

		@Override
		public void codesAssigned(List<ICode> codes, List<URI> uris) {
			if (this.isResponsible(uris)) {
				com.bkahlert.nebula.utils.ViewerUtils
						.refresh(DoclogContentProvider.this.viewer);
			}
		}

		@Override
		public void codeRenamed(ICode code, String oldCaption, String newCaption) {
		}

		@Override
		public void codeRecolored(ICode code, RGB oldColor, RGB newColor) {
			com.bkahlert.nebula.utils.ViewerUtils
					.refresh(DoclogContentProvider.this.viewer);
		}

		@Override
		public void codesRemoved(List<ICode> codes, List<URI> uris) {
			if (this.isResponsible(uris)) {
				com.bkahlert.nebula.utils.ViewerUtils
						.refresh(DoclogContentProvider.this.viewer);
			}
		}

		@Override
		public void codeMoved(ICode code, ICode oldParentCode,
				ICode newParentCode) {
		}

		@Override
		public void codeDeleted(ICode code) {
			com.bkahlert.nebula.utils.ViewerUtils
					.refresh(DoclogContentProvider.this.viewer);
		}

		@Override
		public void memoAdded(URI uri) {
			if (this.isResponsible(new ArrayList<URI>(Arrays.asList(uri)))) {
				com.bkahlert.nebula.utils.ViewerUtils
						.refresh(DoclogContentProvider.this.viewer);
			}
		}

		@Override
		public void memoModified(URI uri) {
		}

		@Override
		public void memoRemoved(URI uri) {
			if (this.isResponsible(new ArrayList<URI>(Arrays.asList(uri)))) {
				com.bkahlert.nebula.utils.ViewerUtils
						.refresh(DoclogContentProvider.this.viewer);
			}
		}

		@Override
		public void episodeAdded(IEpisode episode) {
			com.bkahlert.nebula.utils.ViewerUtils
					.refresh(DoclogContentProvider.this.viewer);
		}

		@Override
		public void episodeReplaced(IEpisode oldEpisode, IEpisode newEpisode) {
			com.bkahlert.nebula.utils.ViewerUtils
					.refresh(DoclogContentProvider.this.viewer);
		}

		@Override
		public void episodesDeleted(Set<IEpisode> episodes) {
			com.bkahlert.nebula.utils.ViewerUtils
					.refresh(DoclogContentProvider.this.viewer);
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

	public DoclogContentProvider() {
		this.codeService.addCodeServiceListener(this.codeServiceListener);
	}

	@Override
	public void inputChanged(Viewer viewer, URI[] oldInput, URI[] newInput,
			Object ignore) {
		this.viewer = viewer;
	}

	@Override
	public void dispose() {
		this.codeService.removeCodeServiceListener(this.codeServiceListener);
	}

	@Override
	public URI[] getTopLevelElements(URI[] uris) {
		if (uris.length == 1
				&& this.locatorService.getType(uris[0]) == Doclog.class) {
			return this.getChildren(uris[0]);
		} else {
			return uris;
		}
	}

	@Override
	public URI getParent(URI uri) {
		if (this.locatorService.getType(uri) == DoclogRecord.class) {
			try {
				DoclogRecord doclogRecord = this.locatorService.resolve(uri,
						DoclogRecord.class, null).get();
				return doclogRecord.getDoclog().getUri();
			} catch (Exception e) {
				LOGGER.error("Error getting parent of " + uri, e);
			}
		}
		return null;
	}

	@Override
	public boolean hasChildren(URI uri) {
		return this.locatorService.getType(uri) == Doclog.class;
	}

	@Override
	public URI[] getChildren(URI uri) {
		if (this.locatorService.getType(uri) == Doclog.class) {
			try {
				Doclog doclog = this.locatorService.resolve(uri, Doclog.class,
						null).get();
				List<DoclogRecord> doclogRecords = doclog.getDoclogRecords();
				URI[] uris = new URI[doclogRecords.size()];
				for (int i = 0, m = uris.length; i < m; i++) {
					uris[i] = doclogRecords.get(i).getUri();
				}
				return uris;
			} catch (Exception e) {
				LOGGER.error("Error getting children of " + uri, e);
			}
		}
		return new URI[0];
	}
}
