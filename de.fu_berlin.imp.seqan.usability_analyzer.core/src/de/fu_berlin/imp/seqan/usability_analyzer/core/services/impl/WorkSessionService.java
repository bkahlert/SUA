package de.fu_berlin.imp.seqan.usability_analyzer.core.services.impl;

import java.io.IOException;

import org.apache.log4j.Logger;

import de.fu_berlin.imp.seqan.usability_analyzer.core.preferences.SUACorePreferenceUtil;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IWorkSession;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IWorkSessionEntity;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IWorkSessionListener;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IWorkSessionService;

class WorkSessionService implements IWorkSessionService {

	private static final Logger LOGGER = Logger
			.getLogger(WorkSessionService.class);

	private WorkSessionListenerNotifier workSessionListenerNotifier = new WorkSessionListenerNotifier();

	private IWorkSession currentWorkSession = null;

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
		this.startWorkSession(new IWorkSessionEntity[] { entity });
	}

	@Override
	public void startWorkSession(final IWorkSessionEntity[] entities) {
		this.startWorkSession(new WorkSession(entities));
	}

	private void startWorkSession(final IWorkSession workSession) {
		this.currentWorkSession = workSession;
		LOGGER.info(IWorkSession.class.getSimpleName() + " started: "
				+ this.currentWorkSession);
		this.workSessionListenerNotifier
				.workSessionStarted(this.currentWorkSession);
		new SUACorePreferenceUtil().setLastWorkSession(this.currentWorkSession);
	}

	@Override
	public IWorkSession getCurrentWorkSession() {
		return this.currentWorkSession;
	}

	@Override
	public void restoreLastWorkSession() {
		IWorkSession lastWorkSession = new SUACorePreferenceUtil()
				.getLastWorkSession();
		if (lastWorkSession != null) {
			this.startWorkSession(lastWorkSession);
		} else {
			LOGGER.info("There is no lastly opened "
					+ IWorkSession.class.getSimpleName() + ". Doing nothing.");
		}
	}
}
