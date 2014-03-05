package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;
import java.util.List;
import java.util.Set;

import com.bkahlert.nebula.utils.colors.RGB;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisode;

public class CodeServiceAdapter implements ICodeServiceListener {

	@Override
	public void codesAdded(List<ICode> codes) {
	}

	@Override
	public void codesAssigned(List<ICode> codes, List<URI> uris) {
	}

	@Override
	public void codeRenamed(ICode code, String oldCaption, String newCaption) {
	}

	@Override
	public void codeRecolored(ICode code, RGB oldColor, RGB newColor) {
	}

	@Override
	public void codesRemoved(List<ICode> codes, List<URI> uris) {
	}

	@Override
	public void codeMoved(ICode code, ICode oldParentCode, ICode newParentCode) {
	}

	@Override
	public void codeDeleted(ICode code) {
	}

	@Override
	public void memoAdded(URI uri) {
	}

	@Override
	public void memoModified(URI uri) {
	}

	@Override
	public void memoRemoved(URI uri) {
	}

	@Override
	public void episodeAdded(IEpisode episode) {
	}

	@Override
	public void episodeReplaced(IEpisode oldEpisode, IEpisode newEpisode) {
	}

	@Override
	public void episodesDeleted(Set<IEpisode> episodes) {
	}

}
