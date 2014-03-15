package de.fu_berlin.imp.seqan.usability_analyzer.core.services;

import java.util.Set;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IImportanceService.Importance;

public interface IImportanceServiceListener {
	public void importanceChanged(Set<URI> uris, Importance importance);
}
