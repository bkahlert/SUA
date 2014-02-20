package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services;

import java.net.URI;
import java.util.List;
import java.util.Set;

import com.bkahlert.nebula.utils.colors.RGB;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.IEpisode;

// TODO rewrite everything to URI
public interface ICodeServiceListener {
	public void codesAdded(List<ICode> codes);

	public void codesAssigned(List<ICode> codes, List<URI> uris);

	public void codeRenamed(ICode code, String oldCaption, String newCaption);

	public void codeRecolored(ICode code, RGB oldColor, RGB newColor);

	public void codesRemoved(List<ICode> removedCodes, List<URI> uris);

	public void codeMoved(ICode code, ICode oldParentCode, ICode newParentCode);

	public void codeDeleted(ICode code);

	public void memoAdded(URI uri);

	public void memoModified(URI uri);

	public void memoRemoved(URI uri);

	public void episodeAdded(IEpisode episode);

	public void episodeReplaced(IEpisode oldEpisode, IEpisode newEpisode);

	public void episodesDeleted(Set<IEpisode> deletedEpisodes);
}
