package de.fu_berlin.imp.seqan.usability_analyzer.diff.views;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ILabelProvider;

import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.ICompilable;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.ui.ImageManager;

public class CompilerOutputView extends AbstractOutputView {

	public static final String ID = "de.fu_berlin.imp.seqan.usability_analyzer.diff.views.CompilerOutputView";

	private static final Logger LOGGER = Logger
			.getLogger(CompilerOutputView.class);

	@Override
	public PartInfo getDefaultPartInfo() {
		return new PartInfo("Compiler Output", ImageManager.COMPILEROUTPUT_MISC);
	}

	@Override
	public PartInfo getPartInfo(ICompilable compilable) {
		ILabelProvider labelProvider = this.getLabelProviderService()
				.getLabelProvider(compilable);
		if (labelProvider != null) {
			return new PartInfo("Compiler Output - "
					+ labelProvider.getText(compilable),
					labelProvider.getImage(compilable));
		} else {
			LOGGER.warn("No label provider found for " + compilable);
			return this.getDefaultPartInfo();
		}
	}

	@Override
	public String getHtml(ICompilable compilable, IProgressMonitor monitor) {
		return this.getCompilationService().compilerOutput(compilable);
	}

	@Override
	public void setHtml(ICompilable compilable, String html,
			IProgressMonitor monitor) {
		this.getCompilationService().compilerOutput(compilable, html);
	}
}
