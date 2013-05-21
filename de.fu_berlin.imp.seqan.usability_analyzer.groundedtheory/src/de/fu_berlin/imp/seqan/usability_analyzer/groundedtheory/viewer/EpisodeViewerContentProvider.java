package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer;

import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.bkahlert.devel.nebula.colors.RGB;
import com.bkahlert.devel.nebula.utils.ViewerUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.identifier.IIdentifier;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeServiceListener;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;

public class EpisodeViewerContentProvider implements
		IStructuredContentProvider, ITreeContentProvider {

	private Viewer viewer;
	private ICodeService codeService;
	private ICodeServiceListener codeServiceListener = new ICodeServiceListener() {

		@Override
		public void codesAdded(List<ICode> codes) {
			ViewerUtils.refresh(EpisodeViewerContentProvider.this.viewer, true);
		}

		@Override
		public void codesAssigned(List<ICode> codes, List<ILocatable> codeables) {
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
		public void codesRemoved(List<ICode> removedCodes,
				List<ILocatable> codeables) {
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
		public void memoAdded(ICode code) {
			ViewerUtils.refresh(EpisodeViewerContentProvider.this.viewer, true);
		}

		@Override
		public void memoAdded(ILocatable codeable) {
			ViewerUtils.refresh(EpisodeViewerContentProvider.this.viewer, true);
		}

		@Override
		public void memoModified(ICode code) {
		}

		@Override
		public void memoModified(ILocatable codeable) {
		};

		@Override
		public void memoRemoved(ICode code) {
			ViewerUtils.refresh(EpisodeViewerContentProvider.this.viewer, true);
		}

		@Override
		public void memoRemoved(ILocatable codeable) {
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
	};

	/**
	 * Creates a new {@link EpisodeViewerContentProvider} that displays all
	 * {@link ICode}s and optionally {@link ICodeInstance}s.
	 */
	public EpisodeViewerContentProvider() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = viewer;

		if (this.codeService != null) {
			this.codeService
					.removeCodeServiceListener(this.codeServiceListener);
		}
		this.codeService = null;

		if (ICodeService.class.isInstance(newInput)) {
			this.codeService = (ICodeService) newInput;
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
	public Object getParent(Object element) {
		if (IEpisode.class.isInstance(element)) {
			return ((IEpisode) element).getIdentifier();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (IIdentifier.class.isInstance(element)) {
			return this.codeService.getEpisodes((IIdentifier) element).size() > 0;
		}
		return false;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (IIdentifier.class.isInstance(parentElement)) {
			Set<IEpisode> episodes = this.codeService
					.getEpisodes((IIdentifier) parentElement);
			return episodes.size() > 0 ? episodes.toArray() : null;
		}
		return null;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (!(inputElement instanceof ICodeService)) {
			return new Object[0];
		}

		List<IIdentifier> identifiers = ((ICodeService) inputElement)
				.getEpisodedIdentifiers();
		return identifiers.toArray();
	}

}
