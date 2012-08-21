package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer.EpisodeViewer;
import de.ralfebert.rcputils.menus.ContextMenu;

public class EpisodeView extends ViewPart {

	public static final String ID = "de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.EpisodeView";
	private EpisodeViewer episodeViewer;

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
	}

	@Override
	public void dispose() {
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
