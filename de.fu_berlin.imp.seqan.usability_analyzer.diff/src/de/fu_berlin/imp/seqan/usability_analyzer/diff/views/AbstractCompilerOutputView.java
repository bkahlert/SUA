package de.fu_berlin.imp.seqan.usability_analyzer.diff.views;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.URI;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.ILabelProviderService.ILabelProvider;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.location.ILocatorService;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.ICompilable;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.ui.ImageManager;

public class AbstractCompilerOutputView extends AbstractOutputView {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(AbstractCompilerOutputView.class);

	private ILocatorService locatorService = (ILocatorService) PlatformUI
			.getWorkbench().getService(ILocatorService.class);

	public AbstractCompilerOutputView(boolean selectionSensitive,
			boolean editorSensitive) {
		super(selectionSensitive, editorSensitive);
	}

	@Override
	public PartInfo getDefaultPartInfo() {
		return new PartInfo("Compiler Output", ImageManager.COMPILEROUTPUT_MISC);
	}

	@Override
	public PartInfo getPartInfo(URI uri) throws Exception {
		ILabelProvider labelProvider = this.getLabelProviderService()
				.getLabelProvider(uri);
		return new PartInfo("Compiler Output - " + labelProvider.getText(uri),
				labelProvider.getImage(uri));
	}

	@Override
	public String getHtml(URI uri, IProgressMonitor monitor) throws Exception {
		ILocatable compilable = this.locatorService.resolve(uri, monitor).get();
		if (compilable instanceof ICompilable) {
			return this.getCompilationService().compilerOutput(
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
			this.getCompilationService().compilerOutput(
					(ICompilable) compilable, html);
		} else {
		}
	}
}
