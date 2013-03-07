package de.fu_berlin.imp.seqan.usability_analyzer.diff.views;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.devel.nebula.views.EditorView;
import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;

import de.fu_berlin.imp.seqan.usability_analyzer.diff.model.ICompilable;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.services.CompilationServiceAdapter;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.services.ICompilationService;
import de.fu_berlin.imp.seqan.usability_analyzer.diff.services.ICompilationServiceListener;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceAdapter;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeServiceListener;

public abstract class AbstractOutputView extends EditorView<ICompilable> {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(AbstractOutputView.class);

	private ISelectionListener selectionListener = new ISelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			List<ICompilable> compilables = SelectionUtils.getAdaptableObjects(
					selection, ICompilable.class);
			if (compilables.size() > 0)
				load(compilables.get(0));
		}
	};

	private ICodeService codeService = (ICodeService) PlatformUI.getWorkbench()
			.getService(ICodeService.class);
	private ICodeServiceListener codeServiceListener = new CodeServiceAdapter() {
		@Override
		public void memoRemoved(ICodeable codeable) {
			refreshHeader();
		}

		@Override
		public void memoRemoved(ICode code) {
			refreshHeader();
		}

		@Override
		public void memoAdded(ICodeable codeable) {
			refreshHeader();
		}

		@Override
		public void memoAdded(ICode code) {
			refreshHeader();
		}

		@Override
		public void codesRemoved(List<ICode> removedCodes,
				List<ICodeable> codeables) {
			refreshHeader();
		}

		@Override
		public void codesAssigned(List<ICode> codes, List<ICodeable> codeables) {
			refreshHeader();
		}

		@Override
		public void codeDeleted(ICode code) {
			refreshHeader();
		}
	};
	private ICompilationService compilationService = (ICompilationService) PlatformUI
			.getWorkbench().getService(ICompilationService.class);
	private ICompilationServiceListener compilationServiceListener = new CompilationServiceAdapter() {
		@Override
		public void compilationStateChanged(ICompilable[] compilables,
				Boolean state) {
			refreshHeader();
		}
	};

	public AbstractOutputView() {
		super(2000, true);
	}

	public ICodeService getCodeService() {
		return codeService;
	}

	public ICompilationService getCompilationService() {
		return compilationService;
	}

	@Override
	public void postInit() {
		SelectionUtils.getSelectionService(getSite().getWorkbenchWindow())
				.addPostSelectionListener(selectionListener);
		this.codeService.addCodeServiceListener(codeServiceListener);
		this.compilationService
				.addCompilationServiceListener(compilationServiceListener);
	}

	@Override
	public void dispose() {
		this.compilationService
				.removeCompilationServiceListener(compilationServiceListener);
		this.codeService.removeCodeServiceListener(codeServiceListener);
		SelectionUtils.getSelectionService(getSite().getWorkbenchWindow())
				.removePostSelectionListener(selectionListener);
		super.dispose();
	}

}
