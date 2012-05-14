package de.fu_berlin.imp.seqan.usability_analyzer.core.services.impl;

import java.util.Arrays;
import java.util.List;

import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IWorkSession;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IWorkSessionEntity;

public class WorkSession implements IWorkSession {
	public List<IWorkSessionEntity> entities;

	public WorkSession(IWorkSessionEntity entity) {
		this.entities = Arrays.asList(entity);
	}

	public WorkSession(List<IWorkSessionEntity> entities) {
		this.entities = entities;
	}

	@Override
	public List<IWorkSessionEntity> getEntities() {
		return this.entities;
	}

	@Override
	public String toString() {
		return "(" + WorkSession.class.getSimpleName() + ": #"
				+ entities.size() + ")";
	}
}