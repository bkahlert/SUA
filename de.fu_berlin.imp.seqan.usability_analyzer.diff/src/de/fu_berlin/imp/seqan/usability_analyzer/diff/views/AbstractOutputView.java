package de.fu_berlin.imp.seqan.usability_analyzer.diff.views;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.nebula.widgets.composer.Composer.ToolbarSet;
import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.core.services.ILabelProviderService;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.ICompilable;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.services.CompilationServiceAdapter;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.services.ICompilationService;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.services.ICompilationServiceListener;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ILocatable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceAdapter;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeServiceListener;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.views.InformationPresentingEditorView;

public abstract class AbstractOutputView extends
		InformationPresentingEditorView<ICompilable> {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(AbstractOutputView.class);

	private ISelectionListener selectionListener = new ISelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			List<ICompilable> compilables = SelectionUtils.getAdaptableObjects(
					selection, ICompilable.class);
			if (compilables.size() > 0) {
				AbstractOutputView.this.load(compilables.get(0));
			}
		}
	};

	private ILabelProviderService labelProviderService = (ILabelProviderService) PlatformUI
			.getWorkbench().getService(ILabelProviderService.class);

	private ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
			.getService(ICodeService.class);
	private ICodeServiceListener codeServiceListener = new CodeServiceAdapter() {
		@Override
		public void memoRemoved(ILocatable locatable) {
			AbstractOutputView.this.refreshHeader();
		}

		@Override
		public void memoRemoved(ICode code) {
			AbstractOutputView.this.refreshHeader();
		}

		@Override
		public void memoAdded(ILocatable locatable) {
			AbstractOutputView.this.refreshHeader();
		}

		@Override
		public void memoAdded(ICode code) {
			AbstractOutputView.this.refreshHeader();
		}

		@Override
		public void codesRemoved(List<ICode> removedCodes,
				List<ILocatable> locatables) {
			AbstractOutputView.this.refreshHeader();
		}

		@Override
		public void codesAssigned(List<ICode> codes, List<ILocatable> locatables) {
			AbstractOutputView.this.refreshHeader();
		}

		@Override
		public void codeDeleted(ICode code) {
			AbstractOutputView.this.refreshHeader();
		}
	};
	private ICompilationService compilationService = (ICompilationService) PlatformUI
			.getWorkbench().getService(ICompilationService.class);
	private ICompilationServiceListener compilationServiceListener = new CompilationServiceAdapter() {
		@Override
		public void compilationStateChanged(ICompilable[] compilables,
				Boolean state) {
			AbstractOutputView.this.refreshHeader();
		}
	};

	public AbstractOutputView() {
		super(2000, ToolbarSet.TERMINAL, true);
	}

	public ILabelProviderService getLabelProviderService() {
		return this.labelProviderService;
	}

	public ICodeService getCodeService() {
		return this.codeService;
	}

	public ICompilationService getCompilationService() {
		return this.compilationService;
	}

	@Override
	public void postInit() {
		super.postInit();
		SelectionUtils.getSelectionService(this.getSite().getWorkbenchWindow())
				.addPostSelectionListener(this.selectionListener);
		this.codeService.addCodeServiceListener(this.codeServiceListener);
		this.compilationService
				.addCompilationServiceListener(this.compilationServiceListener);
	}

	@Override
	public void dispose() {
		this.compilationService
				.removeCompilationServiceListener(this.compilationServiceListener);
		this.codeService.removeCodeServiceListener(this.codeServiceListener);
		SelectionUtils.getSelectionService(this.getSite().getWorkbenchWindow())
				.removePostSelectionListener(this.selectionListener);
		super.dispose();
	}

}
