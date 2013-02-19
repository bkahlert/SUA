package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer;

import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.bkahlert.devel.nebula.colors.RGB;
import com.bkahlert.devel.nebula.utils.ViewerUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
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
			ViewerUtils.refresh(viewer, true);
		}

		@Override
		public void codesAssigned(List<ICode> codes, List<ICodeable> codeables) {
			ViewerUtils.refresh(viewer, true);
		}

		@Override
		public void codeRenamed(ICode code, String oldCaption, String newCaption) {
			ViewerUtils.refresh(viewer, true);
		}

		@Override
		public void codeRecolored(ICode code, RGB oldColor, RGB newColor) {
			ViewerUtils.refresh(viewer, true);
		}

		@Override
		public void codesRemoved(List<ICode> removedCodes,
				List<ICodeable> codeables) {
			ViewerUtils.refresh(viewer, true);
		}

		@Override
		public void codeMoved(ICode code, ICode oldParentCode,
				ICode newParentCode) {
			ViewerUtils.refresh(viewer, true);
		}

		@Override
		public void codeDeleted(ICode code) {
			ViewerUtils.refresh(viewer, true);
		}

		@Override
		public void memoModified(ICode code) {
			ViewerUtils.update(viewer, code, null);
		}

		@Override
		public void memoModified(ICodeable codeable) {
			ViewerUtils.update(viewer, codeable, null);
		};

		public void episodeAdded(IEpisode episode) {
			// ViewerUtils.add(viewer, episode.getKey(), episode);
			// TODO: wenn key node fehlt, passiert nichts
			ViewerUtils.refresh(viewer);
		};

		public void episodeReplaced(IEpisode oldEpisode, IEpisode newEpisode) {
			ViewerUtils.remove(viewer, oldEpisode);
			ViewerUtils.add(viewer, newEpisode.getKey(), newEpisode);
		};

		public void episodesDeleted(Set<IEpisode> episodes) {
			// TODO: wenn letzte episode bleibt key node
			// for (IEpisode episode : episodes) {
			// ViewerUtils.remove(viewer, episode);
			// }
			ViewerUtils.refresh(viewer);
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
			this.codeService.removeCodeServiceListener(codeServiceListener);
		}
		this.codeService = null;

		if (ICodeService.class.isInstance(newInput)) {
			this.codeService = (ICodeService) newInput;
			this.codeService.addCodeServiceListener(codeServiceListener);
		}
	}

	@Override
	public void dispose() {
		if (this.codeService != null) {
			this.codeService.removeCodeServiceListener(codeServiceListener);
		}
	}

	@Override
	public Object getParent(Object element) {
		if (IEpisode.class.isInstance(element)) {
			return ((IEpisode) element).getKey();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (ID.class.isInstance(element)) {
			return codeService.getEpisodes((ID) element).size() > 0;
		}
		if (Fingerprint.class.isInstance(element)) {
			return codeService.getEpisodes((Fingerprint) element).size() > 0;
		}
		return false;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (ID.class.isInstance(parentElement)) {
			Set<IEpisode> episodes = codeService
					.getEpisodes((ID) parentElement);
			return episodes.size() > 0 ? episodes.toArray() : null;
		}
		if (Fingerprint.class.isInstance(parentElement)) {
			Set<IEpisode> episodes = codeService
					.getEpisodes((Fingerprint) parentElement);
			return episodes.size() > 0 ? episodes.toArray() : null;
		}
		return null;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (!(inputElement instanceof ICodeService))
			return new Object[0];

		List<Object> keys = ((ICodeService) inputElement).getEpisodedKeys();
		return keys.toArray();
	}

}
