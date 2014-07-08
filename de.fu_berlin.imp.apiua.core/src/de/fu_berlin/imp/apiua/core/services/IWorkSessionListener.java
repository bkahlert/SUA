package de.fu_berlin.imp.apiua.core.services;

public interface IWorkSessionListener {
	/**
	 * Is called when a new {@link IWorkSession} has started.
	 * 
	 * @param workSession
	 */
	public void workSessionStarted(IWorkSession workSession);
}
