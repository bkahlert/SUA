package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.timeline;

import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.Viewer;

import com.bkahlert.devel.nebula.colors.RGB;
import com.bkahlert.devel.nebula.utils.ExecutorService.DelayableThread;
import com.bkahlert.devel.nebula.utils.ViewerUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeServiceListener;

/**
 * Instances of this class are responsible to update a timeline viewer so that
 * it always reflects the model correctly.
 * 
 * @author bkahlert
 * 
 */
// FIXME let this be implemented by the contributing providers
public class TimelineRefresher implements ICodeServiceListener {

	private static class DelayedRefresher extends DelayableThread {
		public DelayedRefresher(final Viewer viewer, long delay) {
			super(new Runnable() {
				@Override
				public void run() {
					ViewerUtils.refresh(viewer);
				}
			}, delay);
		}
	}

	private Viewer viewer;
	private long delay;

	private DelayedRefresher delayedRefresher = null;

	/**
	 * Creates an {@link TimelineRefresher} that delays the refresh calls by the
	 * specified delay. This way high frequency refreshments don't affect the
	 * performance negatively.
	 * 
	 * @param viewer
	 * @param delay
	 */
	public TimelineRefresher(Viewer viewer, long delay) {
		Assert.isNotNull(viewer);
		this.viewer = viewer;
		this.delay = delay;
	}

	private void scheduleRefresh() {
		if (this.delayedRefresher == null || this.delayedRefresher.isFinished()) {
			this.delayedRefresher = new DelayedRefresher(this.viewer,
					this.delay);
			this.delayedRefresher.start();
		} else {
			this.delayedRefresher.setDelay(this.delay);
		}
	}

	@Override
	public void codesAdded(List<ICode> codes) {
		this.scheduleRefresh();
	}

	@Override
	public void codesAssigned(List<ICode> codes, List<ILocatable> locatables) {
		this.scheduleRefresh();
	}

	@Override
	public void codeRenamed(ICode code, String oldCaption, String newCaption) {
		this.scheduleRefresh();
	}

	@Override
	public void codeRecolored(ICode code, RGB oldColor, RGB newColor) {
		this.scheduleRefresh();
	}

	@Override
	public void codesRemoved(List<ICode> removedCodes,
			List<ILocatable> locatables) {
		this.scheduleRefresh();
	}

	@Override
	public void codeMoved(ICode code, ICode oldParentCode, ICode newParentCode) {
		this.scheduleRefresh();
	}

	@Override
	public void codeDeleted(ICode code) {
		this.scheduleRefresh();
	}

	@Override
	public void memoAdded(ICode code) {
		this.scheduleRefresh();
	}

	@Override
	public void memoAdded(ILocatable locatable) {
		this.scheduleRefresh();
	}

	@Override
	public void memoModified(ICode code) {
	}

	@Override
	public void memoModified(ILocatable locatable) {
	}

	@Override
	public void memoRemoved(ICode code) {
		this.scheduleRefresh();
	}

	@Override
	public void memoRemoved(ILocatable locatable) {
		this.scheduleRefresh();
	}

	@Override
	public void episodeAdded(IEpisode episode) {
		this.scheduleRefresh();
	}

	@Override
	public void episodeReplaced(IEpisode oldEpisode, IEpisode newEpisode) {
		this.scheduleRefresh();
	}

	@Override
	public void episodesDeleted(Set<IEpisode> deletedEpisodes) {
		this.scheduleRefresh();
	}

}
