package de.fu_berlin.imp.seqan.usability_analyzer;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

	public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
	}

	@Override
	protected void makeActions(IWorkbenchWindow window) {
	}

	@Override
	protected void fillMenuBar(IMenuManager menuBar) {

	}

	@Override
	protected void fillStatusLine(IStatusLineManager statusLine) {
		super.fillStatusLine(statusLine);
	}
}
