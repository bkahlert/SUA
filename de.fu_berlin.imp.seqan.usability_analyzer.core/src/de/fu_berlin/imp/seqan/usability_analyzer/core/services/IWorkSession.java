package de.fu_berlin.imp.seqan.usability_analyzer.core.services;

import java.io.Serializable;

public interface IWorkSession extends Serializable {
	/**
	 * Returns the {@link IWorkSessionEntity} the {@link IWorkSession} is
	 * focused on.
	 * 
	 * @return
	 */
	public IWorkSessionEntity[] getEntities();
}
