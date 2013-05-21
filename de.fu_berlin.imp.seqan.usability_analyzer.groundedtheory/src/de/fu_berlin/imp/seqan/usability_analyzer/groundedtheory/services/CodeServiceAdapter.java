package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services;

import java.util.List;
import java.util.Set;

import com.bkahlert.devel.nebula.colors.RGB;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisode;

public class CodeServiceAdapter implements ICodeServiceListener {

	@Override
	public void codesAdded(List<ICode> codes) {
	}

	@Override
	public void codesAssigned(List<ICode> codes, List<ILocatable> codeables) {
	}

	@Override
	public void codeRenamed(ICode code, String oldCaption, String newCaption) {
	}

	@Override
	public void codeRecolored(ICode code, RGB oldColor, RGB newColor) {
	}

	@Override
	public void codesRemoved(List<ICode> codes, List<ILocatable> codeables) {
	}

	@Override
	public void codeMoved(ICode code, ICode oldParentCode, ICode newParentCode) {
	}

	@Override
	public void codeDeleted(ICode code) {
	}

	@Override
	public void memoAdded(ICode code) {
	}

	@Override
	public void memoAdded(ILocatable codeable) {
	}

	@Override
	public void memoModified(ICode code) {
	}

	@Override
	public void memoModified(ILocatable codeable) {
	}

	@Override
	public void memoRemoved(ICode code) {
	}

	@Override
	public void memoRemoved(ILocatable codeable) {
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
