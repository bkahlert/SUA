package de.fu_berlin.imp.apiua.groundedtheory.viewer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.Viewer;

import com.bkahlert.nebula.utils.ViewerUtils;
import com.bkahlert.nebula.utils.colors.RGB;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.apiua.core.services.location.URIUtils;
import de.fu_berlin.imp.apiua.core.ui.viewer.URIContentProvider;
import de.fu_berlin.imp.apiua.groundedtheory.LocatorService;
import de.fu_berlin.imp.apiua.groundedtheory.model.Episodes;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.apiua.groundedtheory.model.IEpisodes;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeServiceListener;
import de.fu_berlin.imp.apiua.groundedtheory.storage.ICodeInstance;

public class EpisodeViewerContentProvider extends
		URIContentProvider<ICodeService> {

	private Viewer viewer;
	private ICodeService codeService;
	private final ICodeServiceListener codeServiceListener = new ICodeServiceListener() {

		@Override
		public void codesAdded(List<ICode> codes) {
			ViewerUtils.refresh(EpisodeViewerContentProvider.this.viewer, true);
		}

		@Override
		public void codesAssigned(List<ICode> codes, List<URI> uris) {
			ViewerUtils.refresh(EpisodeViewerContentProvider.this.viewer, true);
		}

		@Override
		public void codeRenamed(ICode code, String oldCaption, String newCaption) {
			ViewerUtils.refresh(EpisodeViewerContentProvider.this.viewer, true);
		}

		@Override
		public void codeRecolored(ICode code, RGB oldColor, RGB newColor) {
			ViewerUtils.refresh(EpisodeViewerContentProvider.this.viewer, true);
		}

		@Override
		public void codesRemoved(List<ICode> removedCodes, List<URI> uris) {
			ViewerUtils.refresh(EpisodeViewerContentProvider.this.viewer, true);
		}

		@Override
		public void codeMoved(ICode code, ICode oldParentCode,
				ICode newParentCode) {
			ViewerUtils.refresh(EpisodeViewerContentProvider.this.viewer, true);
		}

		@Override
		public void codeDeleted(ICode code) {
			ViewerUtils.refresh(EpisodeViewerContentProvider.this.viewer, true);
		}

		@Override
		public void memoAdded(URI uri) {
			ViewerUtils.refresh(EpisodeViewerContentProvider.this.viewer, true);
		}

		@Override
		public void memoModified(URI uri) {
		};

		@Override
		public void memoRemoved(URI uri) {
			ViewerUtils.refresh(EpisodeViewerContentProvider.this.viewer, true);
		}

		@Override
		public void episodeAdded(IEpisode episode) {
			// ViewerUtils.add(viewer, episode.getKey(), episode);
			// TODO: wenn key node fehlt, passiert nichts
			ViewerUtils.refresh(EpisodeViewerContentProvider.this.viewer);
		};

		@Override
		public void episodeReplaced(IEpisode oldEpisode, IEpisode newEpisode) {
			ViewerUtils.remove(EpisodeViewerContentProvider.this.viewer,
					oldEpisode);
			ViewerUtils.add(EpisodeViewerContentProvider.this.viewer,
					newEpisode.getIdentifier(), newEpisode);
		};

		@Override
		public void episodesDeleted(Set<IEpisode> episodes) {
			// TODO: wenn letzte episode bleibt key node
			// for (IEpisode episode : episodes) {
			// ViewerUtils.remove(viewer, episode);
			// }
			ViewerUtils.refresh(EpisodeViewerContentProvider.this.viewer);
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

	/**
	 * Creates a new {@link EpisodeViewerContentProvider} that displays all
	 * {@link ICode}s and optionally {@link ICodeInstance}s.
	 */
	public EpisodeViewerContentProvider() {
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
		List<IIdentifier> identifiers = input.getEpisodedIdentifiers();
		URI[] uris = new URI[identifiers.size()];
		for (int i = 0, m = uris.length; i < m; i++) {
			uris[i] = new Episodes(identifiers.get(i)).getUri();
		}
		return uris;
	}

	@Override
	public URI getParent(URI uri) {
		if (LocatorService.INSTANCE.getType(uri) == IEpisode.class) {
			new Episodes(URIUtils.getIdentifier(uri)).getUri();
		}
		return null;
	}

	@Override
	public boolean hasChildren(URI uri) {
		return LocatorService.INSTANCE.getType(uri) == IEpisodes.class;
	}

	@Override
	public URI[] getChildren(URI uri) {
		if (LocatorService.INSTANCE.getType(uri) == IEpisodes.class) {
			List<IEpisode> episodes = new ArrayList<IEpisode>(
					this.codeService.getEpisodes(URIUtils.getIdentifier(uri)));
			URI[] uris = new URI[episodes.size()];
			for (int i = 0; i < episodes.size(); i++) {
				uris[i] = episodes.get(i).getUri();
			}
			return uris;
		}
		return new URI[0];
	}

}
