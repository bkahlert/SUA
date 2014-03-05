package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.ViewerUtils;
import com.bkahlert.nebula.utils.colors.RGB;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.ILocatorService;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.URIContentProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeServiceListener;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;

public class CodeViewerContentProvider extends URIContentProvider<ICodeService>
		implements IStructuredContentProvider, ITreeContentProvider {

	private static final Logger LOGGER = Logger
			.getLogger(CodeViewerContentProvider.class);

	private Viewer viewer;
	private final ILocatorService locatorService = (ILocatorService) PlatformUI
			.getWorkbench().getService(ILocatorService.class);
	private ICodeService codeService;

	/**
	 * If false no {@link ICodeInstance}s are shown.
	 */
	private final boolean showInstances;

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
		public void codeRecolored(ICode code, RGB oldColor, RGB newColor) {
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
		public void memoAdded(URI uri) {
			ViewerUtils
					.update(CodeViewerContentProvider.this.viewer, uri, null);
		}

		@Override
		public void memoModified(URI uri) {
		}

		@Override
		public void memoRemoved(URI uri) {
			ViewerUtils
					.update(CodeViewerContentProvider.this.viewer, uri, null);
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
	};

	/**
	 * Creates a new {@link CodeViewerContentProvider} that displays all
	 * {@link ICode}s and optionally {@link ICodeInstance}s.
	 * 
	 * @param showInstances
	 *            false if only {@link ICode}s should be displayed
	 */
	public CodeViewerContentProvider(boolean showInstances) {
		this.showInstances = showInstances;
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
			return new URI[] { NoCodesNode.Uri };
		}
	}

	@Override
	public URI getParent(URI uri) {
		try {
			ILocatable locatable = this.locatorService.resolve(uri, null).get();
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
	public boolean hasChildren(URI uri) {
		try {
			ILocatable locatable = this.locatorService.resolve(uri, null).get();
			if (ICode.class.isInstance(locatable)) {
				ICode code = (ICode) locatable;
				if (this.codeService.getChildren(code).size() > 0) {
					return true;
				}
				if (this.showInstances
						&& this.codeService.getInstances(code).size() > 0) {
					return true;
				}
			}
		} catch (Exception e) {
			LOGGER.error("Error resolving " + uri);
		}
		return false;
	}

	@Override
	public URI[] getChildren(URI parentUri) {
		try {
			ILocatable locatable = this.locatorService.resolve(parentUri, null)
					.get();
			if (ICode.class.isInstance(locatable)) {
				ICode code = (ICode) locatable;

				ArrayList<ILocatable> childNodes = new ArrayList<ILocatable>();
				childNodes.addAll(this.codeService.getChildren(code));
				if (this.showInstances) {
					childNodes.addAll(this.codeService.getInstances(code));
				}

				URI[] childUris = new URI[childNodes.size()];
				for (int i = 0; i < childUris.length; i++) {
					childUris[i] = childNodes.get(i).getUri();
				}

				return childUris;
			}
		} catch (Exception e) {
			LOGGER.error("Error resolving " + parentUri);
		}
		return new URI[0];
	}

}
