package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services;

import java.util.List;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisode;

public interface ICodeServiceListener {
	public void codesAdded(List<ICode> codes);

	public void codesAssigned(List<ICode> codes, List<ICodeable> codeables);

	public void codeRenamed(ICode code, String oldCaption, String newCaption);

	public void codesRemoved(List<ICode> removedCodes, List<ICodeable> codeables);

	public void codeMoved(ICode code, ICode oldParentCode, ICode newParentCode);

	public void codeDeleted(ICode code);

	public void memoModified(ICode code);

	public void memoModified(ICodeable codeable);

	public void episodeAdded(IEpisode episode);

	public void episodeReplaced(IEpisode oldEpisode, IEpisode newEpisode);

	public void episodesDeleted(List<IEpisode> deletedEpisodes);
}
