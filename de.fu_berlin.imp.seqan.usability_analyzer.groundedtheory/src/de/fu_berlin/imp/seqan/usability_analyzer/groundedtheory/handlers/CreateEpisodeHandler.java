package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.handlers;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.nebula.util.ColorUtils;
import com.bkahlert.devel.rcp.selectionUtils.retriever.SelectionRetrieverFactory;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.Fingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.HasFingerprint;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.HasID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.core.ui.viewer.filters.HasDateRange;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.wizards.WizardUtils;

public class CreateEpisodeHandler extends AbstractHandler {

	private static final Logger LOGGER = Logger
			.getLogger(CreateEpisodeHandler.class);

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {

		final List<HasDateRange> objects = SelectionRetrieverFactory
				.getSelectionRetriever(HasDateRange.class).getSelection();

		TimeZoneDateRange range = TimeZoneDateRange
				.calculateOuterDateRange(objects.toArray(new HasDateRange[0]));

		ID id = null;
		if (objects.get(0) instanceof HasID)
			id = ((HasID) objects.get(0)).getID();
		if (id != null) {
			WizardUtils.openAddEpisodeWizard(id, range, getInitialColor(id));
		} else {
			Fingerprint fingerprint = ((HasFingerprint) objects.get(0))
					.getFingerprint();
			if (fingerprint == null) {
				LOGGER.error(IEpisode.class.getSimpleName() + " with no "
						+ ID.class.getSimpleName() + " nor a "
						+ Fingerprint.class.getSimpleName() + " found.");
			}
			WizardUtils.openAddEpisodeWizard(fingerprint, range,
					getInitialColor(fingerprint));
		}

		return null;
	}

	private List<RGB> getRgbs(List<IEpisode> episodes) {
		List<RGB> rgbs = new LinkedList<RGB>();
		for (IEpisode episode : episodes) {
			rgbs.add(episode.getColor());
		}
		return rgbs;
	}

	private RGB getInitialColor(List<IEpisode> episodes) {
		List<RGB> rgbs = getRgbs(episodes);
		return ColorUtils.getBestComplementColor(rgbs);
	}

	private RGB getInitialColor(ID id) {
		ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
				.getService(ICodeService.class);
		List<IEpisode> episodes = codeService.getEpisodes(id);
		return getInitialColor(episodes);
	}

	private RGB getInitialColor(Fingerprint fingerprint) {
		ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
				.getService(ICodeService.class);
		List<IEpisode> episodes = codeService.getEpisodes(fingerprint);
		return getInitialColor(episodes);
	}
}
