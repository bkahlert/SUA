package de.fu_berlin.imp.apiua.diff.views;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.services.location.ILocatorService;
import de.fu_berlin.imp.apiua.diff.model.ICompilable;

public abstract class AbstractExecutionOutputView extends AbstractOutputView {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(AbstractExecutionOutputView.class);

	private final ILocatorService locatorService = (ILocatorService) PlatformUI
			.getWorkbench().getService(ILocatorService.class);

	public AbstractExecutionOutputView(boolean selectionSensitive,
			boolean editorSensitive) {
		super(selectionSensitive, editorSensitive);
	}

	@Override
	public String getHtml(URI uri, IProgressMonitor monitor) throws Exception {
		ILocatable compilable = this.locatorService.resolve(uri, monitor).get();
		if (compilable instanceof ICompilable) {
			return this.getCompilationService().executionOutput(
					(ICompilable) compilable);
		} else {
			return "";
		}
	}

	@Override
	public void setHtml(URI uri, String html, IProgressMonitor monitor)
			throws Exception {
		ILocatable compilable = this.locatorService.resolve(uri, monitor).get();
		if (compilable instanceof ICompilable) {
			this.getCompilationService().executionOutput(
					(ICompilable) compilable);
		} else {

		}
	}
}
