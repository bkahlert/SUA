package de.fu_berlin.imp.seqan.usability_analyzer.uri.services;

import java.util.Set;

import de.fu_berlin.imp.seqan.usability_analyzer.uri.model.IUri;

public interface IUriServiceListener {
	public void urisAdded(Set<IUri> uris);

	public void uriReplaced(IUri oldUri, IUri newUri);

	public void urisRemoved(Set<IUri> uris);
}
