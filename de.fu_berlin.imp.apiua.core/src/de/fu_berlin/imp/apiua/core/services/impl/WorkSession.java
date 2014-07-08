package de.fu_berlin.imp.apiua.core.services.impl;

import de.fu_berlin.imp.apiua.core.services.IWorkSession;
import de.fu_berlin.imp.apiua.core.services.IWorkSessionEntity;

public class WorkSession implements IWorkSession {
	private static final long serialVersionUID = 1L;
	public IWorkSessionEntity[] entities;

	public WorkSession(IWorkSessionEntity[] entities) {
		this.entities = entities;
	}

	@Override
	public IWorkSessionEntity[] getEntities() {
		return this.entities;
	}

	@Override
	public String toString() {
		return "(" + WorkSession.class.getSimpleName() + ": #"
				+ this.entities.length + ")";
	}
}