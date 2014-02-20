package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.ViewerUtils;
import com.bkahlert.nebula.utils.colors.RGB;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.ILocatorService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.URIContentProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeServiceListener;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;

public class CodeInstanceViewerContentProvider extends
		URIContentProvider<URI[]> {

	private static final Logger LOGGER = Logger
			.getLogger(CodeInstanceViewerContentProvider.class);

	private Viewer viewer;
	private final ILocatorService locatorService = (ILocatorService) PlatformUI
			.getWorkbench().getService(ILocatorService.class);
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
	public URI[] getTopLevelElements(URI[] uris) {
		if (uris.length == 1) {
			return this.getChildren(uris[0]);
		} else {
			return uris;
		}
	}

	@Override
	public URI getParent(URI uri) {
		try {
			ILocatable locatable = this.locatorService.resolve(uri, null).get();
			if (ICodeInstance.class.isInstance(locatable)) {
				return ((ICodeInstance) locatable).getCode().getUri();
			}
		} catch (Exception e) {
			LOGGER.error("Error resolving " + uri, e);
		}
		return null;
	}

	@Override
	public boolean hasChildren(URI uri) {
		return this.getChildren(uri).length > 0;
	}

	@Override
	public URI[] getChildren(final URI parent) {

		List<URI> uris = new ArrayList<URI>();

		if (!NoCodesNode.Uri.equals(parent)) {

			// add code's parent code
			ILocatable locatable = null;
			try {
				locatable = this.locatorService.resolve(parent, null).get();
			} catch (Exception e) {
				LOGGER.error("Error getting children of " + parent);
				return new URI[0];
			}

			// add code's parent codes
			if (ICode.class.isInstance(locatable)) {
				ICode code = (ICode) locatable;
				ICode parentCode = this.codeService.getParent(code);
				if (parentCode != null) {
					uris.add(parentCode.getUri());
				}
			}

			// add associated codes
			try {
				List<ICode> codes = this.codeService
						.getCodes(ICodeInstance.class.isInstance(locatable) ? ((ICodeInstance) locatable)
								.getId() : parent);
				for (ICode code : codes) {
					uris.add(code.getUri());
				}
			} catch (CodeServiceException e) {
				return new URI[0];
			}

			if (uris.size() == 0) {
				uris.add(NoCodesNode.Uri);
			}
		}

		return uris.toArray(new URI[0]);
	}
}
