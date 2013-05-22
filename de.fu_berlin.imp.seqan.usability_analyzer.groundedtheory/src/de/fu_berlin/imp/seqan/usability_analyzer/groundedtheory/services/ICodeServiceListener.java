package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services;

import java.util.List;
import java.util.Set;

import com.bkahlert.devel.nebula.colors.RGB;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisode;

public interface ICodeServiceListener {
	public void codesAdded(List<ICode> codes);

	public void codesAssigned(List<ICode> codes, List<ILocatable> locatables);

	public void codeRenamed(ICode code, String oldCaption, String newCaption);

	public void codeRecolored(ICode code, RGB oldColor, RGB newColor);

	public void codesRemoved(List<ICode> removedCodes, List<ILocatable> locatables);

	public void codeMoved(ICode code, ICode oldParentCode, ICode newParentCode);

	public void codeDeleted(ICode code);

	public void memoAdded(ICode code);

	public void memoModified(ICode code);

	public void memoRemoved(ICode code);

	public void memoAdded(ILocatable locatable);

	public void memoModified(ILocatable locatable);

	public void memoRemoved(ILocatable locatable);

	public void episodeAdded(IEpisode episode);

	public void episodeReplaced(IEpisode oldEpisode, IEpisode newEpisode);

	public void episodesDeleted(Set<IEpisode> deletedEpisodes);
}
