package de.fu_berlin.imp.seqan.usability_analyzer.diff.views;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.ILocatorService;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.ICompilable;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.ui.ImageManager;

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
	public PartInfo getDefaultPartInfo() {
		return new PartInfo("Execution Output",
				ImageManager.COMPILEROUTPUT_MISC);
	}

	@Override
	protected String getPartInfoPrefix() {
		return "Execution Output - ";
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
