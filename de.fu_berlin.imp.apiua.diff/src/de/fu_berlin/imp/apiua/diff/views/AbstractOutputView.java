package de.fu_berlin.imp.apiua.diff.views;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.utils.selection.SelectionUtils;
import com.bkahlert.nebula.widgets.composer.Composer.ToolbarSet;

import de.fu_berlin.imp.apiua.core.model.URI;
import de.fu_berlin.imp.apiua.core.services.ILabelProviderService;
import de.fu_berlin.imp.apiua.core.views.UriPresentingEditorView;
import de.fu_berlin.imp.apiua.diff.model.ICompilable;
import de.fu_berlin.imp.apiua.diff.services.CompilationServiceAdapter;
import de.fu_berlin.imp.apiua.diff.services.ICompilationService;
import de.fu_berlin.imp.apiua.diff.services.ICompilationServiceListener;
import de.fu_berlin.imp.apiua.groundedtheory.model.ICode;
import de.fu_berlin.imp.apiua.groundedtheory.services.CodeServiceAdapter;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeService;
import de.fu_berlin.imp.apiua.groundedtheory.services.ICodeServiceListener;
import de.fu_berlin.imp.apiua.groundedtheory.ui.UriPartRenamerConverter;
import de.fu_berlin.imp.apiua.groundedtheory.views.EditorOnlyMemoView;

/**
 * TODO document
 * 
 * @author bkahlert
 * 
 */
public abstract class AbstractOutputView extends UriPresentingEditorView {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(AbstractOutputView.class);

	final private boolean selectionSensitive;
	final private boolean editorSensitive;

	private final ISelectionListener selectionListener = new ISelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			List<ICompilable> compilables = SelectionUtils.getAdaptableObjects(
					selection, ICompilable.class);
			if (compilables.size() > 0) {
				AbstractOutputView.this.load(null, compilables.get(0).getUri());
			}
		}
	};

	private final IPartListener partListener = new IPartListener() {

		private ICompilable getCompilable(IWorkbenchPart part) {
			ISelection selection = SelectionUtils.getSelection(part.getSite()
					.getWorkbenchWindow());
			if (selection == null) {
				return null;
			}
			return (ICompilable) Platform.getAdapterManager().getAdapter(
					selection, ICompilable.class);
		}

		@Override
		public void partOpened(IWorkbenchPart part) {
		}

		@Override
		public void partActivated(IWorkbenchPart part) {
			if (part.getClass() == EditorOnlyMemoView.class) {
				return;
			}
			ICompilable compilable = this.getCompilable(part);
			if (compilable != null) {
				AbstractOutputView.this.load(null, compilable.getUri());
			}
		}

		@Override
		public void partBroughtToTop(IWorkbenchPart part) {
		}

		@Override
		public void partDeactivated(IWorkbenchPart part) {
		}

		@Override
		public void partClosed(IWorkbenchPart part) {
		}
	};

	private final ILabelProviderService labelProviderService = (ILabelProviderService) PlatformUI
			.getWorkbench().getService(ILabelProviderService.class);

	private final ICodeService codeService = (ICodeService) PlatformUI
			.getWorkbench().getService(ICodeService.class);
	private final ICodeServiceListener codeServiceListener = new CodeServiceAdapter() {
		@Override
		public void memoRemoved(URI uri) {
			AbstractOutputView.this.refreshHeader();
		}

		@Override
		public void memoAdded(URI uri) {
			AbstractOutputView.this.refreshHeader();
		}

		@Override
		public void codesRemoved(List<ICode> removedCodes, List<URI> uris) {
			AbstractOutputView.this.refreshHeader();
		}

		@Override
		public void codesAssigned(List<ICode> codes, List<URI> uris) {
			AbstractOutputView.this.refreshHeader();
		}

		@Override
		public void codeDeleted(ICode code) {
			AbstractOutputView.this.refreshHeader();
		}
	};
	private final ICompilationService compilationService = (ICompilationService) PlatformUI
			.getWorkbench().getService(ICompilationService.class);
	private final ICompilationServiceListener compilationServiceListener = new CompilationServiceAdapter() {
		@Override
		public void compilationStateChanged(ICompilable[] compilables,
				Boolean state) {
			AbstractOutputView.this.refreshHeader();
		}
	};

	public AbstractOutputView(boolean selectionSensitive,
			boolean editorSensitive) {
		super(new UriPartRenamerConverter(), 2000, ToolbarSet.TERMINAL, true);
		this.selectionSensitive = selectionSensitive;
		this.editorSensitive = editorSensitive;
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
		if (this.selectionSensitive) {
			SelectionUtils.getSelectionService(
					this.getSite().getWorkbenchWindow())
					.addPostSelectionListener(this.selectionListener);
		}
		if (this.editorSensitive) {
			this.getSite().getPage().addPartListener(this.partListener);
		}
		this.codeService.addCodeServiceListener(this.codeServiceListener);
		this.compilationService
				.addCompilationServiceListener(this.compilationServiceListener);
	}

	@Override
	public void dispose() {
		this.compilationService
				.removeCompilationServiceListener(this.compilationServiceListener);
		this.codeService.removeCodeServiceListener(this.codeServiceListener);
		if (this.editorSensitive) {
			this.getSite().getPage().removePartListener(this.partListener);
		}
		if (this.selectionSensitive) {
			SelectionUtils.getSelectionService(
					this.getSite().getWorkbenchWindow())
					.removePostSelectionListener(this.selectionListener);
		}
		super.dispose();
	}

}
