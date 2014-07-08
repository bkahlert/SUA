package de.fu_berlin.imp.apiua.core.services;

import java.util.Set;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.services.IImportanceService.Importance;

public interface IImportanceServiceListener {
	public void importanceChanged(Set<URI> uris, Importance importance);
}
