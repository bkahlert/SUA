package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.ExecutorUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.util.WorkbenchUtils;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceException;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeableProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.ImageManager;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.viewer.EpisodeViewer;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.EpisodeView;

public class GTCodeableProvider extends CodeableProvider {

	private static final Logger LOGGER = Logger
			.getLogger(GTCodeableProvider.class);

	public static final String DIFF_NAMESPACE = "episode";

	@Override
	public List<String> getAllowedNamespaces() {
		return Arrays.asList(DIFF_NAMESPACE);
	}

	@Override
	public Callable<ICodeable> getCodedObjectCallable(
			final AtomicReference<IProgressMonitor> monitor,
			final URI codeInstanceID) {
		return new Callable<ICodeable>() {
			@Override
			public ICodeable call() throws Exception {
				ICodeService codeService = (ICodeService) PlatformUI
						.getWorkbench().getService(ICodeService.class);

				String[] path = codeInstanceID.getRawPath().substring(1)
						.split("/");

				// 0: Key
				Set<IEpisode> episodes;
				if (ID.isValid(path[0]))
					episodes = codeService.getEpisodes(new ID(path[0]));
				else
					episodes = codeService
							.getEpisodes(new Fingerprint(path[0]));

				// 1: Compare URI
				for (IEpisode episode : episodes) {
					if (episode.getCodeInstanceID().equals(codeInstanceID))
						return episode;
				}

				return null;
			}
		};
	}

	@Override
	public boolean showCodedObjectsInWorkspace2(
			final List<ICodeable> codedObjects) {
		if (codedObjects.size() > 0) {
			EpisodeView episodeView = (EpisodeView) WorkbenchUtils
					.getView(EpisodeView.ID);
			if (episodeView == null)
				return false;

			final EpisodeViewer viewer = episodeView.getEpisodeViewer();
			try {
				return ExecutorUtil.syncExec(new Callable<Boolean>() {
					@Override
					public Boolean call() {
						viewer.setSelection(new StructuredSelection(
								codedObjects));
						return SelectionUtils.getAdaptableObjects(
								viewer.getSelection(), ICodeable.class).size() == codedObjects
								.size();
					}
				});
			} catch (Exception e) {
				LOGGER.error(e);
			}
		}
		return true;
	}

	@Override
	public ILabelProvider getLabelProvider() {
		return new LabelProvider() {

			ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
					.getService(ICodeService.class);

			@Override
			public String getText(Object element) {
				if (element instanceof IEpisode) {
					IEpisode episode = (IEpisode) element;
					return (episode != null) ? episode.getCaption() : "";
				}
				return "";
			}

			@Override
			public Image getImage(Object element) {
				if (element instanceof IEpisode) {
					IEpisode episode = (IEpisode) element;
					Image overlay;
					try {
						overlay = (codeService.getCodes(episode).size() > 0) ? (codeService
								.isMemo(episode) ? ImageManager.EPISODE_CODED_MEMO
								: ImageManager.EPISODE_CODED)
								: (codeService.isMemo(episode) ? ImageManager.EPISODE_MEMO
										: ImageManager.EPISODE);
					} catch (CodeServiceException e) {
						overlay = ImageManager.EPISODE;
					}
					return overlay;
					// Image image = new Image(Display.getCurrent(),
					// new Rectangle(0, 0, 16, 16));
					// GC gc = new GC(image);
					// PaintUtils
					// .drawRoundedRectangle(
					// gc,
					// overlay.getBounds(),
					// new Color(Display.getDefault(), episode
					// .getColor()));
					// gc.copyArea(overlay, 0, 0);
					// return image;
				}
				return super.getImage(element);
			}
		};
	}
}
