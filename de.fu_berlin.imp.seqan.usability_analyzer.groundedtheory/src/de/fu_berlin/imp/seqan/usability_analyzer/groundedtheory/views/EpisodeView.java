package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.HasFingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.HasID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer.EpisodeViewer;
import de.ralfebert.rcputils.menus.ContextMenu;

public class EpisodeView extends ViewPart {

	public static final String ID = "de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.EpisodeView";
	private EpisodeViewer episodeViewer;

	private ISelectionListener selectionListener = new ISelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			List<HasDateRange> ranges = SelectionUtils.getAdaptableObjects(
					selection, HasDateRange.class);

			if (ranges.size() == 0)
				return;

			ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
					.getService(ICodeService.class);
			Map<Object, List<IEpisode>> episodes = new HashMap<Object, List<IEpisode>>();
			List<IEpisode> intersectedEpisodes = new LinkedList<IEpisode>();
			for (HasDateRange range : ranges) {
				if (!(range instanceof HasID)
						&& !(range instanceof HasFingerprint))
					continue;
				List<IEpisode> currentEpisodes;
				if (!episodes.containsKey(range)) {
					if (range instanceof HasID
							&& ((HasID) range).getID() != null)
						currentEpisodes = codeService
								.getEpisodes(((HasID) range).getID());
					else
						currentEpisodes = codeService
								.getEpisodes(((HasFingerprint) range)
										.getFingerprint());
					episodes.put(range, currentEpisodes);
				} else {
					currentEpisodes = episodes.get(range);
				}

				for (IEpisode currentEpisode : currentEpisodes) {
					if (range.getDateRange().isIntersected2(
							currentEpisode.getRange()))
						intersectedEpisodes.add(currentEpisode);
				}
			}

			if (episodeViewer != null)
				episodeViewer.setSelection(new StructuredSelection(
						intersectedEpisodes));
		}
	};

	public EpisodeView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());

		this.episodeViewer = new EpisodeViewer(parent, SWT.NONE);

		new ContextMenu(this.episodeViewer.getViewer(), getSite()) {
			@Override
			protected String getDefaultCommandID() {
				// TODO Auto-generated method stub
				return null;
			}
		};

		SelectionUtils.getSelectionService().addPostSelectionListener(
				selectionListener);
	}

	@Override
	public void dispose() {
		SelectionUtils.getSelectionService().removePostSelectionListener(
				selectionListener);
		super.dispose();
	}

	public EpisodeViewer getEpisodeViewer() {
		return this.episodeViewer;
	}

	@Override
	public void setFocus() {
		if (this.episodeViewer != null && !this.episodeViewer.isDisposed())
			this.episodeViewer.setFocus();
	}

}
