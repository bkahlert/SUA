package de.fu_berlin.imp.seqan.usability_analyzer.core.services.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IWorkSession;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IWorkSessionEntity;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IWorkSessionListener;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IWorkSessionService;

class WorkSessionService implements IWorkSessionService {

	private static final Logger LOGGER = Logger
			.getLogger(WorkSessionService.class);

	private WorkSessionListenerNotifier workSessionListenerNotifier = new WorkSessionListenerNotifier();

	private WorkSession currentWorkSession = null;

	public WorkSessionService() throws IOException {
		this.workSessionListenerNotifier = new WorkSessionListenerNotifier();
	}

	@Override
	public void addWorkSessionListener(
			IWorkSessionListener sessionServiceListener) {
		this.workSessionListenerNotifier
				.addWorkSessionListener(sessionServiceListener);
	}

	@Override
	public void removeWorkSessionListener(
			IWorkSessionListener sessionServiceListener) {
		this.workSessionListenerNotifier
				.removeWorkSessionListener(sessionServiceListener);
	}

	@Override
	public void startWorkSession(final IWorkSessionEntity entity) {
		this.startWorkSession(Arrays.asList(entity));
	}

	@Override
	public void startWorkSession(final List<IWorkSessionEntity> entities) {
		this.currentWorkSession = new WorkSession(entities);
		LOGGER.info(IWorkSession.class.getSimpleName() + " started: "
				+ this.currentWorkSession);
		this.workSessionListenerNotifier
				.workSessionStarted(this.currentWorkSession);
	}

	@Override
	public IWorkSession getCurrentWorkSession() {
		return this.currentWorkSession;
	}

}
