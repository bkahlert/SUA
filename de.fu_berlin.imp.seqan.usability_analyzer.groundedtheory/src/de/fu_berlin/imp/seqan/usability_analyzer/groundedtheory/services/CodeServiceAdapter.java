package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services;

import java.util.List;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisode;

public class CodeServiceAdapter implements ICodeServiceListener {

	@Override
	public void codesAdded(List<ICode> codes) {
	}

	@Override
	public void codesAssigned(List<ICode> codes, List<ICodeable> codeables) {
	}

	@Override
	public void codeRenamed(ICode code, String oldCaption, String newCaption) {
	}

	@Override
	public void codesRemoved(List<ICode> codes, List<ICodeable> codeables) {
	}

	@Override
	public void codeMoved(ICode code, ICode oldParentCode, ICode newParentCode) {
	}

	@Override
	public void codeDeleted(ICode code) {
	}

	@Override
	public void memoModified(ICode code) {
	}

	@Override
	public void memoModified(ICodeable codeable) {
	}

	@Override
	public void episodeAdded(IEpisode episode) {
	}

	@Override
	public void episodeReplaced(IEpisode oldEpisode, IEpisode newEpisode) {
	}

	@Override
	public void episodesDeleted(List<IEpisode> episodes) {
	}

}
