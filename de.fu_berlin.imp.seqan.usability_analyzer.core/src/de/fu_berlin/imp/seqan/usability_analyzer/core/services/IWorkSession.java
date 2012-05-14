package de.fu_berlin.imp.seqan.usability_analyzer.core.services;

import java.util.List;

public interface IWorkSession {
	/**
	 * Returns the {@link IWorkSessionEntity} the {@link IWorkSession} is
	 * focused on.
	 * 
	 * @return
	 */
	public List<IWorkSessionEntity> getEntities();
}
